package se.studieresan.studs.services.backend

import android.content.SharedPreferences
import android.util.Log
import io.reactivex.Single
import se.studieresan.studs.EMAIL
import se.studieresan.studs.models.StudsUser

interface UserSource {

    fun fetchUsers(): Single<Set<StudsUser>>

    fun getLoggedInUser(users: Set<StudsUser>): StudsUser?

}

val TAG = UserSourceImpl::class.java.simpleName
class UserSourceImpl(
        private val backendService: BackendService,
        private val preferences: SharedPreferences
): UserSource {

    var users: Set<StudsUser>? = null

    override fun fetchUsers(): Single<Set<StudsUser>> =
            if (users == null) {
                Log.d(TAG, "no users")
                backendService
                        .fetchUsers()
                        .flatMap {
                            val newUsers = it.data.studsUsers.toSet()
                            users = newUsers
                            Single.just(newUsers)
                        }
                        .doOnError {
                            Log.d(TAG, "ERROR FETCHING USERS: $it")
                        }
            } else Single.just(users)

    override fun getLoggedInUser(users: Set<StudsUser>): StudsUser? {
        val email = preferences.getString(EMAIL, "none")
        return users.find { it.profile.email == email }
    }

}
