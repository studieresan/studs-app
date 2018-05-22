package se.studieresan.studs.components.login.domain

import android.content.SharedPreferences
import android.util.Log
import com.spotify.mobius.Connection
import com.spotify.mobius.functions.Consumer
import io.reactivex.disposables.CompositeDisposable
import se.studieresan.studs.EMAIL
import se.studieresan.studs.LOGGED_IN
import se.studieresan.studs.services.backend.BackendService
import se.studieresan.studs.services.backend.Login


val TAG = "LoginEffectHandler"
val ERROR = "Login Failed. Check your username/password or try again later."

fun effectHandler(backendService: BackendService, sharedPreferences: SharedPreferences): (Consumer<Event>) -> Connection<Effect>  = {
    eventConsumer ->

    object : Connection<Effect> {

        val disposables: CompositeDisposable = CompositeDisposable()

        override fun accept(effect: Effect) {
            val dispatch = eventConsumer::accept
            when (effect) {
                is RemoteLogin -> {
                    val disposable = backendService
                            .login(Login(email = effect.name, password = effect.password))
                            .subscribe(
                                    {
                                        sharedPreferences
                                                .edit()
                                                .putString(EMAIL, effect.name)
                                                .putBoolean(LOGGED_IN, true)
                                                .commit()
                                        dispatch(LoggedIn(Success))
                                    },
                                    { throwable ->
                                        Log.e(TAG, throwable.message)
                                        dispatch(LoggedIn(Error(ERROR)))
                                    })
                    disposables.add(disposable)
                }
            }
        }

        override fun dispose() {
            disposables.clear()
        }
    }
}
