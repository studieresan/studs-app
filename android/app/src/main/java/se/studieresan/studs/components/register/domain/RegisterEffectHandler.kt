package se.studieresan.studs.components.register.domain

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.QuerySnapshot
import com.spotify.mobius.Connectable
import com.spotify.mobius.Connection
import com.spotify.mobius.functions.Consumer
import io.reactivex.disposables.Disposable
import se.studieresan.studs.addSafeSnapshotListener
import se.studieresan.studs.dateOrNull
import se.studieresan.studs.models.Registration
import se.studieresan.studs.services.backend.UserSource
import java.util.*


class RegisterEffectHandler(
        private val userSource: UserSource,
        private val firestore: FirebaseFirestore
): Connectable<RegisterEffect, RegisterEvent> {

    private var registrationListener: ListenerRegistration? = null
    private var userDiposable: Disposable? = null
    private var isDisposed = false

    private fun accept(effect: RegisterEffect, output: Consumer<RegisterEvent>) {
        when (effect) {

            is FetchRegistrations -> {
                val activityId = effect.activityId
                registrationListener = firestore.collection("activities")
                        .document(activityId)
                        .collection("people")
                        .addSafeSnapshotListener { snap ->
                            val registrations = parseRegistrations(snap, activityId)
                            output.accept(RegistrationsChanged(registrations))
                        }
            }

            is RegisterRemotely -> {
                userSource.getLoggedInUser(effect.users)?.let { user ->
                    val registration = mapOf<String, Any>(
                            "userId" to effect.userId,
                            "registeredById" to user.id,
                            "registeredAt" to Timestamp(Date())
                    )
                    firestore.collection("activities")
                            .document(effect.activityId)
                            .collection("people")
                            .add(registration)
                }
            }

            is UnregisterRemotely ->
                firestore.collection("activities")
                        .document(effect.registration.activityId)
                        .collection("people")
                        .document(effect.registration.id)
                        .delete()

            is FetchUsers -> {
                userDiposable = userSource
                        .fetchUsers()
                        .subscribe { users ->
                            synchronized(isDisposed) {
                                if (!isDisposed) {
                                    output.accept(UsersChanged(users.toList()))
                                }
                            }
                        }
            }

        }
    }

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


    private fun dispose() {
        synchronized(isDisposed) {
            registrationListener?.remove()
            isDisposed = true
        }
    }

    override fun connect(output: Consumer<RegisterEvent>): Connection<RegisterEffect> {
        synchronized(isDisposed) {
            isDisposed = false
        }
        return object: Connection<RegisterEffect> {
            override fun accept(value: RegisterEffect) = this@RegisterEffectHandler.accept(value, output)
            override fun dispose() = this@RegisterEffectHandler.dispose()
        }
    }
}
