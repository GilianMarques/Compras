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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dev.gmarques.compras.App
import dev.gmarques.compras.BuildConfig
import dev.gmarques.compras.R
import dev.gmarques.compras.data.model.SyncAccount
import dev.gmarques.compras.data.repository.UserRepository
import dev.gmarques.compras.databinding.BsdManageSyncInviteBinding
import dev.gmarques.compras.ui.Vibrator
import kotlinx.coroutines.launch

class BsdManageSyncInvite(
    private val invite: SyncAccount,
    private val canAcceptInvite: Boolean,
    private val targetActivity: Activity,
    private val lifecycleScope: LifecycleCoroutineScope,
) {

    private var binding = BsdManageSyncInviteBinding.inflate(targetActivity.layoutInflater)
    private val dialog: BottomSheetDialog = BottomSheetDialog(targetActivity)


    init {
        dialog.setContentView(binding.root)

        binding.apply {

            fabAccept.setOnClickListener {
                Vibrator.interaction()
                if (invite.mergeData) showDialogConfirmToKeepDeviceOnWhileCloningData()
                else acceptInvite()
            }

            fabDecline.setOnClickListener {
                Vibrator.interaction()
                declineInvite()
                fabDecline.isEnabled = false
                pbStatus.visibility = VISIBLE
            }

            tvUserName.text = invite.name
            tvEmail.text = invite.email


            Glide.with(root.context)
                .load(invite.photoUrl)
                .circleCrop()
                .placeholder(R.drawable.vec_invite_user)
                .into(ivProfilePicture)


            startProgressAnimation(pbAccept) {
                binding.fabAccept.isEnabled = canAcceptInvite
                fabAccept.visibility = VISIBLE
                pbAccept.visibility = GONE
            }

            tvInfo.text = if (!canAcceptInvite) {
                targetActivity.getString(R.string.Voce_nao_pode_aceitar_um_convite_enquanto)
            } else if (invite.mergeData) {
                targetActivity.getString(R.string.X_te_enviou_um_convite_para_sincronizar_dados_com_ele, invite.name)
            } else targetActivity.getString(R.string.X_te_enviou_um_convite_para_a_conta_dele, invite.name)

        }
    }

    private fun showDialogConfirmToKeepDeviceOnWhileCloningData() {

        val title = targetActivity.getString(R.string.Atencao)
        val msg = targetActivity.getString(R.string.Nao_feche_o_app_ou_se_desconecte_da_internet)

        MaterialAlertDialogBuilder(targetActivity).setTitle(title).setMessage(msg)
            .setPositiveButton(targetActivity.getString(R.string.Entendi)) { dialog, _ ->
                dialog.dismiss()
                acceptInvite()

            }.setNegativeButton(targetActivity.getString(R.string.Cancelar)) { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false).show()

    }

    private fun startProgressAnimation(
        pbAccept: ProgressBar,
        duration: Long = if (BuildConfig.DEBUG || !canAcceptInvite) 1L else 5000L,
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

        MaterialAlertDialogBuilder(targetActivity)
            .setTitle(title)
            .setCancelable(false)
            .setPositiveButton(targetActivity.getString(R.string.Entendi)) { dialog, _ ->
                dialog.dismiss()
                binding.root.postDelayed({
                    this@BsdManageSyncInvite.dialog.dismiss()
                }, 250) // 250ms delay
            }
            .show()
    }

    private fun acceptInvite() = lifecycleScope.launch {

        binding.fabAccept.isEnabled = false
        binding.pbStatus.visibility = VISIBLE

        val success = UserRepository.acceptInvite(invite)
        if (success) {
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

        MaterialAlertDialogBuilder(targetActivity)
            .setTitle(title)
            .setMessage(msg)
            .setCancelable(false)
            .setPositiveButton(targetActivity.getString(R.string.Entendi)) { dialog, _ ->
                dialog.dismiss()
                App.close(targetActivity)
            }
            .show()

    }

    fun show() {

        dialog.show()

        val behavior = BottomSheetBehavior.from(binding.root.parent as View)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }


}
