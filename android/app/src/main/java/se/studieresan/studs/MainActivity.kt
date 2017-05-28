package se.studieresan.studs

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.FirebaseDatabase
import se.studieresan.studs.extensions.FirebaseAPI

class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    override fun onMapReady(googleMap: GoogleMap?) {
        Log.d("MAIN", "map ready")
        var first = true
        locationListener = FirebaseAPI.createChildEventListener({ snap ->
            val lat = snap.child("lat").value as Double
            val lng = snap.child("lng").value as Double
            val msg = snap.child("message").value as String
            val location = LatLng(lat, lng)
            googleMap?.apply {
                addMarker(MarkerOptions().position(location).title(msg))
                if (first) {
                    animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15F))
                    first = false
                }
            }
        })
        locations.addChildEventListener(locationListener)
    }

    var locationListener: ChildEventListener? = null

    val map by lazy {
        supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
    }

    val locations by lazy {
        val db = FirebaseDatabase.getInstance()
        db.getReference("locations").limitToLast(20)
//                .orderByKey()
//                .startAt(System.currentTimeMillis().toDouble()/1000-3600)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        map.getMapAsync(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        locationListener?.let {
            locations.removeEventListener(it)
        }
    }
}
