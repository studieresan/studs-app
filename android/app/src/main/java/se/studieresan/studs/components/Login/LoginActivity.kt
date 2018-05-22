package se.studieresan.studs.components.login

import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import com.spotify.mobius.Connection
import com.spotify.mobius.Mobius
import com.spotify.mobius.MobiusLoop
import com.spotify.mobius.android.MobiusAndroid
import com.spotify.mobius.functions.Consumer
import kotlinx.android.synthetic.main.activity_login.*
import se.studieresan.studs.*
import se.studieresan.studs.components.login.domain.*

class LoginActivity : AppCompatActivity() {

    private var controller: MobiusLoop.Controller<LoginModel, Event>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme_Launcher)
        super.onCreate(savedInstanceState)

        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        if (preferences.getBoolean(LOGGED_IN, false)) {
            transitionToMain()
        } else {
            setContentView(R.layout.activity_login)
            val service = (application as Application).backendService
            val loopFactory = Mobius.loop(::update, effectHandler(service, preferences))
            controller = MobiusAndroid.controller(loopFactory, INITIAL_STATE)
            controller?.connect { connectViews(it) }
        }
    }

    override fun onStart() {
        super.onStart()
        controller?.start()
    }

    private fun connectViews(eventConsumer: Consumer<Event>): Connection<LoginModel> {
        val dispatch = eventConsumer::accept

        login_button.setOnClickListener { dispatch(Login) }
        val usernameWatcher = username_edittext.onTextChange { dispatch(UsernameChanged(it)) }
        val passwordWatcher = password_edittext.onTextChange { dispatch(PasswordChanged(it)) }

        return object : Connection<LoginModel> {
            override fun accept(model: LoginModel) = render(model)

            override fun dispose() {
                login_button.setOnClickListener(null)
                username_edittext.removeTextChangedListener(usernameWatcher)
                password_edittext.removeTextChangedListener(passwordWatcher)
            }
        }
    }

    private fun render(model: LoginModel = INITIAL_STATE) {
        login_button.isEnabled =
                model.username.isNotBlank() and
                model.password.isNotBlank() and
                (model.loginError !is Error)

        progress.show(model.loggingIn)
        login_button.show(!model.loggingIn)

        val loginError = model.loginError
        login_error_textview.show(loginError is Error)
        when (loginError) {
            is Error -> login_error_textview.text = loginError.message
            is Success -> transitionToMain()
        }
    }

    override fun onStop() {
        super.onStop()
        controller?.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        controller?.disconnect()
    }

    private fun transitionToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

}
