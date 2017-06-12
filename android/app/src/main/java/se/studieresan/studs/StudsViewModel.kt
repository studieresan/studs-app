package se.studieresan.studs

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.util.Log
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import se.studieresan.studs.extensions.FirebaseAPI
import se.studieresan.studs.models.Location
import se.studieresan.studs.models.User


/**
 * Created by jespersandstrom on 2017-06-07.
 */
class StudsViewModel : ViewModel() {
    companion object {
        val TAG = StudsViewModel::class.java.simpleName
    }
    private val locationRef by lazy {
        val db = FirebaseDatabase.getInstance()
        db.getReference("locations").limitToLast(20)
    }
    private val userRef by lazy {
        val db = FirebaseDatabase.getInstance()
        db.getReference("users")
    }
    private var locationListener: ChildEventListener? = null
    private var userListener: ValueEventListener? = null

    private var posts: MutableLiveData<List<Location>>? = null
    fun getPosts(): LiveData<List<Location>>? {
        if (posts == null) {
            posts = MutableLiveData<List<Location>>()
            loadPosts()
        }
        return posts
    }

    private var users: MutableLiveData<List<User>>? = null
    fun getUsers(): LiveData<List<User>>? {
        if (users == null) {
            users = MutableLiveData<List<User>>()
            loadUsers()
        }
        return users
    }

    private var selectedPost: MutableLiveData<Location> = MutableLiveData<Location>()
    fun getSelectedPost(): LiveData<Location> = selectedPost
    fun selectPost(location: Location) {
        selectedPost.value = location
    }

    private fun loadPosts() {
        locationListener = FirebaseAPI.createChildEventListener({ snap ->
            val data = snap.getValue(Location::class.java) ?: return@createChildEventListener
            data.key = snap.key
            val currentPosts = posts?.value ?: emptyList()
            posts?.value = listOf(data) + currentPosts
        })
        locationRef.addChildEventListener(locationListener)
    }

    private fun loadUsers() {
        userListener = FirebaseAPI.createValueEventListener({ snap ->
            Log.d(TAG, "new users $snap")
            val newUsers = snap.children.map { data ->
                val user = data.getValue(User::class.java) ?: return@createValueEventListener
                user.id = data.key
                user
            }
            users?.value = newUsers
        })
        userRef.addListenerForSingleValueEvent(userListener)
    }

    override fun onCleared() {
        super.onCleared()
        locationListener?.let {
            locationRef?.removeEventListener(it)
        }
        userListener?.let {
            userRef?.removeEventListener(it)
        }
    }

}
