package se.studieresan.studs

import android.location.Geocoder
import android.preference.PreferenceManager
import com.google.android.gms.location.LocationServices
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import se.studieresan.studs.components.register.domain.RegisterEffectHandler
import se.studieresan.studs.components.share.domain.ShareEffectHandler
import se.studieresan.studs.domain.EffectHandler
import se.studieresan.studs.services.backend.*
import java.util.*


class Application: android.app.Application() {

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }

    private val retrofit by lazy {
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY
        val cacheSize = 5 * 1024 * 1024L // 5 MiB
        val cache = Cache(applicationContext.cacheDir, cacheSize)

        val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(logging)
                .addInterceptor(cacheInterceptor(this))
                .addInterceptor(AddCookiesInterceptor(this))
                .addInterceptor(ReceivedCookiesInterceptor(this))
                .cache(cache)
                .build()

        Retrofit.Builder()
                .baseUrl("https://studs18-overlord.herokuapp.com/")
                .client(okHttpClient)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
    }

    private val firestore: FirebaseFirestore by lazy {
        val firestore = FirebaseFirestore.getInstance()
        val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .setTimestampsInSnapshotsEnabled(true)
                .build()
        firestore.firestoreSettings = settings
        firestore
    }

    private val geocoder: Geocoder by lazy {
        Geocoder(applicationContext, Locale.getDefault())
    }

    private val userSource: UserSource by lazy {
        UserSourceImpl(backendService, PreferenceManager.getDefaultSharedPreferences(this))
    }

    val backendService by lazy {
        retrofit.create(BackendService::class.java)
    }

    val effectHandler: EffectHandler by lazy {
        EffectHandler(
                userSource =  userSource,
                sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this),
                firestore = firestore,
                locationProvider = LocationServices.getFusedLocationProviderClient(this)
        )
    }

    val shareEffectHandler: ShareEffectHandler by lazy {
        ShareEffectHandler(
                userSource = userSource,
                firestore = firestore,
                geocoder = geocoder
        )
    }

    val registerEffectHandler: RegisterEffectHandler by lazy {
        RegisterEffectHandler(
                userSource = userSource,
                firestore = firestore
        )
    }
}
