package se.studieresan.studs.domain

import com.spotify.mobius.Next
import com.spotify.mobius.Next.dispatch
import com.spotify.mobius.Next.next

fun update(model: StudsModel, event: StudsEvent): Next<StudsModel, StudsEffect> =
        when (event) {
            is Load -> load(model, event.loadable)

            is LoadedCities ->
                next(model.copy(
                        citites = event.cities,
                        isLoadingCities = false
                ))

            is LoadedActivities ->
                next(model.copy(
                        activities = model.activities.unionBy(event.activities) { a, b -> a.id == b.id },
                        isLoadingActivities = false
                ))

            is LoadedUsers ->
                next(model.copy(
                        users = event.users,
                        isLoadingAllUsers = false
                ))

            is Logout -> dispatch(setOf(TriggerLogout))

            is SelectActivity ->
                next(model.copy(selectedActivityId = event.activityId))

            is UnselectActivity ->
                next(model.copy(selectedActivityId = null))

            is SelectPage -> next(model.copy(page = event.page))

            is ThemeChanged -> next(
                    model.copy(theme = event.theme),
                    setOf(SaveTheme(theme = event.theme))
            )

            is ThemeSetWithoutSaving -> next(model.copy(theme = event.theme))

            is SelectUserLocation -> dispatch(setOf(DetermineLocation))

            is LocationChanged -> next(model.copy(location = event.coordinate))
        }

private fun load(model: StudsModel, loadable: Loadable): Next<StudsModel, StudsEffect> =
        when (loadable) {
            is CitiesLoadable -> next(
                    model.copy(isLoadingCities = true),
                    setOf(Fetch(loadable))
            )

            is ActivitiesLoadable -> next(
                    model.copy(isLoadingActivities = true),
                    setOf(Fetch(loadable))
            )

            is UsersLoadable -> next(
                    model.copy(isLoadingAllUsers = true),
                    setOf(Fetch(UsersLoadable))
            )
        }

private fun <A> Set<A>.unionBy(other: Set<A>, same: (A, A) -> Boolean): Set<A> {
    val notInOther = this.filterNot { thisEl ->
        other.any { thatEl ->
            same(thisEl, thatEl)
        }
    }
    return notInOther.union(other)
}
