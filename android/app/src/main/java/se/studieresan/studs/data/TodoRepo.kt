package se.studieresan.studs.data

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener
import se.studieresan.studs.extensions.FirebaseAPI
import se.studieresan.studs.models.Todo

/**
 * Source of TODOs
 */
class TodoRepo {

    private var todoListener: ValueEventListener? = null
    private val todoRef by lazy {
        val db = FirebaseDatabase.getInstance()
        db.getReference("static/locations")
    }
    private var todo: MutableLiveData<List<Todo>>? = null

    fun getTodos(): LiveData<List<Todo>>? =
            if (todo == null) {
                todo = MutableLiveData<List<Todo>>()
                todoListener = FirebaseAPI.createValueEventListener({ snap ->
                    val t = object : GenericTypeIndicator<ArrayList<Todo>>() {}
                    todo?.value = snap.getValue(t)
                })
                todoRef.addValueEventListener(todoListener)
                todo
            } else todo

    fun clear() = todoListener?.let {
        todoRef.removeEventListener(it)
    }
}
