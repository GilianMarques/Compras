package dev.gmarques.compras.ui.profile

import android.app.Activity
import android.app.AlertDialog
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.lifecycle.LifecycleCoroutineScope
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import dev.gmarques.compras.R
import dev.gmarques.compras.data.model.SyncAccount
import dev.gmarques.compras.data.repository.UserRepository
import dev.gmarques.compras.databinding.BsdDisconnectAccountBinding
import dev.gmarques.compras.ui.Vibrator
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.system.exitProcess

class BsdDisconnectAccount(
    private val account: SyncAccount,
    private val targetActivity: Activity,
    private val lifecycleScope: LifecycleCoroutineScope,
    private val accountIsHost: Boolean,
) {

    private var binding = BsdDisconnectAccountBinding.inflate(targetActivity.layoutInflater)
    private val dialog: BottomSheetDialog = BottomSheetDialog(targetActivity)


    init {
        dialog.setContentView(binding.root)

        binding.apply {

            fabDisconnect.setOnClickListener {
                Vibrator.interaction()
                confirmDisconnect()
            }

            tvUserName.text = account.name
            tvEmail.text = account.email
            if (accountIsHost) tvTitle.text = targetActivity.getString(R.string.Gerenciar_anfitriao)

            Glide.with(root.context).load(account.photoUrl).circleCrop()
                .placeholder(R.drawable.vec_invite_user).into(ivProfilePicture)

        }

    }

    private fun confirmDisconnect() {

        val title = targetActivity.getString(R.string.Por_favor_confirme)
        val msg = targetActivity.getString(R.string.Deseja_mesmo_interromper_a_conexao)

        AlertDialog.Builder(targetActivity).setTitle(title).setMessage(msg)
            .setPositiveButton(targetActivity.getString(R.string.Interromper_conexao)) { dialog, _ ->
                dialog.dismiss()
                binding.fabDisconnect.isEnabled = false
                binding.pbAccept.visibility = VISIBLE
                if (accountIsHost) disconnectFromHost()
                else disconnectGuest()
            }.show()

    }

    private fun disconnectFromHost() = GlobalScope.launch(Dispatchers.IO) {

        val result = UserRepository.disconnectFromHost(account)
        withContext(Dispatchers.Main) {

            if (result.isSuccess) {

                AlertDialog.Builder(targetActivity)
                    .setTitle(targetActivity.getString(R.string.Conta_desconectada_com_sucesso))
                    .setMessage(targetActivity.getString(R.string.O_app_ser_fechado_para_aplicar_as_altera_es))
                    .setPositiveButton(targetActivity.getString(R.string.Entendi)) { dialog, _ ->
                        GlobalScope.launch {
                            dialog.dismiss()
                            binding.root.postDelayed({
                                exitProcess(0)
                            }, 250) // 250ms delay)
                        }
                    }
                    .show()


            } else showErrorMsg(targetActivity.getString(R.string.Nao_foi_possivel_desconectar_o_anfitriao_por_favor_tente_novamente))
        }
    }

    private fun disconnectGuest() = GlobalScope.launch(Dispatchers.IO) {

        val result = UserRepository.disconnectGuest(account)
        if (result.isSuccess) {
            dialog.dismiss()
        } else showErrorMsg(targetActivity.getString(R.string.Nao_foi_possivel_desconectar_o_convidado_por_favor_tente_novamente))

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
