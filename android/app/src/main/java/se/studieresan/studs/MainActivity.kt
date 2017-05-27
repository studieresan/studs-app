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

class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    override fun onMapReady(googleMap: GoogleMap?) {
        Log.d("MAIN", "map ready")
        val timesSquare = LatLng(40.758899, -73.9873197)
        googleMap?.apply {
            addMarker(MarkerOptions().position(timesSquare).title("Party"))
            animateCamera(CameraUpdateFactory.newLatLngZoom(timesSquare, 15F))
        }
    }

    val map by lazy {
        supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        map.getMapAsync(this)
    }
}
