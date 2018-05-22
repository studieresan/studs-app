package se.studieresan.studs.services.backend

import android.content.Context
import android.preference.PreferenceManager
import okhttp3.Interceptor
import okhttp3.Response
import se.studieresan.studs.COOKIES


class ReceivedCookiesInterceptor(val context: Context) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalResponse = chain.proceed(chain.request())

        if (!originalResponse.headers("Set-Cookie").isEmpty()) {
            val cookies = mutableSetOf<String>()

            originalResponse.headers("Set-Cookie").forEach {
                cookies.add(it)
            }

            PreferenceManager
                    .getDefaultSharedPreferences(context)
                    .edit()
                    .putStringSet(COOKIES, cookies)
                    .commit()
        }

        return originalResponse
    }

}
