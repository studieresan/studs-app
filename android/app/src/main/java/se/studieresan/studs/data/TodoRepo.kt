package se.studieresan.studs.data

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.GenericTypeIndicator
import se.studieresan.studs.models.Todo

/**
 * Source of TODOs
 */
class TodoRepo : FirebaseRepo<List<Todo>>("static/locations") {

    override fun convertSnap(snap: DataSnapshot): List<Todo>? {
        val t = object : GenericTypeIndicator<ArrayList<Todo>>() {}
        return snap.getValue(t)
    }

}
