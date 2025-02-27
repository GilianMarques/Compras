package dev.gmarques.compras.ui.profile

import android.app.Activity
import android.app.AlertDialog
import android.view.View
import android.view.View.GONE
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import androidx.lifecycle.LifecycleCoroutineScope
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import dev.gmarques.compras.R
import dev.gmarques.compras.data.model.SyncAccount
import dev.gmarques.compras.data.repository.UserRepository
import dev.gmarques.compras.databinding.BsdSendSyncInviteBinding
import dev.gmarques.compras.ui.Vibrator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BsdSendSyncInvite(
    private val targetActivity: Activity,
    private val lifecycleScope: LifecycleCoroutineScope,
    private val guests: List<SyncAccount>?,
) {

    private var binding = BsdSendSyncInviteBinding.inflate(targetActivity.layoutInflater)
    private val dialog: BottomSheetDialog = BottomSheetDialog(targetActivity)

    init {
        dialog.setContentView(binding.root)

        binding.apply {
            fabConfirm.setOnClickListener {
                validateInput(edtInput.text.toString())
            }
            swMergeData.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) showMergeDataWarningDialog()
            }
        }

    }

    private fun showMergeDataWarningDialog() {

        Vibrator.interaction()

        val title = targetActivity.getString(R.string.Atencao)
        val msg = targetActivity.getString(
            R.string.Marcar_a_opcao_X_fara_com_que_os_dados_da_conta,
            targetActivity.getString(R.string.Mesclar_dados_das_duas_contas)
        )

        AlertDialog.Builder(targetActivity).setTitle(title).setMessage(msg)
            .setPositiveButton(targetActivity.getString(R.string.Entendi)) { dialog, _ ->

            }.setNegativeButton(targetActivity.getString(R.string.Cancelar)) { dialog, _ ->
                binding.swMergeData.isChecked = false
            }
            .setCancelable(false).show()
    }

    private fun validateInput(email: String) = lifecycleScope.launch(Dispatchers.IO) {

        if (email.isEmpty()) {
            showErrorMsg(targetActivity.getString(R.string.O_email_n_o_pode_ficar_em_branco))

        } else if (email == UserRepository.getUser()!!.email) {
            showErrorMsg(targetActivity.getString(R.string.Voce_nao_pode_convidar_a_s_mesmo))

        } else if (!email.contains("@") || !email.contains(".com")) {
            showErrorMsg(targetActivity.getString(R.string.Insira_um_endere_o_v_lido))

        } else if (!UserRepository.checkIfUserExists(email)) {
            showErrorMsg(targetActivity.getString(R.string.Usu_rio_n_o_existe))

        } else if (targetEmailIsAlreadyAGuest(email)) {
            showErrorMsg(targetActivity.getString(R.string.X_ja_esta_sincronizando_com_voce, email))

        } else {
            updateUiStatus(true)
            sendRequest(email)
        }
    }

    private fun targetEmailIsAlreadyAGuest(email: String): Boolean {
        guests?.forEach { if (email == it.email) return true }
        return false
    }

    private suspend fun updateUiStatus(freeze: Boolean) = withContext(Dispatchers.Main) {
        binding.fabConfirm.isEnabled = !freeze
        binding.pb.visibility = if (freeze) VISIBLE else INVISIBLE
    }

    private fun sendRequest(email: String) = lifecycleScope.launch {

        val success = UserRepository.sendSyncInvite(email, binding.swMergeData.isChecked)

        if (success) Vibrator.success() else Vibrator.error()

        val title =
            targetActivity.getString(
                if (success) R.string.Solicita_o_enviada
                else R.string.Erro_ao_enviar_solicitacao
            )
        val msg =
            targetActivity.getString(
                if (success) R.string.Convite_enviado_com_sucesso
                else R.string.Houve_um_erro_ao_enviar_a_solicitacao
            )

        AlertDialog.Builder(targetActivity)
            .setTitle(title)
            .setMessage(msg)
            .setCancelable(false)
            .setPositiveButton(targetActivity.getString(R.string.Entendi)) { dialog, _ ->
                dialog.dismiss()
                binding.root.postDelayed({
                    this@BsdSendSyncInvite.dialog.dismiss()
                }, 250) // 250ms delay
            }
            .show()

    }

    private suspend fun showErrorMsg(msg: String) = withContext(Dispatchers.Main) {
        binding.tvErrorMsg.text = msg
        binding.tvErrorMsg.visibility = VISIBLE

        Vibrator.error()
        withContext(Dispatchers.IO) {
            delay(3000)
            withContext(Dispatchers.Main) { binding.tvErrorMsg.visibility = GONE }
        }
    }

    fun show() {

        dialog.show()
        binding.edtInput.requestFocus()


        val behavior = BottomSheetBehavior.from(binding.root.parent as View)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }


}
