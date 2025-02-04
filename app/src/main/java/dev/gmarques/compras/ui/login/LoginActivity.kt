package dev.gmarques.compras.ui.login

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import dev.gmarques.compras.R
import dev.gmarques.compras.data.repository.UserRepository
import dev.gmarques.compras.databinding.ActivityLoginBinding
import dev.gmarques.compras.domain.utils.InternetConnectionChecker
import dev.gmarques.compras.ui.main.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.MessageFormat

// TODO: usa api credentials 
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    companion object {
        private const val REQ_LOGIN_CODE: Int = 123
    }

    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(this.binding.root)

        checkInternetConnection()
        initTryAgainFab()
    }

    private fun initTryAgainFab() {
        binding.btnTentarNovamente.setOnClickListener {

            binding.progressBar.visibility = View.VISIBLE
            binding.btnTentarNovamente.visibility = View.GONE
            binding.tvInfo.text = ""
            checkInternetConnection()
        }
    }

    private fun checkInternetConnection() {
        InternetConnectionChecker().checkConnection { connected ->
            if (connected) {
                initObjects()
                doLogin()
            } else {
                binding.progressBar.visibility = View.GONE
                binding.btnTentarNovamente.visibility = View.VISIBLE

                binding.tvInfo.setText(R.string.Vocenaoestaconectadoainternetouadata)
            }
        }
    }

    private fun initObjects() {
        mAuth = FirebaseAuth.getInstance()

        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.Google_auth_web_client_id)).requestEmail().build()


        mGoogleSignInClient = GoogleSignIn.getClient(this, signInOptions)
    }

    /**
     * a execuçao continua a partir do onActivityResult
     */
    private fun doLogin() {
        val signInIntent: Intent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, REQ_LOGIN_CODE)
    }

    @Deprecated("")
    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        try {
            val account = task.getResult(ApiException::class.java)
            firebaseAuthWithGoogle(account)
        } catch (e: ApiException) {
            this.binding.progressBar.visibility = View.GONE
            this.binding.btnTentarNovamente.visibility = View.VISIBLE

            when (e.statusCode) {
                GoogleSignInStatusCodes.SIGN_IN_CANCELLED -> {
                    this.binding.tvInfo.setText(R.string.Voce_cancelou_o_login)
                }

                GoogleSignInStatusCodes.SIGN_IN_CURRENTLY_IN_PROGRESS -> {
                    this.binding.tvInfo.setText(R.string.Hamaisdeumprocessodeloginemandamentoquantas)
                }

                GoogleSignInStatusCodes.SIGN_IN_FAILED -> {
                    this.binding.tvInfo.text =
                        MessageFormat.format(getString(R.string.Ologinfalhou), e.statusCode)
                }

                else -> {
                    this.binding.tvInfo.text = MessageFormat.format(
                        getString(R.string.Houveumerroaocontactaraapidogoole), e.statusCode
                    )
                }
            }
        }
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        mAuth.signInWithCredential(credential).addOnCompleteListener(
            this
        ) { task ->
            if (task.isSuccessful) {
                val user: FirebaseUser = checkNotNull(mAuth.currentUser)
                this.binding.progressBar.visibility = View.GONE
                val nome = if (user.displayName != null) user.displayName else "?"
                this.binding.tvInfo.text = MessageFormat.format(getString(R.string.BemvindoX),
                    nome!!.split(" ".toRegex()).dropLastWhile { it.isEmpty() }
                        .toTypedArray()[0])

                //deve rodar mesmo setivity fechar
                CoroutineScope(Dispatchers.IO).launch { UserRepository.initUserDatabase() }

                Handler().postDelayed({
                    startActivity(
                        Intent(applicationContext, MainActivity::class.java).addFlags(
                            Intent.FLAG_ACTIVITY_NEW_TASK
                        )
                    )
                    finishAffinity()
                }, 2000)
            } else {
                this.binding.progressBar.visibility = View.GONE
                this.binding.btnTentarNovamente.visibility = View.VISIBLE
                val ex = task.exception
                if (ex != null) this.binding.tvInfo.text = MessageFormat.format(
                    getString(R.string.Houveumerroaoautenticarsuacontatente), ex.message
                )
            }
        }
    }

}

