package se.studieresan.studs

import com.spotify.mobius.Connectable
import com.spotify.mobius.Mobius
import com.spotify.mobius.Next
import com.spotify.mobius.Update
import kotlin.properties.ObservableProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

fun <M, F> next(model: M? = null, effects: Set<F> = emptySet()): Next<M, F> =
        Next.next(model, effects)

fun <M, E, F> loopFrom(update: (M, E) -> Next<M, F>, effectHandler: Connectable<F, E>) =
        Mobius.loop(
                Update<M, E, F> { model, event -> update(model, event) },
                effectHandler
        )

inline fun <T> didChange(initialValue: T, crossinline didChange: (newValue: T) -> Unit):
        ReadWriteProperty<Any?, T> = object : ObservableProperty<T>(initialValue) {
        override fun afterChange(property: KProperty<*>, oldValue: T, newValue: T) {
                if (oldValue != newValue) didChange(newValue)
        }
}
