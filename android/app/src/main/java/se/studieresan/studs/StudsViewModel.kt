package se.studieresan.studs

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import se.studieresan.studs.data.PostRepo
import se.studieresan.studs.data.StaticRepo
import se.studieresan.studs.data.TodoRepo
import se.studieresan.studs.data.UserRepo
import se.studieresan.studs.models.Location
import se.studieresan.studs.models.Todo
import se.studieresan.studs.models.User


/**
 * Created by jespersandstrom on 2017-06-07.
 */
class StudsViewModel : ViewModel() {

    private val userRepo: UserRepo = UserRepo()
    private val todoRepo: TodoRepo = TodoRepo()
    private val postRepo: PostRepo = PostRepo()
    private val staticRepo: StaticRepo = StaticRepo()

    private var selectedPost: MutableLiveData<Location> = MutableLiveData()
    fun getSelectedPost(): LiveData<Location> = selectedPost
    fun selectPost(location: Location) {
        selectedPost.value = location
    }
    fun unselectPost() {
        selectedPost.value = null
    }

    private var selectedTodo: MutableLiveData<Todo> = MutableLiveData()
    fun getSelectedTodo(): LiveData<Todo> = selectedTodo
    fun selectTodo(todo: Todo) {
        selectedTodo.value = todo
    }
    fun unselectTodo() {
        selectedTodo.value = null
    }

    fun getUsers(): LiveData<List<User>>? = userRepo.load()
    fun getTodos(): LiveData<List<Todo>>? = todoRepo.load()
    fun getPosts(): LiveData<List<Location>>? = postRepo.load()
    fun getStaticContent(): LiveData<String>? = staticRepo.load()

    override fun onCleared() {
        super.onCleared()
        userRepo.clear()
        todoRepo.clear()
        postRepo.clear()
        staticRepo.clear()
    }

}
