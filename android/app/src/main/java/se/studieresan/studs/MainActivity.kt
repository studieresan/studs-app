package se.studieresan.studs

import android.arch.lifecycle.LifecycleActivity
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import se.studieresan.studs.components.Posts.OverviewFragment
import se.studieresan.studs.components.Share.LoginDialogFragment
import se.studieresan.studs.components.Share.ShareLocationFragment
import se.studieresan.studs.components.Todos.TodoFragment
import se.studieresan.studs.components.TravelInfo.InfoFragment
import se.studieresan.studs.extensions.GoogleAuthAPI
import se.studieresan.studs.extensions.observeNotNull
import se.studieresan.studs.models.User
import se.studieresan.studs.ui.SlideupNestedScrollview

class MainActivity : LifecycleActivity(), OnMapReadyCallback, View.OnClickListener {

    companion object {
        val FINE_LOCATION = android.Manifest.permission.ACCESS_FINE_LOCATION
        val TAG = MainActivity::class.java.simpleName!!
    }
    val PERMISSIONS_REQUEST_FINE_LOCATION = 1
    val PERMISSIONS_REQUEST_IGNORE = 2
    val bottomSheet by lazy {
        findViewById(R.id.slide_up) as SlideupNestedScrollview
    }
    val mapFragment by lazy {
        supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
    }
    var map: GoogleMap? = null
    var currentUser: FirebaseUser? = null
    var todoMarkerMap: Map<String, Marker> = emptyMap()
    var postMarkerMap: Map<String, Marker> = emptyMap()
    var displayLocation: String? = null
    var loginFragment: LoginDialogFragment? = null
    val model: StudsViewModel by lazy {
        ViewModelProviders.of(this).get(StudsViewModel::class.java)
    }
    val googleAuthApi = GoogleAuthAPI(this)

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        googleAuthApi.googleSigninResult(requestCode, data) {
            val db = FirebaseDatabase.getInstance()
            currentUser = it
            currentUser?.apply {
                val user = User(displayName, email, photoUrl?.toString(), uid)
                db.getReference("users").child(uid).setValue(user)
                loginFragment?.dismiss()
            }
            ShareLocationFragment().display(supportFragmentManager)
        }
    }
    override fun onMapReady(googleMap: GoogleMap?) {
        Log.d(TAG, "mapFragment ready")
        map = googleMap

        var firstTouch = true
        map?.setOnMarkerClickListener {
            if (firstTouch) {
                val width = findViewById(R.id.fab_share).width * 1.25f
                map?.setPadding(0, 0, width.toInt(), 0)
                firstTouch = false
            }
            false
        }

        getPosts()
        getTodos()
    }

    fun getTodos() {
        model.getTodos()?.observeNotNull(this) { todos ->
            val map = map ?: return@observeNotNull
            todos.filterNotNull().forEach {
                val location = LatLng(it.latitude, it.longitude)
                if (!todoMarkerMap.containsKey(it.name)) {
                    val marker = map.addMarker(
                            MarkerOptions()
                                    .position(location)
                                    .title(it.name))
                    todoMarkerMap += (it.name to marker)
                }
            }
        }
    }

    fun getPosts() {
        model.getPosts()?.observeNotNull(this) { posts ->
            val map = map ?: return@observeNotNull
            posts.forEach {
                val location = LatLng(it.lat, it.lng)
                if (!postMarkerMap.containsKey(it.key)) {
                    val marker = map.addMarker(
                            MarkerOptions()
                                    .position(location)
                                    .title(it.getDescriptionForCategory())
                                    .snippet(it.getTimeAgo()))
                    postMarkerMap += it.key to marker

                    if (displayLocation != null && it.key == displayLocation) {
                        animateToMarker(location, it.key, postMarkerMap)
                        marker.showInfoWindow()
                        displayLocation = null
                    }
                }
            }
        }
    }

    fun showDeviceLocation () {
        if (ContextCompat.checkSelfPermission(this, FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(FINE_LOCATION), PERMISSIONS_REQUEST_FINE_LOCATION)
            return
        }

        map?.apply {
            isMyLocationEnabled = true
            uiSettings.isMyLocationButtonEnabled = false
            val location = LocationServices.FusedLocationApi
                    .getLastLocation(googleAuthApi.googleApi)
            location?.let {
                val latLng = LatLng(location.latitude, location.longitude)
                animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15F))
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSIONS_REQUEST_FINE_LOCATION ->
                if (grantResults.isNotEmpty() && grantResults.first() == PackageManager.PERMISSION_GRANTED) {
                    showDeviceLocation()
                }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        FirebaseMessaging.getInstance().subscribeToTopic("locations")

        findViewById(R.id.fab_my_location).setOnClickListener(this)
        findViewById(R.id.fab_share).setOnClickListener(this)
        setupBottomNav()

        model.getSelectedPost().observeNotNull(this) { (lat, lng, _, key) ->
            animateToMarker(LatLng(lat, lng), key, postMarkerMap)
            model.unselectPost()
        }
        model.getSelectedTodo().observeNotNull(this) { todo ->
            animateToMarker(LatLng(todo.latitude, todo.longitude), todo.name, todoMarkerMap)
            model.unselectTodo()
        }
        mapFragment.getMapAsync(this)

        currentUser = FirebaseAuth.getInstance().currentUser

        if (intent.hasExtra("locationKey")) {
            displayLocation = intent.extras["locationKey"] as String
        }
    }

    private fun setupBottomNav() {
        listOf(R.id.bottom_nav, R.id.top_nav).forEach {
            with (findViewById(it)) {
                findViewById(R.id.nav_1).setOnClickListener {
                    switchFragment(1)
                    todoMarkerMap.values.forEach { it.isVisible = false }
                    postMarkerMap.values.forEach { it.isVisible = true }
                }
                findViewById(R.id.nav_2).setOnClickListener {
                    switchFragment(2)
                }
                findViewById(R.id.nav_3).setOnClickListener {
                    switchFragment(3)
                    postMarkerMap.values.forEach { it.isVisible = false }
                    todoMarkerMap.values.forEach { it.isVisible = true }
                }
            }
        }
        switchFragment(1)
    }

    private fun switchFragment(page: Int) {
        slideUp()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        val (fragment, id) = when (page) {
            1 -> OverviewFragment() to "overview"
            2 -> InfoFragment() to "info"
            else -> TodoFragment() to "todo"
        }
//        fragmentTransaction.setCustomAnimations(R.anim.slide_up, R.anim.out)
        fragmentTransaction.replace(R.id.fragment_container, fragment, id)
        fragmentTransaction.commit()

        listOf(R.id.bottom_nav, R.id.top_nav).forEach { nav ->
            with (findViewById(nav) as ViewGroup) {
                (0..childCount - 1).forEach {
                    if (it == page - 1) {
                        val accent = ContextCompat.getColor(this@MainActivity, R.color.colorAccent)
                        (getChildAt(it) as ImageButton).setColorFilter(accent)
                    } else {
                        (getChildAt(it) as ImageButton).setColorFilter(Color.WHITE)
                    }
                }
            }
        }
    }

    override fun onBackPressed() =
        if (bottomSheet.isAtTop) {
            bottomSheet.obscure()
        } else {
            super.onBackPressed()
        }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.fab_my_location -> showDeviceLocation()
            R.id.fab_share ->
                if (currentUser != null) {
                    ShareLocationFragment().display(supportFragmentManager)
                } else {
                    loginFragment = LoginDialogFragment(googleAuthApi)
                    loginFragment?.display(supportFragmentManager)
                }
        }
    }

    private fun animateToMarker(latLng: LatLng, markerKey: String, markerMap: Map<String, Marker>) {
        map?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15F))
        markerMap[markerKey]?.showInfoWindow()
        if (bottomSheet.isAtTop) bottomSheet.obscure()
    }

    private fun slideUp() = bottomSheet.preview()
}
