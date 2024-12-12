package dev.gmarques.compras.data.data.repository

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import dev.gmarques.compras.R

/**
 * Encapsula as chamadas ao Firebase Auth
 * */
object UserRepository {

    fun getUser() = FirebaseAuth.getInstance().currentUser

    fun logOut(activity: Context, onResult: (error: Exception?) -> Unit) {

        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(activity.getString(R.string.Google_auth_web_client_id))
            .requestEmail()
            .build()

        GoogleSignIn.getClient(activity, signInOptions)
            .signOut()
            .addOnSuccessListener {
                FirebaseAuth.getInstance().signOut()
                onResult(null)
            }
            .addOnFailureListener {
                onResult(it)
            }
    }
}