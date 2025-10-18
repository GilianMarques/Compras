package dev.gmarques.compras.presenter.products

import android.app.Activity
import android.content.DialogInterface
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.TextView
import androidx.appcompat.widget.AppCompatEditText
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import dev.gmarques.compras.R
import dev.gmarques.compras.data.model.Product
import dev.gmarques.compras.databinding.BsdAddProductInfoBinding
import dev.gmarques.compras.presenter.Vibrator

class BsdAddProductInfo(
    private val targetActivity: Activity,
    private val callback: (info: String) -> Any,
) : DialogInterface.OnDismissListener {

    private var binding = BsdAddProductInfoBinding.inflate(targetActivity.layoutInflater)
    private val dialog: BottomSheetDialog = BottomSheetDialog(targetActivity)
    private var canSave = false

    init {
        dialog.setContentView(binding.root)
        dialog.setOnDismissListener(this)
        setupListeners()
        setupInputInfo()
    }

    private fun setupInputInfo() {

        val edtTarget = binding.edtInfo
        val tvTarget = binding.tvInfoError

        edtTarget.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) resetFocus(edtTarget, tvTarget)
            else {
                val term = edtTarget.text.toString()
                val result = Product.Validator.validateInfo(term, targetActivity)

                if (result.isSuccess) edtTarget.setText(result.getOrThrow())
                else showError(edtTarget, tvTarget, result.exceptionOrNull()!!.message!!)

            }
        }

    }

    private fun showError(targetView: View, errorView: TextView, errorMessage: String) {
        targetView.setBackgroundResource(R.drawable.back_addproduct_edittext_error)
        errorView.text = errorMessage
        errorView.visibility = VISIBLE
        Vibrator.error()
        targetView.clearFocus()
        targetView.postDelayed({ targetView.requestFocus() }, 2000)
    }

    private fun resetFocus(edtTarget: AppCompatEditText, tvTarget: TextView) {
        edtTarget.setBackgroundResource(R.drawable.back_edit_text)
        tvTarget.visibility = GONE
    }

    private fun setupListeners() = binding.apply {

        fabConfirm.setOnClickListener {
            if (edtInfo.text.isNullOrEmpty()) showError(
                edtInfo, tvInfoError, targetActivity.getString(R.string.Nao_poss_vel_salvar_com_o_campo_vazio)
            )
            else {

                val result = Product.Validator.validateInfo(binding.edtInfo.text.toString(), targetActivity)
                if (result.isSuccess) {
                    canSave = true
                    dialog.dismiss()
                } else showError(
                    edtInfo, tvInfoError, targetActivity.getString(R.string.Conteudo_inv_lido_tente_novamente)
                )

            }
        }

    }

    fun show() {
        dialog.show()

        val behavior = BottomSheetBehavior.from(binding.root.parent as View)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    override fun onDismiss(p0: DialogInterface?) {
        if (canSave) {
            val result = Product.Validator.validateInfo(binding.edtInfo.text.toString(), targetActivity)
            callback(result.getOrThrow())
        } else callback("")
    }


}
