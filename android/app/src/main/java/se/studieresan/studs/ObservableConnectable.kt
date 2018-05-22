package se.studieresan.studs

import com.spotify.mobius.Connectable
import com.spotify.mobius.Connection
import com.spotify.mobius.disposables.Disposable
import com.spotify.mobius.functions.Consumer

class ObservableConnectable<I, O>: Connectable<I, O> {

    private val observers: MutableList<Connection<I>> = mutableListOf()
    private var latest: I? = null
    private var output: Consumer<O>? = null

    override fun connect(output: Consumer<O>?): Connection<I> {
        this.output = output
        return object : Connection<I> {
            override fun accept(value: I) = this@ObservableConnectable.accept(value)
            override fun dispose() = this@ObservableConnectable.dispose()
        }
    }

    private fun dispose() =
        synchronized(observers) {
            observers.forEach { it.dispose() }
        }

    private fun accept(value: I) {
        latest = value
        synchronized(observers) {
            observers.forEach { it.accept(value) }
        }
    }

    fun observe(connection: Connection<I>): Disposable {
        synchronized(observers) {
            observers.add(connection)
            latest?.let { connection.accept(it) }
        }
        return Disposable {
            synchronized(observers) {
                connection.dispose()
                observers.remove(connection)
            }
        }
    }

    fun dispatch(value: O) {
        output?.accept(value)
    }

}
