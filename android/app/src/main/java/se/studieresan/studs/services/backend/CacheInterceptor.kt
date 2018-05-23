package se.studieresan.studs.services.backend

import android.content.Context
import android.net.ConnectivityManager
import okhttp3.Interceptor


fun cacheInterceptor(context: Context) = Interceptor { chain ->
    val isOnline: Boolean = {
        val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        connectivityManager?.activeNetworkInfo?.isConnectedOrConnecting ?: false
    }()

    val response = chain.request()

    if (!isOnline) {
        val maxStale = 60 * 60 * 24 * 7 * 8; // tolerate 4-weeks stale
        val request = response.newBuilder()
                .header("Cache-Control", "public, only-if-cached, max-stale=" + maxStale)
                .build()
        val response = chain.proceed(request)
        response
    } else {
        chain.proceed(response)
    }
}
