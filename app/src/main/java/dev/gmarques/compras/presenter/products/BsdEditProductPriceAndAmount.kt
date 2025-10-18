package dev.gmarques.compras.presenter.products

import dev.gmarques.compras.presenter.PricesHistoryViewComponent
import android.app.Activity
import android.view.View
import android.view.View.VISIBLE
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import dev.gmarques.compras.App
import dev.gmarques.compras.R
import dev.gmarques.compras.data.model.Establishment
import dev.gmarques.compras.data.model.Product
import dev.gmarques.compras.databinding.BsdEditProductDialogBinding
import dev.gmarques.compras.domain.model.PriceHistory
import dev.gmarques.compras.domain.utils.ExtFun.Companion.currencyToDouble
import dev.gmarques.compras.domain.utils.ExtFun.Companion.onlyIntegerNumbers
import dev.gmarques.compras.domain.utils.ExtFun.Companion.toCurrency
import dev.gmarques.compras.presenter.Vibrator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.cancel

class BsdEditProduct private constructor() {

    private var currentEstablishment: Establishment? = null
    private var focusOnInfo: Boolean = false
    private var focusOnQuantity: Boolean = false
    private var focusOnPrice: Boolean = false

    private var buyAndSave: Boolean = false

    private lateinit var targetActivity: Activity

    private lateinit var editProduct: Product

    private lateinit var binding: BsdEditProductDialogBinding
    private lateinit var dialog: BottomSheetDialog

    private lateinit var onConfirmListener: ((Product) -> Unit)
    private lateinit var onEditListener: ((Product) -> Unit)
    private lateinit var onRemoveListener: ((Product) -> Unit)

    private val scope = CoroutineScope(IO)

    private fun init() {
        dialog = BottomSheetDialog(targetActivity)
        binding = BsdEditProductDialogBinding.inflate(targetActivity.layoutInflater)
        dialog.setContentView(binding.root)
        dialog.setOnDismissListener { scope.cancel("Dialog dismissed") }

        with(binding) {

            edtQuantity.setText(
                String.format(
                    targetActivity.getString(R.string.un), editProduct.quantity
                )
            )
            edtPrice.setText(editProduct.price.toCurrency())
            edtInfo.setText(editProduct.info)
            tvTitle.text = targetActivity.getString(R.string.Editar_x, editProduct.name)

            val clickOnSave = {
                val newQuantity = edtQuantity.text.toString().onlyIntegerNumbers()
                val newPrice = edtPrice.text.toString().currencyToDouble()
                val newInfo = edtInfo.text.toString()
                validateUserInput(newQuantity, newPrice, newInfo)
            }
            fabSave.setOnClickListener { clickOnSave() }
            fabBuyAndSave.setOnClickListener { buyAndSave = true; clickOnSave() }

            if (focusOnQuantity) edtQuantity.requestFocus()
            else if (focusOnPrice) edtPrice.requestFocus()
            else if (focusOnInfo) edtInfo.requestFocus()
            else llAdittionalOptions.visibility = VISIBLE

        }

        setupInputPriceFocusListener()
        setupInputQuantityFocusListener()
        setupInputInfoFocusListener()
        setupAdditionalOptions()
        setupPriceHistory()

    }

    private fun setupPriceHistory() {

        val onPriceClick: (PriceHistory) -> Unit = { priceItem ->
            binding.edtPrice.requestFocus()
            binding.edtPrice.setText(priceItem.price.toCurrency())
        }

        val pricesHistory = PricesHistoryViewComponent(
            targetActivity.layoutInflater,
            scope,
            editProduct,
            onPriceClick
        )
        binding.fragment.addView(pricesHistory.view)
    }

    private fun setupAdditionalOptions() = binding.apply {

        tvEditProduct.setOnClickListener { onEditListener(editProduct); dialog.dismiss() }

        tvRemoveProduct.setOnClickListener { onRemoveListener(editProduct); dialog.dismiss() }

    }

    private fun setupInputPriceFocusListener() {
        val edtTarget = binding.edtPrice
        edtTarget.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val term = edtTarget.text.toString().ifBlank { "-1" }.currencyToDouble()
                val result = Product.Validator.validatePrice(term, App.getContext())
                if (result.isSuccess) edtTarget.setText(result.getOrThrow().toCurrency())
            }
        }
    }

    private fun setupInputQuantityFocusListener() {
        val edtTarget = binding.edtQuantity
        edtTarget.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val term = edtTarget.text.toString().ifBlank { "0" }.onlyIntegerNumbers()
                val result = Product.Validator.validateQuantity(term, targetActivity)
                if (result.isSuccess) edtTarget.setText(
                    String.format(
                        targetActivity.getString(R.string.un), result.getOrThrow()
                    )
                )
            }
        }
    }

    private fun setupInputInfoFocusListener() {
        val edtTarget = binding.edtInfo
        edtTarget.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val term = edtTarget.text.toString()
                val result = Product.Validator.validateInfo(term, targetActivity)
                if (result.isSuccess) edtTarget.setText(result.getOrThrow())
            }
        }
    }

    private fun validateUserInput(newQuantity: Int, newPrice: Double, info: String) {
        val resultQuantity = Product.Validator.validateQuantity(newQuantity, targetActivity)
        val resultPrice = Product.Validator.validatePrice(newPrice, App.getContext())
        val resultInfo = Product.Validator.validateInfo(info, App.getContext())

        when {
            resultQuantity.isSuccess && resultPrice.isSuccess && resultInfo.isSuccess -> {
                updateProductAndClose(newQuantity, newPrice, resultInfo.getOrThrow())
                Vibrator.success()
            }

            resultQuantity.isFailure -> {
                showErrorSnackBar(resultQuantity.exceptionOrNull()!!.message)
            }

            resultPrice.isFailure -> {
                showErrorSnackBar(resultPrice.exceptionOrNull()!!.message)
            }

            resultInfo.isFailure -> {
                showErrorSnackBar(resultInfo.exceptionOrNull()!!.message)
            }
        }
    }

    private fun showErrorSnackBar(message: String?) {
        Vibrator.error()
        Snackbar.make(binding.root, message.orEmpty(), Snackbar.LENGTH_LONG).show()
    }

    private fun updateProductAndClose(newQuantity: Int, newPrice: Double, newInfo: String) {
        val edited = if (buyAndSave) editProduct.copy(
            quantity = newQuantity,
            price = newPrice,
            info = newInfo,
            hasBeenBought = true,
            boughtDate = System.currentTimeMillis(),
            establishmentId = currentEstablishment?.id
        )
        else editProduct.copy(
            quantity = newQuantity, price = newPrice, info = newInfo
        )
        onConfirmListener(edited)
        dialog.dismiss()
    }

    fun show() {
        dialog.show()
        val behavior = BottomSheetBehavior.from(binding.root.parent as View)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }


    class Builder {
        private val instance = BsdEditProduct()

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

        fun setFocus(focus: Focus?): Builder {

            when (focus) {
                Focus.PRICE -> instance.focusOnPrice = true
                Focus.QUANTITY -> instance.focusOnQuantity = true
                Focus.INFO -> instance.focusOnInfo = true
                else -> {}
            }

            return this
        }


        fun build(): BsdEditProduct {
            instance.init()
            return instance
        }

        fun setCurrentEstablishment(currentEstablishment: Establishment?): Builder {
            instance.currentEstablishment = currentEstablishment
            return this
        }


    }

    enum class Focus {
        PRICE, QUANTITY, INFO
    }

}
