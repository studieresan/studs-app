package se.studieresan.studs.components.register.domain

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import se.studieresan.studs.models.Registration
import se.studieresan.studs.models.StudsUser

@Parcelize
data class RegisterModel(
        val users: List<StudsUser> = emptyList(),
        val registrations: Set<Registration> = emptySet(),
        val activityId: String,
        val registeringUserIds: Set<String> = emptySet(),
        val unregisteringUserIds: Set<String> = emptySet(),
        val isLoadingRegistrations: Boolean = false,
        val isLoadingUsers: Boolean = false
): Parcelable

sealed class RegisterEvent
data class UsersChanged(val users: List<StudsUser>): RegisterEvent()
data class RegistrationsChanged(val registrations: Set<Registration>): RegisterEvent()
data class RegisterUser(val userId: String): RegisterEvent()
data class UnregisterUser(val registration: Registration): RegisterEvent()


sealed class RegisterEffect
object FetchUsers: RegisterEffect()
data class FetchRegistrations(val activityId: String): RegisterEffect()
data class RegisterRemotely(val userId: String, val activityId: String, val users: List<StudsUser>): RegisterEffect()
data class UnregisterRemotely(val registration: Registration): RegisterEffect()
