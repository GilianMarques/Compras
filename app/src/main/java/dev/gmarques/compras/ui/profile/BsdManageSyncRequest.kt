package dev.gmarques.compras.ui.profile

import android.app.Activity
import android.app.AlertDialog
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.core.text.htmlEncode
import androidx.lifecycle.LifecycleCoroutineScope
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import dev.gmarques.compras.R
import dev.gmarques.compras.data.model.SyncRequest
import dev.gmarques.compras.data.repository.UserRepository
import dev.gmarques.compras.databinding.BsdManageSyncRequestBinding
import dev.gmarques.compras.domain.utils.ExtFun.Companion.formatHtml
import dev.gmarques.compras.ui.Vibrator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BsdManageSyncRequest(
    val request: SyncRequest,
    private val targetActivity: Activity,
    private val lifecycleScope: LifecycleCoroutineScope,
) {

    private var binding = BsdManageSyncRequestBinding.inflate(targetActivity.layoutInflater)
    private val dialog: BottomSheetDialog = BottomSheetDialog(targetActivity)


    init {
        dialog.setContentView(binding.root)

        binding.apply {

            fabAccept.setOnClickListener {
            }

            fabDecline.setOnClickListener {
            }

            tvUserName.text = request.name
            tvEmail.text = request.email
            tvInfo.text = targetActivity.getString(
                R.string.X_te_enviou_uma_solicita_o_de_sincronismo_ao_aceitar_a_solicita_o_os_dados_de_ambas_as_contas_ser_o_mesclados_e_as_altera_es_feitas_por_um_usu_rio_ser_o_refletidas_na_conta_do_outro_n_nn_o_aceite_solicita_es_de_pessoas_desconhecidas,
                request.name
            )

            Glide.with(root.context)
                .load(request.photoUrl)
                .circleCrop()
                .placeholder(R.drawable.vec_invite_user)
                .into(ivProfilePicture)

        }

    }


    private fun sendRequest(email: String) = lifecycleScope.launch {

        val success = UserRepository.sendSyncRequest(email)

        val title =
            targetActivity.getString(
                if (success) R.string.Solicita_o_enviada
                else R.string.Erro_ao_enviar_solicitacao
            )
        val msg =
            targetActivity.getString(
                if (success) R.string.Reinicie_o_app_ap_s_a_solicita_o_ser_aceita
                else R.string.Houve_um_erro_ao_enviar_a_solicitacao
            )

        AlertDialog.Builder(targetActivity)
            .setTitle(title)
            .setMessage(msg)
            .setPositiveButton(targetActivity.getString(R.string.Entendi)) { dialog, _ ->
                dialog.dismiss()
                binding.root.postDelayed({
                    this@BsdManageSyncRequest.dialog.dismiss()
                }, 500) // 500ms delay
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

        val behavior = BottomSheetBehavior.from(binding.root.parent as View)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }


}
