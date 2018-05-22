package se.studieresan.studs

import android.util.Log
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot

private val TAG = "FirestoreKotlin"

fun CollectionReference.addSafeSnapshotListener(body: (QuerySnapshot) -> Unit): ListenerRegistration {
    return this.addSnapshotListener { snap, exception ->
        if (snap == null || exception != null) {
            Log.d(TAG, "FETCH FAILED: $exception")
        } else {
            body(snap)
        }
    }
}

fun QueryDocumentSnapshot.dateOrNull(key: String) =
        this.getTimestamp(key)?.toDate()
