package se.studieresan.studs.extensions

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.LiveData

/**
 * Extensions to the Android Architecture component api
 */
fun <T> LiveData<T>.observeNotNull(owner: LifecycleOwner, body: (T) -> Unit) =
        this.observe(owner, android.arch.lifecycle.Observer {
            it ?: return@Observer
            body(it)
        })
