package dev.gmarques.compras.ui.products

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
import dev.gmarques.compras.data.PreferencesHelper
import dev.gmarques.compras.data.PreferencesHelper.PrefsDefaultValue
import dev.gmarques.compras.data.PreferencesHelper.PrefsKeys.Companion.BOUGHT_PRODUCTS_AT_END
import dev.gmarques.compras.data.PreferencesHelper.PrefsKeys.Companion.SORT_ASCENDING
import dev.gmarques.compras.data.PreferencesHelper.PrefsKeys.Companion.SORT_CRITERIA
import dev.gmarques.compras.data.model.Product
import dev.gmarques.compras.data.repository.ProductRepository
import dev.gmarques.compras.data.repository.model.ValidatedProduct
import dev.gmarques.compras.databinding.BsdAddProductInfoBinding
import dev.gmarques.compras.databinding.BsdSortProductsDialogBinding
import dev.gmarques.compras.domain.SortCriteria
import dev.gmarques.compras.ui.Vibrator

class BsdAddProductInfo(
    private val targetActivity: Activity,
    private val callback: (info: String) -> Any,
) : DialogInterface.OnDismissListener {

    private var binding = BsdAddProductInfoBinding.inflate(targetActivity.layoutInflater)
    private val dialog: BottomSheetDialog = BottomSheetDialog(targetActivity)

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
    }

    private fun resetFocus(edtTarget: AppCompatEditText, tvTarget: TextView) {
        edtTarget.setBackgroundResource(R.drawable.back_addproduct_edittext)
        tvTarget.visibility = GONE
    }

    private fun setupListeners() = binding.apply {

        fabConfirm.setOnClickListener {
            dialog.dismiss()
        }

    }

    fun show() {
        dialog.show()

        val behavior = BottomSheetBehavior.from(binding.root.parent as View)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    override fun onDismiss(dialog: DialogInterface?) {
        val term = binding.edtInfo.text.toString()
        val result = Product.Validator.validateInfo(term, targetActivity)

        if (result.isSuccess) callback(result.getOrThrow())
        else callback("")
    }


}
