package dev.gmarques.compras.ui.products

import android.annotation.SuppressLint
import android.app.Activity
import android.view.View
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import dev.gmarques.compras.R
import dev.gmarques.compras.data.data.model.Product
import dev.gmarques.compras.data.data.repository.ProductRepository
import dev.gmarques.compras.databinding.BsdEditProductPriceAndQuantityDialogBinding
import dev.gmarques.compras.ui.Vibrator
import dev.gmarques.compras.utils.ExtFun.Companion.currencyToDouble
import dev.gmarques.compras.utils.ExtFun.Companion.onlyIntegerNumbers
import dev.gmarques.compras.utils.ExtFun.Companion.toCurrency
import dev.gmarques.compras.utils.Result


@SuppressLint("SetTextI18n") // nao é necessario levar a localizaçao em conta quando editando valor e quantidade nessa tela
class BsdEditProductPriceOrQuantity(
    targetActivity: Activity,
    private val editProduct: Product,
) {

    private var binding = BsdEditProductPriceAndQuantityDialogBinding.inflate(targetActivity.layoutInflater)
    private val dialog: BottomSheetDialog = BottomSheetDialog(targetActivity)
    private lateinit var onConfirmListener: ((Product) -> Unit)

    init {
        dialog.setContentView(binding.root)

        with(binding) {

            edtInputQuantity.setText(String.format(targetActivity.getString(R.string.un), editProduct.quantity))
            edtInputPrice.setText(editProduct.price.toCurrency())

            // Configura o botão de confirmação
            fabConfirm.setOnClickListener {

                val newQuantity = edtInputQuantity.text.toString().onlyIntegerNumbers()

                // FIXME: as vezes o valor digitado sera moeda ou nao, tem que ajustar o codigo pra lidar com isso   
                val newPrice = edtInputPrice.text.toString().currencyToDouble()

                validateUserInput(newQuantity, newPrice)
            }
        }
    }


    private fun validateUserInput(newQuantity: Int, newPrice: Double) {

        val resultQuantity = ProductRepository.validateQuantity(newQuantity)
        val resultPrice = ProductRepository.validatePrice(newPrice)

        when {
            resultQuantity is Result.Success && resultPrice is Result.Success -> {
                updateProductAndClose(newQuantity, newPrice)
                Vibrator.success()
            }

            resultQuantity is Result.Error -> {
                showErrorSnackbar(resultQuantity.exception.message)
            }

            resultPrice is Result.Error -> {
                showErrorSnackbar(resultPrice.exception.message)
            }
        }
    }

    private fun showErrorSnackbar(message: String?) {
        Vibrator.error()
        Snackbar.make(binding.root, message.orEmpty(), Snackbar.LENGTH_LONG).show()
    }


    private fun updateProductAndClose(newQuantity: Int, newPrice: Double) {
        val edited = editProduct.copy(quantity = newQuantity, price = newPrice)
        onConfirmListener.invoke(edited)
        dialog.dismiss()
    }


    fun setOnConfirmListener(listener: (Product) -> Unit): BsdEditProductPriceOrQuantity {
        onConfirmListener = listener
        return this
    }

    fun show() {

        dialog.show()
        binding.edtInputPrice.requestFocus()

        // Acesse o comportamento do BottomSheet e defina o estado para expandido
        val behavior = BottomSheetBehavior.from(binding.root.parent as View)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }


}


