package se.studieresan.studs.components.share.domain

import android.location.Geocoder
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.spotify.mobius.Connectable
import com.spotify.mobius.Connection
import com.spotify.mobius.functions.Consumer
import io.reactivex.disposables.Disposable
import se.studieresan.studs.services.backend.UserSource
import se.studieresan.studs.components.share.domain.ShareError.LocationAddressInvalid
import se.studieresan.studs.components.share.domain.ShareState.Finished
import java.util.*

class ShareEffectHandler(
        private val userSource: UserSource,
        private val firestore: FirebaseFirestore,
        private val geocoder: Geocoder
): Connectable<ShareEffect, ShareEvent> {

    private var userSub: Disposable? = null

    private fun accept(effect: ShareEffect, output: Consumer<ShareEvent>) {
        val dispatch = output::accept
        when (effect) {

            is FetchUsers -> {
                userSub = userSource.fetchUsers().subscribe { users ->
                    dispatch(UsersChanged(users.toList()))
                }
            }

            is Submit -> {
                val model = effect.model
                val user = userSource.getLoggedInUser(model.users.toSet())
                        ?: throw IllegalStateException("User or Users did not exist")

                val location = geocoder
                        .getFromLocationName(model.location, 1)
                        .firstOrNull()
                if (location != null) {
                    val activity = mapOf(
                            "author" to user.id,
                            "description" to model.description,
                            "category" to effect.model.selectedCategory.asString,
                            "isUserActivity" to true,
                            "createdDate" to Timestamp(model.start ?: Date()),
                            "endDate" to Timestamp(
                                    model.end ?: Date( Date().time + 3600 * 1000)
                            ),
                            "location" to mapOf(
                                    "address" to model.location,
                                    "coordinate" to GeoPoint(location.latitude, location.longitude)
                            )
                    )
                    firestore.collection("activities")
                            .add(activity)
                    output.accept(StateChanged(Finished)) // TODO do when response is received
                } else {
                    output.accept(ErrorAdded(LocationAddressInvalid))
                }
            }

        }
    }

    private fun dispose() {
        userSub?.dispose()
    }

    override fun connect(output: Consumer<ShareEvent>): Connection<ShareEffect> {
        return object: Connection<ShareEffect> {
            override fun accept(value: ShareEffect) = this@ShareEffectHandler.accept(value, output)
            override fun dispose() = this@ShareEffectHandler.dispose()
        }
    }

}
