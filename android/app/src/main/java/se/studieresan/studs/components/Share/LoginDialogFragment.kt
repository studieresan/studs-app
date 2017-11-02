package se.studieresan.studs.components.Share
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentManager
import android.view.LayoutInflater
import android.view.ViewGroup
import se.studieresan.studs.R
import se.studieresan.studs.extensions.GoogleAuthAPI

class LoginDialogFragment(val googleAuthAPI: GoogleAuthAPI): DialogFragment() {

    companion object {
        val TAG = "${LoginDialogFragment::class.java.simpleName}_tag"
        val RC_SIGN_IN = 5001
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): android.view.View? {
        val v = inflater?.inflate(R.layout.fragment_login, container, false) ?: return null
        v.findViewById(R.id.google_login).setOnClickListener {
            googleAuthAPI.signin()
        }
        return v
    }

    fun display(fragmentManager: FragmentManager) = with(fragmentManager) {
        val ft = beginTransaction()
        val prev = findFragmentByTag(LoginDialogFragment.Companion.TAG)
        if (prev != null) {
            ft.remove(prev)
        }
        ft.addToBackStack(null)
        show(ft, LoginDialogFragment.Companion.TAG)
    }
}
