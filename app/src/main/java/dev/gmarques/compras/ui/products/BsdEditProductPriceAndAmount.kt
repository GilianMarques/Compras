package dev.gmarques.compras.ui.products

import android.app.Activity
import android.view.View
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import dev.gmarques.compras.R
import dev.gmarques.compras.data.data.model.Product
import dev.gmarques.compras.databinding.BsdEditProductPriceAndQuantityDialogBinding
import dev.gmarques.compras.ui.Vibrator
import dev.gmarques.compras.utils.ExtFun.Companion.currencyToDouble
import dev.gmarques.compras.utils.ExtFun.Companion.onlyIntegerNumbers
import dev.gmarques.compras.utils.ExtFun.Companion.toCurrency

class BsdEditProductPriceOrQuantity private constructor() {

    private lateinit var targetActivity: Activity
    private lateinit var editProduct: Product
    private lateinit var binding: BsdEditProductPriceAndQuantityDialogBinding
    private lateinit var dialog: BottomSheetDialog
    private lateinit var onConfirmListener: ((Product) -> Unit)
    private lateinit var onEditListener: ((Product) -> Unit)
    private lateinit var onRemoveListener: ((Product) -> Unit)

    private fun init() {
        dialog = BottomSheetDialog(targetActivity)
        binding = BsdEditProductPriceAndQuantityDialogBinding.inflate(targetActivity.layoutInflater)
        dialog.setContentView(binding.root)

        with(binding) {

            edtQuantity.setText(String.format(targetActivity.getString(R.string.un), editProduct.quantity))
            edtPrice.setText(editProduct.price.toCurrency())

            fabConfirm.setOnClickListener {
                val newQuantity = edtQuantity.text.toString().onlyIntegerNumbers()
                val newPrice = edtPrice.text.toString().currencyToDouble()
                validateUserInput(newQuantity, newPrice)
            }
        }
        setupInputPriceFocusListener()
        setupInputQuantityFocusListener()
        setupAditionalOptions()
    }

    private fun setupAditionalOptions() = binding.apply {

        tvEditProduct.setOnClickListener { onEditListener(editProduct); dialog.dismiss() }

        tvRemoveProduct.setOnClickListener { onRemoveListener(editProduct); dialog.dismiss() }

    }

    private fun setupInputPriceFocusListener() {
        val edtTarget = binding.edtPrice
        edtTarget.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val term = edtTarget.text.toString().ifBlank { "-1" }.currencyToDouble()
                val result = Product.Validator.validatePrice(term)
                if (result.isSuccess) edtTarget.setText(result.getOrThrow().toCurrency())
            }
        }
    }

    private fun setupInputQuantityFocusListener() {
        val edtTarget = binding.edtQuantity
        edtTarget.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val term = edtTarget.text.toString().ifBlank { "0" }.onlyIntegerNumbers()
                val result = Product.Validator.validateQuantity(term)
                if (result.isSuccess) edtTarget.setText(String.format(targetActivity.getString(R.string.un), result.getOrThrow()))
            }
        }
    }

    private fun validateUserInput(newQuantity: Int, newPrice: Double) {
        val resultQuantity = Product.Validator.validateQuantity(newQuantity)
        val resultPrice = Product.Validator.validatePrice(newPrice)

        when {
            resultQuantity.isSuccess && resultPrice.isSuccess -> {
                updateProductAndClose(newQuantity, newPrice)
                Vibrator.success()
            }

            resultQuantity.isFailure -> {
                showErrorSnackbar(resultQuantity.exceptionOrNull()!!.message)
            }

            resultPrice.isFailure -> {
                showErrorSnackbar(resultPrice.exceptionOrNull()!!.message)
            }
        }
    }

    private fun showErrorSnackbar(message: String?) {
        Vibrator.error()
        Snackbar.make(binding.root, message.orEmpty(), Snackbar.LENGTH_LONG).show()
    }

    private fun updateProductAndClose(newQuantity: Int, newPrice: Double) {
        val edited = editProduct.copy(quantity = newQuantity, price = newPrice)
        onConfirmListener(edited)
        dialog.dismiss()
    }

    fun show() {
        dialog.show()
        val behavior = BottomSheetBehavior.from(binding.root.parent as View)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    class Builder {
        private val instance = BsdEditProductPriceOrQuantity()

        fun setActivity(activity: Activity): Builder {
            instance.targetActivity = activity
            return this
        }

        fun setProduct(product: Product): Builder {
            instance.editProduct = product
            return this
        }

        fun setConfirmListener(listener: (Product) -> Unit): Builder {
            instance.onConfirmListener = listener
            return this
        }

        fun setEditListener(listener: (Product) -> Unit): Builder {
            instance.onEditListener = listener
            return this
        }

        fun setRemoveListener(listener: (Product) -> Unit): Builder {
            instance.onRemoveListener = listener
            return this
        }

        fun build(): BsdEditProductPriceOrQuantity {
            instance.init()
            return instance
        }
    }
}
