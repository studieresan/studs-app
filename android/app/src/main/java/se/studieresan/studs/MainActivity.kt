package se.studieresan.studs

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
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
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.FirebaseDatabase
import se.studieresan.studs.LoginDialogFragment.Companion.RC_SIGN_IN
import se.studieresan.studs.extensions.FirebaseAPI
import se.studieresan.studs.ui.SlideupNestedScrollview

class MainActivity : AppCompatActivity(), OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, View.OnClickListener {
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
                    val user = User(displayName, email, photoUrl?.toString())
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
        var first = true
        locationListener = FirebaseAPI.createChildEventListener({ snap ->
            val data = snap.getValue(Location::class.java)
            val location = LatLng(data.lat, data.lng)
            map?.apply {
                addMarker(MarkerOptions().position(location).title(data.message))
                if (first) {
                    animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15F))
                    first = false
                }
            }
        })
        locations.addChildEventListener(locationListener)
    }

    var locationListener: ChildEventListener? = null

    var map: GoogleMap? = null

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

    var currentUser: FirebaseUser? = null

    val locations by lazy {
        val db = FirebaseDatabase.getInstance()
        db.getReference("locations").limitToLast(20)
//                .orderByKey()
//                .startAt(System.currentTimeMillis().toDouble()/1000-3600)
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

    override fun onDestroy() {
        super.onDestroy()
        locationListener?.let {
            locations.removeEventListener(it)
        }
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
            else -> DummyFragment() to "dummy3"
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
        val bottomSheet = findViewById(R.id.slide_up)
        if (bottomSheet is SlideupNestedScrollview) {
            bottomSheet.preview()
        }
    }

    override fun onBackPressed() {
        val bottomSheet = findViewById(R.id.slide_up)
        if (bottomSheet is SlideupNestedScrollview && bottomSheet.isAtTop) {
            bottomSheet.obscure()
        } else {
            super.onBackPressed()
        }
    }
}
