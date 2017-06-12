package se.studieresan.studs

import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.common.api.GoogleApiClient

class LoginDialogFragment(val googleApiClient: GoogleApiClient): DialogFragment() {
    companion object {
        val TAG = "${LoginDialogFragment::class.java.simpleName}_tag"
        val RC_SIGN_IN = 5001
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater?.inflate(R.layout.fragment_login, container, false) ?: return null
        v.findViewById(R.id.google_login).setOnClickListener {
            val signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient)
            activity.startActivityForResult(signInIntent, RC_SIGN_IN)
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
