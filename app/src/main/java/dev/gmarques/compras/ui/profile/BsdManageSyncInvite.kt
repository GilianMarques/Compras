package dev.gmarques.compras.ui.profile

import android.animation.ValueAnimator
import android.app.Activity
import android.app.AlertDialog
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ProgressBar
import androidx.core.animation.doOnEnd
import androidx.lifecycle.LifecycleCoroutineScope
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import dev.gmarques.compras.R
import dev.gmarques.compras.data.PreferencesHelper
import dev.gmarques.compras.data.model.SyncAccount
import dev.gmarques.compras.data.repository.UserRepository
import dev.gmarques.compras.databinding.BsdManageSyncInviteBinding
import dev.gmarques.compras.ui.Vibrator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.system.exitProcess

class BsdManageSyncInvite(
    private val invite: SyncAccount,
    private val targetActivity: Activity,
    private val lifecycleScope: LifecycleCoroutineScope,
) {

    private var canAccept: Boolean = false
    private var binding = BsdManageSyncInviteBinding.inflate(targetActivity.layoutInflater)
    private val dialog: BottomSheetDialog = BottomSheetDialog(targetActivity)


    init {
        dialog.setContentView(binding.root)

        binding.apply {

            fabAccept.setOnClickListener {
                if (!canAccept) return@setOnClickListener // melhor que desativar o botao (n gosto da aparencia do botao desativado)
                Vibrator.interaction()
                acceptInvite()
                fabAccept.isEnabled = false
                pbStatus.visibility = VISIBLE
            }

            fabDecline.setOnClickListener {
                Vibrator.interaction()
                declineInvite()
                fabDecline.isEnabled = false
                pbStatus.visibility = VISIBLE
            }

            tvUserName.text = invite.name
            tvEmail.text = invite.email
            tvInfo.text = targetActivity.getString(
                R.string.X_te_enviou_uma_solicita_o_de_sincronismo_ao_aceitar,
                invite.name
            )

            Glide.with(root.context)
                .load(invite.photoUrl)
                .circleCrop()
                .placeholder(R.drawable.vec_invite_user)
                .into(ivProfilePicture)


            startProgressAnimation(pbAccept) {
                canAccept = true
                fabAccept.visibility = VISIBLE
                pbAccept.visibility = GONE
            }

        }

    }

    private fun startProgressAnimation(
        pbAccept: ProgressBar,
        duration: Long = 5000L,
        onComplete: () -> Unit,
    ) {
        ValueAnimator.ofInt(0, 100).apply {
            this.duration = duration
            interpolator = android.view.animation.AccelerateDecelerateInterpolator()

            addUpdateListener { animation ->
                pbAccept.progress = animation.animatedValue as Int
            }

            doOnEnd {
                onComplete()
            }

            start()
        }
    }


    private fun declineInvite() = lifecycleScope.launch {

        val success = UserRepository.declineInvite(invite)
        if (success) Vibrator.success() else Vibrator.error()

        val title =
            targetActivity.getString(
                if (success) R.string.solicita_recusada
                else R.string.Erro_ao_recusar_solicita_o
            )

        AlertDialog.Builder(targetActivity)
            .setTitle(title)
            .setPositiveButton(targetActivity.getString(R.string.Entendi)) { dialog, _ ->
                dialog.dismiss()
                binding.root.postDelayed({
                    this@BsdManageSyncInvite.dialog.dismiss()
                }, 250) // 250ms delay
            }
            .show()
    }

    private fun acceptInvite() = lifecycleScope.launch {

        val success = UserRepository.acceptInvite(invite)
        if (success) {
            PreferencesHelper().saveValue(PreferencesHelper.PrefsKeys.HOST, invite.email)
            Vibrator.success()
        } else {
            Vibrator.error()
        }

        val title =
            targetActivity.getString(
                if (success) R.string.solicita_o_aceita
                else R.string.Erro_ao_aceitar_solicita_o
            )
        val msg =
            targetActivity.getString(
                if (success) R.string.Solicita_o_aceita_com_sucesso_o_solicitante_deve_reiniciar_o_aplicativo
                else R.string.Nao_foi_poss_vel_aceitar_a_solicita_o_tente_novamente_mais_tarde
            )

        AlertDialog.Builder(targetActivity)
            .setTitle(title)
            .setMessage(msg)
            .setPositiveButton(targetActivity.getString(R.string.Entendi)) { dialog, _ ->
                dialog.dismiss()
                binding.root.postDelayed({
                    this@BsdManageSyncInvite.dialog.dismiss()
                    exitProcess(0)
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

        val behavior = BottomSheetBehavior.from(binding.root.parent as View)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }


}
