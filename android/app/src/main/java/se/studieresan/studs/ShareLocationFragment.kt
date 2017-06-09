package se.studieresan.studs

import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentManager
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import se.studieresan.studs.models.Location

class ShareLocationFragment: DialogFragment() {
    companion object {
        val TAG = "${ShareLocationFragment::class.java.simpleName}_tag"
    }
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater?.inflate(R.layout.fragment_share_location, container, false) ?: return null

        val messageView = v.findViewById(R.id.message) as EditText

        v.findViewById(R.id.share_button).setOnClickListener {
            val message = messageView.text.toString().trim()
            if (message.isBlank()) return@setOnClickListener
            
            val db = FirebaseDatabase.getInstance().getReference("locations")

            val fineLocation = android.Manifest.permission.ACCESS_FINE_LOCATION
            val ctx = context as MainActivity
            if (ContextCompat.checkSelfPermission(ctx, fineLocation)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(ctx, arrayOf(fineLocation), ctx.PERMISSIONS_REQUEST_IGNORE)
                return@setOnClickListener
            }

            val user = FirebaseAuth.getInstance().currentUser
            val location = LocationServices.FusedLocationApi
                    .getLastLocation(ctx.googleApi)
            val data = Location(
                    lat = location.latitude,
                    lng = location.longitude,
                    message = message,
                    user = user?.uid
            )
            db.push().setValue(data)
            dismiss()
        }

        return v
    }

    fun display(fragmentManager: FragmentManager) = with(fragmentManager) {
        val ft = beginTransaction()
        val prev = findFragmentByTag(TAG)
        if (prev != null) {
            ft.remove(prev)
        }
        ft.addToBackStack(null)
        show(ft, TAG)
    }
}
