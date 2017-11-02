package se.studieresan.studs.data

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.Query
import se.studieresan.studs.models.Location

/**
 * Source of user created location posts.
 */
class PostRepo : FirebaseRepo<List<Location>>("locations", isChildEventListener = true){

    override fun createDBRef(): Query {
        val ref = super.createDBRef()
        val POST_LIFETIME = 60*60 * 24 // Posts disappear after 24 hours
        val oneHourAgo = System.currentTimeMillis()/1000 - POST_LIFETIME
        return ref.orderByChild("timestamp").startAt(oneHourAgo.toDouble())
    }

    override fun convertSnap(snap: DataSnapshot): List<Location>? {
        val location = snap.getValue(Location::class.java)!!
        location.key = snap.key
        Log.d("asdf", location.toString())
        val currentPosts = data?.value ?: emptyList()
        return listOf(location) + currentPosts
    }

}
