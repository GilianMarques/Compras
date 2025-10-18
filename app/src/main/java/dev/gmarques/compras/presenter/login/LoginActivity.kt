package dev.gmarques.compras.presenter.login

import android.os.Bundle
import android.view.View
import dev.gmarques.compras.presenter.MyActivity
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes.DEVELOPER_ERROR
import com.firebase.ui.auth.ErrorCodes.NO_NETWORK
import com.firebase.ui.auth.ErrorCodes.PROVIDER_ERROR
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.IdpResponse
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import dev.gmarques.compras.R
import dev.gmarques.compras.databinding.ActivityLoginBinding
import dev.gmarques.compras.presenter.Vibrator
import dev.gmarques.compras.presenter.splash.SplashActivity
import java.text.MessageFormat

class LoginActivity: MyActivity() {

    private lateinit var binding: ActivityLoginBinding

    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract(),
    ) { res -> this.onSignInResult(res) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(this.binding.root)

        setupFabTryAgain()
        doLogin()
    }

    private fun setupFabTryAgain() {
        binding.fabTryAgain.setOnClickListener {

            binding.fabTryAgain.visibility = View.GONE
            binding.tvInfo.text = ""
            doLogin()
        }
    }

    /**
     * a execuçao continua a partir do onSignInResult
     * @see onSignInResult
     */
    private fun doLogin() {

        val providers = arrayListOf(AuthUI.IdpConfig.GoogleBuilder().build())

        val signInIntent =
            AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(providers)
              //  .setLogo(R.drawable.vec_product) // Set logo drawable
              //  .setTheme(R.style.Login_Activity) // Set theme
                .build()

        signInLauncher.launch(signInIntent)
    }

    /**
     * Retorna aqui após o ususario concluir o fluxo de autenticação do firebase
     */
    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        val response = result.idpResponse
        if (result.resultCode == RESULT_OK) {
            signInSuccess(FirebaseAuth.getInstance().currentUser)
        } else handleSignInErrors(response)

    }

    private fun signInSuccess(user: FirebaseUser?) {
        Vibrator.success()

        val nome = if (user?.displayName != null) user.displayName else "?"
        binding.tvInfo.text = getString(R.string.BemvindoX, nome!!.split(" ")[0]).ifBlank { nome }

        startActivity(SplashActivity.newIntentUpdateMetadata(this@LoginActivity))
        finishAffinity()

    }

    /**
    Sign in failed. If response is null the user canceled the sign-in flow using the back button. Otherwise check
    response.getError().getErrorCode() and handle the error.
     */
    private fun handleSignInErrors(response: IdpResponse?) {
        Vibrator.error()
        this.binding.fabTryAgain.visibility = View.VISIBLE

        if (response == null) this.binding.tvInfo.setText(R.string.Voce_cancelou_o_login)
        else when (response.error?.errorCode) {

            NO_NETWORK -> {
                this.binding.tvInfo.setText(R.string.Vocenaoestaconectadoainternetouadata)
            }

            DEVELOPER_ERROR -> {
                this.binding.tvInfo.text = getString(
                    R.string.O_login_falhou_por_um_erro_de_desenvolvimento_da_aplica_o_contate_o_desenvolvedor_c_digo_de_erro_1_mensagem_2,
                    response.error!!.errorCode,
                    response.error!!.message!!
                )
            }

            PROVIDER_ERROR -> {
                this.binding.tvInfo.text = getString(
                    R.string.O_login_falhou_por_um_erro_no_provedor_de_login,
                    response.error!!.errorCode,
                    response.error!!.message!!
                )
            }

            else -> {
                this.binding.tvInfo.text = MessageFormat.format(
                    getString(R.string.Ologinfalhou),
                    response.error!!.errorCode,
                    response.error!!.message!!
                )
            }
        }
    }


}
