package se.studieresan.studs.data

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import se.studieresan.studs.extensions.FirebaseAPI
import se.studieresan.studs.models.User

/**
 * Source of users
 */
class UserRepo {

    private var userListener: ValueEventListener? = null
    private val userRef by lazy {
        val db = FirebaseDatabase.getInstance()
        db.getReference("users")
    }
    private var users: MutableLiveData<List<User>>? = null

    fun getUsers(): LiveData<List<User>>? =
            if (users == null) {
                users = MutableLiveData<List<User>>()
                loadUsers()
                users
            } else users

    private fun loadUsers() {
        userListener = FirebaseAPI.createValueEventListener({ snap ->
            val newUsers = snap.children.map { data ->
                val user = data.getValue(User::class.java) ?: return@createValueEventListener
                user.id = data.key
                user
            }
            users?.value = newUsers
        })
        userRef.addListenerForSingleValueEvent(userListener)
    }

    fun clear() = userListener?.let {
        userRef?.removeEventListener(it)
    }

}
