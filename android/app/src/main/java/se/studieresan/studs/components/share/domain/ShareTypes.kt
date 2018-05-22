package se.studieresan.studs.components.share.domain

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import se.studieresan.studs.components.share.domain.ShareState.New
import se.studieresan.studs.models.Category
import se.studieresan.studs.models.StudsUser
import java.util.*

@Parcelize
data class ShareModel(
        val users: List<StudsUser> = emptyList(),
        val goingUserIds: Set<String> = emptySet(),
        val selectedCategory: Category = Category.Attraction,
        val description: String = "",
        val location: String = "",
        val start: Date? = null,
        val end: Date? = null,
        val errors: Set<ShareError> = emptySet(),
        val isLoadingUsers: Boolean = false,
        val state: ShareState = New
): Parcelable

sealed class ShareEvent
data class UsersChanged(val users: List<StudsUser>): ShareEvent()
data class GoingChanged(val goingUserIds: Set<String>): ShareEvent()
data class CategoryChanged(val category: Category): ShareEvent()
data class DescriptionChanged(val description: String): ShareEvent()
data class LocationChanged(val location: String): ShareEvent()
data class PersonGoing(val person: StudsUser): ShareEvent()
data class PersonNotGoing(val person: StudsUser): ShareEvent()
data class ErrorAdded(val error: ShareError): ShareEvent()
data class StateChanged(val state: ShareState): ShareEvent()
object SubmitClicked: ShareEvent()

sealed class ShareEffect
object FetchUsers: ShareEffect()
data class Submit(val model: ShareModel): ShareEffect()

enum class ShareError {
    DescriptionMissing,
    DescriptionLineCount,
    DescriptionCharCount,
    LocationMissing,
    LocationAddressInvalid,
}

enum class ShareState {
    New,
    Editing,
    Finished
}
