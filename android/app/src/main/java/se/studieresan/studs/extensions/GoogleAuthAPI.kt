package se.studieresan.studs.extensions

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.util.Log
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.Auth.GoogleSignInApi
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import se.studieresan.studs.R
import se.studieresan.studs.components.Share.LoginDialogFragment
import se.studieresan.studs.components.Share.LoginDialogFragment.Companion.RC_SIGN_IN

/**
 * Wrapper for google authentication
 */
class GoogleAuthAPI(val context: FragmentActivity) :
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    val TAG = GoogleAuthAPI::class.java.simpleName!!
    val auth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }
    val gso: GoogleSignInOptions by lazy {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
    }
    val googleApi: GoogleApiClient by lazy {
        val api = GoogleApiClient.Builder(context)
                .enableAutoManage(context, this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build()
        api.connect()
        api
    }

    override fun onConnected(p0: Bundle?) {
        Log.d(TAG, "Connected")
    }

    override fun onConnectionSuspended(p0: Int) {
        Log.d(TAG, "Connection suspended")
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        Log.d(TAG, "Connection failed")
    }

    fun googleSigninResult(requestCode: Int, data: Intent?, onSuccess: (FirebaseUser?) -> Unit) {
        if (requestCode == RC_SIGN_IN) {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)

            if (result.isSuccess) {
                result.signInAccount?.let {
                    firebaseAuthWithUser(it, onSuccess)
                }
            } else {
                Log.e(TAG, "${result.status}")
            }
        }
    }

    fun firebaseAuthWithUser(user: GoogleSignInAccount, onSuccess: (FirebaseUser?) -> Unit) {
        val credential = GoogleAuthProvider.getCredential(user.idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onSuccess(auth.currentUser)
            }
        }
    }

    fun signin() {
        val signInIntent = GoogleSignInApi.getSignInIntent(googleApi)
        context.startActivityForResult(signInIntent, LoginDialogFragment.Companion.RC_SIGN_IN)
    }

}
