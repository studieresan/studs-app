package se.studieresan.studs.domain

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.QuerySnapshot
import com.spotify.mobius.Connectable
import com.spotify.mobius.Connection
import com.spotify.mobius.disposables.Disposable
import com.spotify.mobius.functions.Consumer
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.experimental.channels.SendChannel
import kotlinx.coroutines.experimental.channels.actor
import kotlinx.coroutines.experimental.launch
import se.studieresan.studs.*
import se.studieresan.studs.models.Activity
import se.studieresan.studs.models.City
import se.studieresan.studs.models.Coordinate
import se.studieresan.studs.services.backend.UserSource

val TAG = EffectHandler::class.java.simpleName

class EffectHandler(
        private val userSource: UserSource,
        private val sharedPreferences: SharedPreferences,
        private val firestore: FirebaseFirestore,
        private val locationProvider: FusedLocationProviderClient
): Connectable<StudsEffect, StudsEvent> {

    private val listeners: MutableMap<String, Disposable?> = mutableMapOf()
    private var output: Consumer<StudsEvent>? = null
    private var isDisposed = false

    private var loadableChannel: SendChannel<Loadable>? = null

    @SuppressLint("ApplySharedPref", "MissingPermission")
    private fun accept(effect: StudsEffect, output: Consumer<StudsEvent>) {
        when (effect) {
            is Fetch -> launch {
                loadableChannel?.send(effect.loadable)
            }

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

    private fun parseCity(citySnap: QuerySnapshot) =
            citySnap.map { snap ->
                snap.toObject(City::class.java).copy(
                        id = snap.id,
                        start = snap.dateOrNull("startDate"),
                        end = snap.dateOrNull("endDate")
                )
            }.toSet()

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
