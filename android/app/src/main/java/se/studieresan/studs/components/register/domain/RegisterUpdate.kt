package se.studieresan.studs.components.register.domain

import com.spotify.mobius.Next
import se.studieresan.studs.next

val update: (RegisterModel, RegisterEvent) -> Next<RegisterModel, RegisterEffect> = {
    model, event ->
    when (event) {

        is RegistrationsChanged -> {
            val registeredUsers = event
                    .registrations
                    .map { it.userId }
                    .toSet()

            val registering = model
                    .registeringUserIds
                    .filter { !registeredUsers.contains(it) }
                    .toSet()

            val unregistering = model
                    .unregisteringUserIds
                    .filter { registeredUsers.contains(it) }
                    .toSet()

            next(model.copy(
                    registrations = event.registrations,
                    isLoadingRegistrations = false,
                    registeringUserIds = registering,
                    unregisteringUserIds = unregistering
            ))
        }

        is UsersChanged ->
            next(model = model.copy(
                    users = event.users,
                    isLoadingUsers = false
            ))

        is RegisterUser ->
                next(
                        model = model.copy(registeringUserIds = model.registeringUserIds + event.userId),
                        effects = setOf(RegisterRemotely(
                                userId = event.userId,
                                activityId = model.activityId,
                                users = model.users
                        ))
                )

        is UnregisterUser ->
            next(
                model = model.copy(unregisteringUserIds = model.unregisteringUserIds + event.registration.userId),
                effects = setOf(UnregisterRemotely(
                        registration = event.registration
                ))
            )
    }
}
