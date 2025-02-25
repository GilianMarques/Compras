package dev.gmarques.compras.ui.splash

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import dev.gmarques.compras.App
import dev.gmarques.compras.R
import dev.gmarques.compras.data.firestore.FirebaseCloneDatabase
import dev.gmarques.compras.data.firestore.Firestore
import dev.gmarques.compras.data.repository.UserRepository
import dev.gmarques.compras.databinding.ActivitySplashBinding
import dev.gmarques.compras.ui.Vibrator
import dev.gmarques.compras.ui.login.LoginActivity
import dev.gmarques.compras.ui.main.MainActivity
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.properties.Delegates


class SplashActivity : AppCompatActivity() {

    private var updateUserMetadata by Delegates.notNull<Boolean>()
    private lateinit var binding: ActivitySplashBinding

    companion object {
        private const val UPDATE_USER_METADATA = "update_user_metadata"

        fun newIntentUpdateMetadata(context: Context): Intent {
            return Intent(context, SplashActivity::class.java).apply {
                putExtra(UPDATE_USER_METADATA, true).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        updateUserMetadata = intent.getBooleanExtra(UPDATE_USER_METADATA, false)

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.root.postDelayed({ checkUserAuthenticated() }, 10)

    }

    private fun checkUserAuthenticated() {
        if (UserRepository.getUser() == null) loginUser()
        else setupDatabase()
    }

    private fun setupDatabase() = lifecycleScope.launch(IO) {
        // TODO: tratar o retorno dessa forma nao ta legal, refatora isso
        val hostEmail = Firestore.loadDatabasePaths()

        if (hostEmail != null) withContext(Main) {
            binding.progressBar2.visibility = INVISIBLE
            showDialogConfirmBeginMigration(hostEmail)
        }
        else {
            if (updateUserMetadata) UserRepository.updateDatabaseMetadata()
            openApp()
        }
    }

    private fun showDialogConfirmBeginMigration(hostEmail: String) {

        AlertDialog.Builder(this).setTitle(getString(R.string.Sincronismo_entre_contas))
            .setMessage(getString(R.string.O_sincronismo_entre_contas_foi_interrompido))
            .setCancelable(false).setPositiveButton(getString(R.string.Entendi)) { _, _ ->
                binding.progressBar2.visibility = VISIBLE
                lifecycleScope.launch(IO) { cloneDatabase(hostEmail) }
            }.setNegativeButton(getString(R.string.Sair)) { _, _ ->
                App.close(this)
            }.show()
    }

    private suspend fun cloneDatabase(hostEmail: String) {
        updateUi(getString(R.string.Aguarde_enquanto_seus_dados_s_o_migrados_de_volta_n_o_feche_o_app_ou_se_desconecte_da_internet))
        FirebaseCloneDatabase(hostEmail, UserRepository.getUser()!!.email!!).beginCloning()
        openApp()
    }

    private fun openApp() {
        startActivity(
            Intent(
                applicationContext, MainActivity::class.java
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
        this@SplashActivity.finishAffinity()
    }

    private fun loginUser() {
        startActivity(
            Intent(
                applicationContext, LoginActivity::class.java
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
        this@SplashActivity.finishAffinity()
    }


    private suspend fun updateUi(msg: String) = withContext(Main) {
        binding.tvInfo.visibility = VISIBLE
        binding.tvInfo.text = msg

        Vibrator.interaction()
    }

}



