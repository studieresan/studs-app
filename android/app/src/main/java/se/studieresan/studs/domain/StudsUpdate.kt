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
                        loadingCities = false
                ))

            is LoadedActivities ->
                next(model.copy(
                        activities = model.activities.unionBy(event.activities) { a, b -> a.id == b.id },
                        loadingActivities = false
                ))

            is LoadedRegistrations -> {
                val registeredUsers = event
                        .registrations
                        .map { it.userId }.toSet()

                val registering = model
                        .registeringUserIdsForActivity[event.activityId]
                        ?.filter { user ->
                            !registeredUsers.contains(user)
                        }
                        ?.toSet() ?: emptySet()

                val unregistering = model
                        .unregisteringUserIdsForActivity[event.activityId]
                        ?.filter { user ->
                            registeredUsers.contains(user)
                        }
                        ?.toSet() ?: emptySet()

                next(model.copy(
                        registrations = event.registrations,
                        loadingRegistrations = false,
                        registeringUserIdsForActivity =
                        model.registeringUserIdsForActivity +
                                (event.activityId to registering),
                        unregisteringUserIdsForActivity =
                        model.unregisteringUserIdsForActivity +
                                (event.activityId to unregistering)
                ))
            }

            is LoadedUsers ->
                next(model.copy(
                        users = event.users,
                        loadingAllUsers = false
                ))

            is RegisterUser -> {
                val oldSet = model.registeringUserIdsForActivity
                        .getOrDefault(event.activityId, emptySet())
                next(model.copy(
                        registeringUserIdsForActivity =
                        model.registeringUserIdsForActivity
                                + (event.activityId to oldSet + event.userId)),
                        setOf(RegisterRemotely(event.userId, event.activityId))
                )
            }

            is UnregisterUser -> {
                val activity = event.registration.activityId
                val user = event.registration.userId
                val oldSet = model.unregisteringUserIdsForActivity.getOrDefault(activity, emptySet())
                next(model.copy(
                        unregisteringUserIdsForActivity =
                        model.unregisteringUserIdsForActivity
                                + (activity to oldSet + user)),
                        setOf(UnregisterRemotely(event.registration))
                )
            }

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
                    model.copy(loadingCities = true),
                    setOf(Fetch(loadable))
            )

            is ActivitiesLoadable -> next(
                    model.copy(loadingActivities = true),
                    setOf(Fetch(loadable))
            )

            is RegistrationsLoadable -> next(
                    model.copy(
                            loadingRegistrations = true,
                            registrations = emptySet(),
                            registeringUserIdsForActivity =
                            model.registeringUserIdsForActivity + (loadable.activityId to emptySet()),
                            unregisteringUserIdsForActivity =
                            model.unregisteringUserIdsForActivity + (loadable.activityId to emptySet())
                    ),
                    setOf(Fetch(loadable))
            )

            is UsersLoadable -> next(
                    model.copy(loadingAllUsers = true),
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
