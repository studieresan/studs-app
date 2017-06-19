package se.studieresan.studs

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener
import se.studieresan.studs.extensions.FirebaseAPI
import se.studieresan.studs.models.Location
import se.studieresan.studs.models.Todo
import se.studieresan.studs.models.User


/**
 * Created by jespersandstrom on 2017-06-07.
 */
class StudsViewModel : ViewModel() {
    companion object {
        val TAG = StudsViewModel::class.java.simpleName
        val ONE_HOUR = 60*60
    }
    private val locationRef by lazy {
        val db = FirebaseDatabase.getInstance()
        val oneHourAgo = System.currentTimeMillis()/1000 - ONE_HOUR
        db.getReference("locations").orderByChild("timestamp").startAt(oneHourAgo.toDouble())
    }
    private val userRef by lazy {
        val db = FirebaseDatabase.getInstance()
        db.getReference("users")
    }
    private val staticRef by lazy {
        val db = FirebaseDatabase.getInstance()
        db.getReference("static/faq")
    }
    private val todoRef by lazy {
        val db = FirebaseDatabase.getInstance()
        db.getReference("static/locations")
    }
    private var locationListener: ChildEventListener? = null
    private var userListener: ValueEventListener? = null
    private var staticListener: ValueEventListener? = null
    private var todoListener: ValueEventListener? = null

    private var posts: MutableLiveData<List<Location>>? = null
    fun getPosts(): LiveData<List<Location>>? {
        if (posts == null) {
            posts = MutableLiveData<List<Location>>()
            loadPosts()
        }
        return posts
    }

    private var todo: MutableLiveData<List<Todo>>? = null
    fun getTodos(): LiveData<List<Todo>>? {
        if (todo == null) {
            todo = MutableLiveData<List<Todo>>()
            todoListener = FirebaseAPI.createValueEventListener({ snap ->
                val t = object : GenericTypeIndicator<ArrayList<Todo>>() {}
                todo?.value = snap.getValue(t)
            })
            todoRef.addValueEventListener(todoListener)
        }
        return todo
    }

    private var users: MutableLiveData<List<User>>? = null
    fun getUsers(): LiveData<List<User>>? {
        if (users == null) {
            users = MutableLiveData<List<User>>()
            loadUsers()
        }
        return users
    }

    private var static: MutableLiveData<String>? = null
    fun getStatic(): LiveData<String>? {
        if (static == null) {
            static = MutableLiveData<String>()
            staticListener = FirebaseAPI.createValueEventListener({ snap ->
                static?.value = snap.value as String
            })
            staticRef.addValueEventListener(staticListener)
        }
        return static
    }

    private var selectedPost: MutableLiveData<Location> = MutableLiveData<Location>()
    fun getSelectedPost(): LiveData<Location> = selectedPost
    fun selectPost(location: Location) {
        selectedPost.value = location
    }
    fun unselectPost() {
        selectedPost.value = null
    }

    private var selectedTodo: MutableLiveData<Todo> = MutableLiveData<Todo>()
    fun getSelectedTodo(): LiveData<Todo> = selectedTodo
    fun selectTodo(todo: Todo) {
        selectedTodo.value = todo
    }
    fun unselectTodo() {
        selectedTodo.value = null
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
        staticListener?.let {
            staticRef.removeEventListener(it)
        }
        todoListener?.let {
            todoRef.removeEventListener(it)
        }
    }

}
