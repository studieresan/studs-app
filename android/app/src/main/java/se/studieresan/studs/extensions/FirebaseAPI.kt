package se.studieresan.studs.extensions

import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

object FirebaseAPI {

    private val TAG = FirebaseAPI::class.java.simpleName

    fun createValueEventListener(
            onData: (DataSnapshot) -> Unit
    ): ValueEventListener {
        return object: ValueEventListener {
            override fun onDataChange(snap: DataSnapshot?) {
                snap ?: return
                onData(snap)
            }

            override fun onCancelled(error: DatabaseError?) {}
        }
    }

    fun createChildEventListener(
            onAdded: (DataSnapshot) -> Unit,
            onRemoved: (DataSnapshot) -> Unit = {},
            onChanged: (DataSnapshot) -> Unit = {},
            onMoved: (DataSnapshot) -> Unit = {},
            onCancelled: (DatabaseError) -> Unit = {}
    ): ChildEventListener {
        return object: ChildEventListener {
            override fun onCancelled(error: DatabaseError?) {
                error ?: return
                onCancelled(error)
            }

            override fun onChildMoved(snap: DataSnapshot?, prevName: String?) {
                snap ?: return
                onMoved(snap)
            }

            override fun onChildChanged(snap: DataSnapshot?, prevName: String?) {
                snap ?: return
                onChanged(snap)
            }

            override fun onChildAdded(snap: DataSnapshot?, prevName: String?) {
                snap ?: return
                onAdded(snap)
            }

            override fun onChildRemoved(snap: DataSnapshot?) {
                snap ?: return
                onRemoved(snap)
            }
        }
    }
}

