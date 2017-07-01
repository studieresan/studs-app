package se.studieresan.studs.data

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.FirebaseDatabase
import se.studieresan.studs.extensions.FirebaseAPI
import se.studieresan.studs.models.Location

/**
 * Source of user created location posts.
 */
class PostRepo {

    private val POST_LIFETIME = 60*60 * 24 // Posts disappear after 24 hours
    private var postListener: ChildEventListener? = null
    private var posts: MutableLiveData<List<Location>>? = null
    private val postRef by lazy {
        val db = FirebaseDatabase.getInstance()
        val oneHourAgo = System.currentTimeMillis()/1000 - POST_LIFETIME
        db.getReference("locations").orderByChild("timestamp").startAt(oneHourAgo.toDouble())
    }

    fun getPosts(): LiveData<List<Location>>? =
            if (posts == null) {
                posts = MutableLiveData<List<Location>>()
                loadPosts()
                posts
            } else posts

    private fun loadPosts() {
        postListener = FirebaseAPI.createChildEventListener({ snap ->
            val data = snap.getValue(Location::class.java) ?: return@createChildEventListener
            data.key = snap.key
            val currentPosts = posts?.value ?: emptyList()
            posts?.value = listOf(data) + currentPosts
        })
        postRef.addChildEventListener(postListener)
    }

    fun clear() = postListener?.let {
        postRef?.removeEventListener(it)
    }
}
