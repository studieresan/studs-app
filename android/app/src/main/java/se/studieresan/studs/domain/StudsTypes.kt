package se.studieresan.studs.domain

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import se.studieresan.studs.R
import se.studieresan.studs.domain.Page.UserShares
import se.studieresan.studs.models.*

@SuppressWarnings("ParcelCreator")
@Parcelize
data class StudsModel(
        val citites: Set<City> = emptySet(),
        val users: Set<StudsUser> = emptySet(),
        val activities: Set<Activity> = emptySet(),
        val registrations: Set<Registration> = emptySet(),
        val location: Coordinate? = null,
        val isLoadingAllUsers: Boolean = false,
        val isLoadingCities: Boolean = false,
        val isLoadingActivities: Boolean = false,
        val theme: Theme = Theme.Night,
        val selectedActivityId: String? = null,
        val page: Page = UserShares
): Parcelable {
    init {}
}

sealed class Loadable
object ActivitiesLoadable: Loadable()
object CitiesLoadable: Loadable()
object UsersLoadable: Loadable()

sealed class StudsEvent
data class Load(val loadable: Loadable): StudsEvent()
data class LoadedCities(val cities: Set<City>): StudsEvent()
data class LoadedActivities(val activities: Set<Activity>): StudsEvent()
data class LoadedUsers(val users: Set<StudsUser>): StudsEvent()
data class SelectActivity(val activityId: String): StudsEvent()
data class SelectPage(val page: Page): StudsEvent()
data class LocationChanged(val coordinate: Coordinate): StudsEvent()
data class ThemeSetWithoutSaving(val theme: Theme): StudsEvent()
data class ThemeChanged(val theme: Theme): StudsEvent()
object UnselectActivity: StudsEvent()
object Logout: StudsEvent()
object SelectUserLocation: StudsEvent()

sealed class StudsEffect
data class Fetch(val loadable: Loadable): StudsEffect()
data class SaveTheme(val theme: Theme): StudsEffect()
object RestoreTheme: StudsEffect()
object TriggerLogout: StudsEffect()
object DetermineLocation: StudsEffect()

enum class Page {
    TravelTips,
    Info,
    UserShares,
}

enum class Theme(val description: String, val resource: Int) {
    Night("Night", R.raw.night_style_json),
    Standard("Standard", R.raw.light_style_json),
    BlackAndWhite("Black and White", R.raw.black_and_white_style_json),
    Dark("Very Dark", R.raw.dark_style_json),
    Retro("Retro", R.raw.retro_style_json)
}
