package se.studieresan.studs

import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentManager
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import se.studieresan.studs.models.Location

class ShareLocationFragment: DialogFragment(), View.OnClickListener {
    var selectedCategory: ImageButton? = null
    val defaultColor: ColorStateList by lazy {
        ColorStateList.valueOf(Color.parseColor("#FFFFFF"))
    }
    val selectedColor: ColorStateList by lazy {
        ColorStateList.valueOf(ContextCompat.getColor(context, R.color.colorAccent))
    }

    var category: String = "activity"
    fun selectCategory(id: Int) {
        category = when (id) {
            R.id.eat -> "eat"
            R.id.drink -> "drink"
            R.id.shopping -> "shopping"
            else -> "activity"
        }
    }

    override fun onClick(v: View?) {
        v  ?: return
        val selected = v as ImageButton
        selectedCategory?.imageTintList = defaultColor
        selected.imageTintList = selectedColor
        selectedCategory = selected
        selectCategory(selected.id)
    }

    companion object {
        val TAG = "${ShareLocationFragment::class.java.simpleName}_tag"
    }
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater?.inflate(R.layout.fragment_share_location, container, false) ?: return null

        val messageView = v.findViewById(R.id.message) as EditText

        selectedCategory = v.findViewById(R.id.activity) as ImageButton
        selectedCategory?.setOnClickListener(this)
        v.findViewById(R.id.eat).setOnClickListener(this)
        v.findViewById(R.id.drink).setOnClickListener(this)
        v.findViewById(R.id.shopping).setOnClickListener(this)

        v.findViewById(R.id.share_button).setOnClickListener {
            val message = messageView.text.toString().trim()

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
                    user = user?.uid,
                    category = category,
                    timestamp = System.currentTimeMillis()/1000
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
