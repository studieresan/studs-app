package se.studieresan.studs.data

import com.google.firebase.database.DataSnapshot

/**
 * Source of static content such as the travel information.
 */
class StaticRepo : FirebaseRepo<String>("static/faq") {

    override fun convertSnap(snap: DataSnapshot) = snap.value as String

}
