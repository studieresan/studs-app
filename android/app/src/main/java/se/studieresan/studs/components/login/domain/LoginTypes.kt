package se.studieresan.studs.components.login.domain

// MODEL
data class LoginModel(
        val loggingIn: Boolean,
        val loginError: LoginStatus,
        val username: String,
        val password: String
)

val INITIAL_STATE = LoginModel(
        loggingIn = false,
        loginError = None,
        username = "",
        password = ""
)

// EVENTS
sealed class Event
object Login : Event()
data class LoggedIn(val status: LoginStatus): Event()
data class UsernameChanged(val username: String): Event()
data class PasswordChanged(val password: String): Event()

sealed class LoginStatus
object None : LoginStatus()
object Success : LoginStatus()
data class Error(val message: String): LoginStatus()

// EFFECTS
sealed class Effect
data class RemoteLogin(val name: String, val password: String): Effect()
