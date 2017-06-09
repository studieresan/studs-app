package se.studieresan.studs

import android.arch.lifecycle.*
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.View
import android.view.ViewGroup import android.widget.ImageButton
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import se.studieresan.studs.LoginDialogFragment.Companion.RC_SIGN_IN
import se.studieresan.studs.models.User
import se.studieresan.studs.ui.SlideupNestedScrollview

class MainActivity : LifecycleActivity(), OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, View.OnClickListener {

    val bottomSheet by lazy {
        findViewById(R.id.slide_up) as SlideupNestedScrollview
    }
    val mapFragment by lazy {
        supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
    }
    val gso by lazy {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
    }
    val googleApi by lazy {
        GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build()
    }
    val auth by lazy {
        FirebaseAuth.getInstance()
    }
    var map: GoogleMap? = null
    var currentUser: FirebaseUser? = null
    var markerMap: Map<String, MarkerOptions> = emptyMap<String, MarkerOptions>()

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == RC_SIGN_IN) {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)

            if (result.isSuccess) {
                result.signInAccount?.let {
                    firebaseAuthWithUser(it)
                }
            } else {
                Log.e(TAG, "${result.status}")
            }
        }
    }

    private fun firebaseAuthWithUser(user: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(user.idToken, null)

        auth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                loginDialog?.dismiss()
                currentUser = auth.currentUser
                val db = FirebaseDatabase.getInstance()
                currentUser?.apply {
                    val user = User(displayName, email, photoUrl?.toString(), uid)
                    db.getReference("users").child(uid).setValue(user)
                }
                ShareLocationFragment().display(supportFragmentManager)
            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.fab_my_location -> showDeviceLocation()
            R.id.fab_share ->
                if (currentUser != null) {
                    ShareLocationFragment().display(supportFragmentManager)
                } else {
                    loginDialog = LoginDialogFragment(googleApi)
                    loginDialog?.display(supportFragmentManager)
                }
        }
    }

    var loginDialog: LoginDialogFragment? = null

    val TAG = MainActivity::class.java.simpleName

    override fun onConnected(p0: Bundle?) {
        Log.d(TAG, "Connected")
    }

    override fun onConnectionSuspended(p0: Int) {
        Log.d(TAG, "Connection suspended")
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        Log.d(TAG, "Connection failed")
    }

    val PERMISSIONS_REQUEST_FINE_LOCATION = 1
    val PERMISSIONS_REQUEST_IGNORE = 2

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

        var firstMarker = true
        val model = ViewModelProviders.of(this).get(StudsViewModel::class.java)
        model.getPosts()?.observe(this, Observer { posts ->
            val map = map ?: return@Observer
            posts?.forEach {
                val location = LatLng(it.lat, it.lng)
                if (!markerMap.containsKey(it.key)) {
                    val marker = MarkerOptions().position(location).title(it.message)
                    markerMap += (it.key to marker)
                    map.addMarker(marker)
                }
                if (firstMarker) {
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15F))
                    firstMarker = false
                }
            }
        })

        model?.getSelectedPost()?.observe(this, Observer { post ->
            post ?: return@Observer
            val latLng = LatLng(post.lat, post.lng)
            map?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15F))
            if (bottomSheet.isAtTop) bottomSheet.obscure()
        })
    }

    fun showDeviceLocation () {
        val fineLocation = android.Manifest.permission.ACCESS_FINE_LOCATION
        if (ContextCompat.checkSelfPermission(this, fineLocation)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(fineLocation), PERMISSIONS_REQUEST_FINE_LOCATION)
            return
        }

        map?.apply {
            isMyLocationEnabled = true
            uiSettings.isMyLocationButtonEnabled = false
            val location = LocationServices.FusedLocationApi
                    .getLastLocation(googleApi)
            val latLng = LatLng(location.latitude, location.longitude)
            animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15F))
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
        googleApi.connect()
        mapFragment.getMapAsync(this)
        findViewById(R.id.fab_my_location).setOnClickListener(this)
        findViewById(R.id.fab_share).setOnClickListener(this)
        currentUser = auth.currentUser
        setupBottomNav()
    }

    private fun setupBottomNav() {
        listOf(R.id.bottom_nav, R.id.top_nav).forEach {
            with (findViewById(it)) {
                findViewById(R.id.nav_1).setOnClickListener { switchFragment(1) }
                findViewById(R.id.nav_2).setOnClickListener { switchFragment(2) }
                findViewById(R.id.nav_3).setOnClickListener { switchFragment(3) }
            }
        }
        switchFragment(2)
    }

    private fun switchFragment(page: Int) {
        slideUp()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        val (fragment, id) = when (page) {
            1 -> DummyFragment() to "dummy1"
            2 -> DummyFragment() to "dummy2"
            else -> OverviewFragment() to "overview"
        }
        fragmentTransaction.setCustomAnimations(R.transition.slide_up, R.transition.out)
        fragmentTransaction.replace(R.id.fragment_container, fragment, id)
        fragmentTransaction.commit()

        listOf(R.id.bottom_nav, R.id.top_nav).forEach { nav ->
            with (findViewById(nav) as ViewGroup) {
                (0..childCount - 1).forEach {
                    if (it == page - 1) {
                        (getChildAt(it) as ImageButton).setColorFilter(resources.getColor(R.color.colorAccent))
                    } else {
                        (getChildAt(it) as ImageButton).setColorFilter(Color.WHITE)
                    }
                }
            }
        }
    }

    private fun slideUp() {
        bottomSheet.preview()
    }

    override fun onBackPressed() {
        if (bottomSheet.isAtTop) {
            bottomSheet.obscure()
        } else {
            super.onBackPressed()
        }
    }

}
