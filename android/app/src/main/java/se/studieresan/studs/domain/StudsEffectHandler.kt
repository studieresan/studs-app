package se.studieresan.studs.domain

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.firebase.Timestamp
import com.google.firebase.firestore.*
import com.spotify.mobius.Connectable
import com.spotify.mobius.Connection
import com.spotify.mobius.disposables.Disposable
import com.spotify.mobius.functions.Consumer
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.experimental.channels.SendChannel
import kotlinx.coroutines.experimental.channels.actor
import kotlinx.coroutines.experimental.launch
import se.studieresan.studs.COOKIES
import se.studieresan.studs.LOGGED_IN
import se.studieresan.studs.THEME_KEY
import se.studieresan.studs.models.Activity
import se.studieresan.studs.models.City
import se.studieresan.studs.models.Coordinate
import se.studieresan.studs.models.Registration
import se.studieresan.studs.services.backend.UserSource
import java.util.*

val TAG = EffectHandler::class.java.simpleName

class EffectHandler(
        private val userSource: UserSource,
        private val sharedPreferences: SharedPreferences,
        private val firestore: FirebaseFirestore,
        private val locationProvider: FusedLocationProviderClient
): Connectable<StudsEffect, StudsEvent> {

    private val listeners: MutableMap<String, Disposable?> = mutableMapOf()
    private var registrationListener: ListenerRegistration? = null
    private var output: Consumer<StudsEvent>? = null
    private var isDisposed = false

    var loadableChannel: SendChannel<Loadable>? = null

    @SuppressLint("ApplySharedPref", "MissingPermission")
    private fun accept(effect: StudsEffect, output: Consumer<StudsEvent>) {
        when (effect) {
            is Fetch -> launch {
                loadableChannel?.send(effect.loadable)
            }

            is RegisterRemotely -> {
                val registration = mapOf<String, Any>(
                        "userId" to effect.userId,
                        "registeredById" to effect.userId, // TODO set the correct ID here
                        "registeredAt" to Timestamp(Date())
                )
                firestore.collection("activities")
                        .document(effect.activityId)
                        .collection("people")
                        .add(registration)
            }

            is UnregisterRemotely ->
                firestore.collection("activities")
                        .document(effect.registration.activityId)
                        .collection("people")
                        .document(effect.registration.id)
                        .delete()

            is TriggerLogout -> logout()

            is SaveTheme ->
                sharedPreferences.edit()
                        .putString(THEME_KEY, effect.theme.name)
                        .commit()

            is RestoreTheme -> {
                val name = sharedPreferences.getString(THEME_KEY, Theme.Night.name)
                val theme = enumValueOf<Theme>(name)
                output.accept(ThemeSetWithoutSaving(theme))
            }

            is DetermineLocation -> {
                locationProvider
                        .lastLocation
                        .addOnSuccessListener { location ->
                            if (!isDisposed) {
                                val coordinate = Coordinate(
                                        latitude = location.latitude,
                                        longitude = location.longitude
                                )
                                output.accept(LocationChanged(coordinate))
                            }
                        }
                        .addOnFailureListener {
                            Log.d(TAG, "FAILED TO GET LOCATION $it")
                        }
            }
        }
    }

    private fun fetch(loadable: Loadable, output: Consumer<StudsEvent>) {
        synchronized(listeners) {
            if (isDisposed) return
            val (id, listener) = when (loadable) {
                is CitiesLoadable -> {
                    if (listeners.containsKey("cities")) return@synchronized
                    "cities" to firestore.collection("cities")
                            .addSafeSnapshotListener { snap ->
                                val cities = parseCity(snap)
                                output.accept(LoadedCities(cities))
                            }
                }

                is ActivitiesLoadable -> {
                    if (listeners.containsKey("activities")) return@synchronized
                    "activities" to firestore.collection("activities")
                            .addSafeSnapshotListener { snap ->
                                val activities = parseActivities(snap)
                                output.accept(LoadedActivities(activities))
                            }
                }

                is RegistrationsLoadable -> {
                    registrationListener?.remove()
                    registrationListener = firestore.collection("activities")
                            .document(loadable.activityId)
                            .collection("people")
                            .addSafeSnapshotListener { snap ->
                                val registrations = parseRegistrations(snap, loadable.activityId)
                                output.accept(LoadedRegistrations(registrations, loadable.activityId))
                            }
                    "registrations" to registrationListener
                }

                is UsersLoadable -> {
                    if (listeners.containsKey("users")) return@synchronized
                    "users" to userSource
                            .fetchUsers()
                            .observeOn(Schedulers.computation())
                            .subscribe { users ->
                                if (!isDisposed) output.accept(LoadedUsers(users))
                            }
                }
            }
            if (listener == null) return
            listeners += when (listener) {
                is io.reactivex.disposables.Disposable -> id to Disposable { listener.dispose() }
                is ListenerRegistration -> id to Disposable { listener.remove() }
                else -> throw IllegalStateException("Unhandled listener type ${listener::class.java.simpleName}")
            }
        }
    }

    private fun CollectionReference.addSafeSnapshotListener(body: (QuerySnapshot) -> Unit): ListenerRegistration {
        return this.addSnapshotListener { snap, exception ->
            if (snap == null || exception != null) {
                Log.d(TAG, "FETCH FAILED: $exception")
            } else {
                body(snap)
            }
        }
    }

    private fun parseCity(citySnap: QuerySnapshot) =
            citySnap.map { snap ->
                snap.toObject(City::class.java).copy(
                        id = snap.id,
                        start = snap.dateOrNull("startDate"),
                        end = snap.dateOrNull("endDate")
                )
            }.toSet()

    private fun parseRegistrations(registrationSnap: QuerySnapshot, activityId: String): Set<Registration> =
            registrationSnap
                    .map { snap ->
                        snap.toObject(Registration::class.java).copy(
                                id = snap.id,
                                activityId = activityId,
                                time = snap.dateOrNull("registeredAt")
                        )
                    }
                    .toSet()

    private fun parseActivities(activitiesSnap: QuerySnapshot): Set<Activity> =
            activitiesSnap.map { snap ->
                val obj = snap.toObject(Activity::class.java)
                val point = (snap.data["location"] as Map<String, Any>)["coordinate"] as GeoPoint
                val coordinate = Coordinate(point.latitude, point.longitude)
                obj.copy(
                        id = snap.id,
                        created = snap.dateOrNull("createdDate"),
                        start = snap.dateOrNull("startDate"),
                        end = snap.dateOrNull("endDate"),
                        location = obj.location.copy(
                                latLng = coordinate
                        ),
                        isUserActivity = snap.getBoolean("isUserActivity")
                )
            }.toSet()

    private fun dispose() {
        loadableChannel?.close()
        synchronized(listeners) {
            isDisposed = true
            listeners.values.forEach { listener ->
                listener?.dispose()
            }
            listeners.clear()
        }
    }

    private fun QueryDocumentSnapshot.dateOrNull(key: String) =
            this.getTimestamp(key)?.toDate()

    @SuppressLint("ApplySharedPref")
    private fun logout() {
        sharedPreferences
                .edit()
                .putBoolean(LOGGED_IN, false)
                .commit()
        sharedPreferences
                .edit()
                .remove(COOKIES)
                .commit()
    }

    override fun connect(output: Consumer<StudsEvent>): Connection<StudsEffect> {
        this.output = output
        isDisposed = false
        listeners.clear()
        loadableChannel = actor {
            for (loadable in channel) {
                fetch(loadable, output)
            }
        }
        return  object: Connection<StudsEffect> {
            override fun accept(value: StudsEffect) = accept(value, output)
            override fun dispose() = this@EffectHandler.dispose()
        }
    }
}
