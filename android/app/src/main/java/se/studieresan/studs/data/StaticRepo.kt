package se.studieresan.studs.data

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import se.studieresan.studs.extensions.FirebaseAPI

/**
 * Source of static content such as the travel information.
 */
class StaticRepo {

    private val staticRef by lazy {
        val db = FirebaseDatabase.getInstance()
        db.getReference("static/faq")
    }
    private var staticListener: ValueEventListener? = null
    private var static: MutableLiveData<String>? = null

    fun getStatic(): LiveData<String>? =
            if (static == null) {
                static = MutableLiveData<String>()
                staticListener = FirebaseAPI.createValueEventListener({ snap ->
                    static?.value = snap.value as String
                })
                staticRef.addValueEventListener(staticListener)
                static
            } else static

    fun clear() = staticListener?.let {
        staticRef.removeEventListener(it)
    }

}
