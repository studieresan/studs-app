package se.studieresan.studs.data

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import com.google.firebase.database.*
import se.studieresan.studs.extensions.FirebaseAPI

/**
 * Generic wrapper for data fetched from Firebase.
 * When extending, you should only need to implement the 'convertSnap' method which describes
 * how to convert a Firebase snapshot to the parametrized data type.
 * IMPORTANT: Remember to call clear() when you are done with the data to prevent leaks.
 */
abstract class FirebaseRepo<T>(
        val path: String,
        val isChildEventListener: Boolean = false
) {

    private val childEventListener: ChildEventListener by lazy {
        FirebaseAPI.createChildEventListener({ snap ->
            snap.let {
                data?.value = convertSnap(it)
            }
        })
    }
    private val valueEventListener: ValueEventListener by lazy {
        FirebaseAPI.createValueEventListener { snap ->
            snap.let {
                data?.value = convertSnap(it)
            }
        }
    }

    private val dbRef by lazy {
      createDBRef()
    }

    protected var data: MutableLiveData<T>? = null

    fun load(): LiveData<T>? =
        if (data == null) {
            data = MutableLiveData()
            if (isChildEventListener) dbRef.addChildEventListener(childEventListener)
            else dbRef.addValueEventListener(valueEventListener)
            data
        } else data

    fun clear() =
            if (data != null) {
                if (isChildEventListener) dbRef.removeEventListener(childEventListener)
                else dbRef.removeEventListener(valueEventListener)
            }
            else Unit

    open fun createDBRef(): Query {
        val db = FirebaseDatabase.getInstance()
        return db.getReference(path)
    }

    abstract fun convertSnap(snap: DataSnapshot): T?

}
