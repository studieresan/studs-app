package se.studieresan.studs

import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.FirebaseDatabase
import se.studieresan.studs.extensions.FirebaseAPI

class MainActivity : AppCompatActivity(), OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, View.OnClickListener {
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.fab_my_location -> showDeviceLocation()
            R.id.fab_share -> ShareLocationFragment().display(supportFragmentManager)
        }
    }

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

    val googleApi by lazy {
        GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build()
    }

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
    }

    override fun onDestroy() {
        super.onDestroy()
        locationListener?.let {
            locations.removeEventListener(it)
        }
    }
}
