package se.studieresan.studs.data

import com.google.firebase.database.DataSnapshot
import se.studieresan.studs.models.User

/**
 * Source of users
 */
class UserRepo : FirebaseRepo<List<User>>("users") {

    override fun convertSnap(snap: DataSnapshot): List<User>? =
        snap.children.map { data ->
            val user = data.getValue(User::class.java)!!
            user.id = data.key
            user
        }

}
