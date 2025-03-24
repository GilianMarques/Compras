package dev.gmarques.compras.ui.splash

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import dev.gmarques.compras.ui.MyActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dev.gmarques.compras.App
import dev.gmarques.compras.R
import dev.gmarques.compras.data.firestore.Firestore
import dev.gmarques.compras.data.firestore.migration.Migration_1_2
import dev.gmarques.compras.data.model.Product
import dev.gmarques.compras.data.model.SyncAccount
import dev.gmarques.compras.data.repository.ProductRepository
import dev.gmarques.compras.data.repository.UserRepository
import dev.gmarques.compras.databinding.ActivitySplashBinding
import dev.gmarques.compras.ui.Vibrator
import dev.gmarques.compras.ui.login.LoginActivity
import dev.gmarques.compras.ui.main.MainActivity
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext


@SuppressLint("CustomSplashScreen")
class SplashActivity: MyActivity() {

    // usado para fazer uma copia dos dados do anfitrao no momento da desconexao
    private var hostEmail: String? = null
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

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.root.postDelayed({ checkIfUserIsAuthenticated() }, 1)


    }

    private fun checkIfUserIsAuthenticated() {
        if (UserRepository.getUser() == null) loginUser()
        else setupDatabase()
    }

    /**
     * inicializa o banco de dados e faz verificações relacionadas antes de prosseguir com o boot do app
     */
    private fun setupDatabase() = lifecycleScope.launch(IO) {

        hostEmail = Firestore.setupDatabaseHost()
        val localKicked = Firestore.wasLocalUserDisconnectedFromHost()


        if (localKicked) withContext(Main) {
            Vibrator.error()
            binding.pb.visibility = INVISIBLE
            showDialogConfirmIfCloneDataBeforeDisconnectFromHost()
        } else {
            App.getContext().toggleGuestListener(Firestore.amIaGuest)
            openApp()
        }

    }

    private fun showDialogConfirmIfCloneDataBeforeDisconnectFromHost() {
        val title = getString(R.string.Sincronismo_entre_contas_interrompido)
        val msg = getString(R.string.Voce_gostaria_de_manter_os_dados_atuais_na_sua_conta, hostEmail)

        MaterialAlertDialogBuilder(this@SplashActivity).setTitle(title).setMessage(msg)
            .setPositiveButton(getString(R.string.Manter_dados_atuais)) { dialog, _ ->
                dialog.dismiss()
                showDialogConfirmToKeepDeviceOnWhileCloningData()

            }
            .setNegativeButton(getString(R.string.Ficar_com_dados_antigos)) { dialog, _ ->
                dialog.dismiss()
                disconnectFromHost(false)
            }
            .setCancelable(false)
            .show()
    }

    private fun showDialogConfirmToKeepDeviceOnWhileCloningData() {

        val title = getString(R.string.Atencao)
        val msg = getString(R.string.Nao_feche_o_app_ou_se_desconecte_da_internet)

        MaterialAlertDialogBuilder(this@SplashActivity).setTitle(title).setMessage(msg)
            .setPositiveButton(getString(R.string.Entendi)) { dialog, _ ->
                dialog.dismiss()
                binding.pb.visibility = VISIBLE
                disconnectFromHost(true)

            }.setNegativeButton(getString(R.string.Cancelar)) { dialog, _ ->
                dialog.dismiss()
                App.close(this@SplashActivity)
            }.setCancelable(false).show()

    }

    private fun disconnectFromHost(cloneData: Boolean) = lifecycleScope.launch(IO) {
        updateUi(getString(R.string.Por_favor_aguarde))

        val result = UserRepository.disconnectFromHost(SyncAccount("_", hostEmail!!, "_"), cloneData)

        if (result.isSuccess) {
            openApp()
            Vibrator.success()
        } else {
            Vibrator.error()
            Snackbar.make(
                binding.root, getString(R.string.Algo_deu_errado_tente_novamente_mais_tarde), Snackbar.LENGTH_LONG
            ).show()
            Log.d("USUK", "SplashActivity.cloneDatabase: ${result.exceptionOrNull()}")
        }
    }

    private fun openApp() = lifecycleScope.launch {
        UserRepository.updateLastAccessInfo()
        startActivity(Intent(applicationContext, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        this@SplashActivity.finishAffinity()
    }

    private fun loginUser() {
        startActivity(Intent(applicationContext, LoginActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        this@SplashActivity.finishAffinity()
    }

    private suspend fun updateUi(msg: String) = withContext(Main) {
        binding.tvInfo.visibility = VISIBLE
        binding.tvInfo.text = msg

        Vibrator.interaction()
    }

}



