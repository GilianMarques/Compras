package dev.gmarques.compras.ui.profile

import android.animation.ValueAnimator
import android.app.Activity
import android.app.AlertDialog
import android.os.CountDownTimer
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
import dev.gmarques.compras.data.model.SyncRequest
import dev.gmarques.compras.data.repository.UserRepository
import dev.gmarques.compras.databinding.BsdManageSyncRequestBinding
import dev.gmarques.compras.ui.Vibrator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BsdManageSyncRequest(
    private val request: SyncRequest,
    private val targetActivity: Activity,
    private val lifecycleScope: LifecycleCoroutineScope,
) {

    private var canAccept: Boolean = false
    private var binding = BsdManageSyncRequestBinding.inflate(targetActivity.layoutInflater)
    private val dialog: BottomSheetDialog = BottomSheetDialog(targetActivity)


    init {
        dialog.setContentView(binding.root)

        binding.apply {

            fabAccept.setOnClickListener {
                if (!canAccept) return@setOnClickListener // melhor que desativar o botao (n gosto da aparencia do botao desativado)
                Vibrator.interaction()
                acceptRequest()
                fabAccept.isEnabled = false
            }

            fabDecline.setOnClickListener {
                Vibrator.interaction()
                declineRequest()
                fabDecline.isEnabled = false
            }

            tvUserName.text = request.name
            tvEmail.text = request.email
            tvInfo.text = targetActivity.getString(
                R.string.X_te_enviou_uma_solicita_o_de_sincronismo_ao_aceitar,
                request.name
            )

            Glide.with(root.context)
                .load(request.photoUrl)
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
        duration: Long = 3000L,
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


    private fun declineRequest() {

        val success = UserRepository.declineRequest(request)
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
                    this@BsdManageSyncRequest.dialog.dismiss()
                }, 500) // 500ms delay
            }
            .show()
    }


    private fun acceptRequest() = lifecycleScope.launch {

        val success = UserRepository.acceptRequest(request)
        if (success) Vibrator.success() else Vibrator.error()

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
