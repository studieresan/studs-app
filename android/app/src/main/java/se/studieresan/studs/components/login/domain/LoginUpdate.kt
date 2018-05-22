package se.studieresan.studs.components.login.domain

import com.spotify.mobius.Next
import com.spotify.mobius.Next.next

fun update(model: LoginModel, event: Event): Next<LoginModel, Effect> =
    when (event) {

        is Login -> next(
                model.copy(loggingIn = true),
                setOf(RemoteLogin(name = model.username, password = model.password))
        )

        is LoggedIn
                -> next(model.copy(loggingIn = false, loginError = event.status))

        is UsernameChanged
                -> next(model.copy(username = event.username, loginError = None))

        is PasswordChanged
                -> next(model.copy(password = event.password, loginError = None))
    }
