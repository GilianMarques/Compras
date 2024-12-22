package dev.gmarques.compras.ui.add_product

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import dev.gmarques.compras.R
import dev.gmarques.compras.data.data.model.Product
import dev.gmarques.compras.databinding.ActivityAddProductBinding
import dev.gmarques.compras.ui.Vibrator
import dev.gmarques.compras.utils.ExtFun.Companion.currencyToDouble
import dev.gmarques.compras.utils.ExtFun.Companion.onlyIntegerNumbers
import dev.gmarques.compras.utils.ExtFun.Companion.showKeyboard
import dev.gmarques.compras.utils.ExtFun.Companion.toCurrency
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Activity para adicionar ou editar produtos em uma lista.
 * Implementada seguindo o padrão MVVM e princípios de Clean Code e SOLID.
 */
class AddEditProductActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddProductBinding
    private lateinit var viewModel: AddProductActivityViewModel

    companion object {
        private const val LIST_ID = "list_id"
        private const val PRODUCT_ID = "poduct_id"

        fun newIntentAddProduct(context: Context, listId: Long): Intent {
            return Intent(context, AddEditProductActivity::class.java).apply {
                putExtra(LIST_ID, listId)
            }
        }

        fun newIntentEditProduct(context: Context, listId: Long, productId: Long): Intent {
            return Intent(context, AddEditProductActivity::class.java).apply {
                putExtra(LIST_ID, listId)
                putExtra(PRODUCT_ID, productId)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[AddProductActivityViewModel::class.java]
        viewModel.listId = intent.getLongExtra(LIST_ID, -1)
        viewModel.productId = intent.getLongExtra(PRODUCT_ID, -1)


        setupToolbar()
        initFabAddProduct()
        setupInputName()
        setupInputInfo()
        setupInputPrice()
        setupInputQuantity()
        observeProduct()
        observeViewmodelErrorMessages()
        observeViewmodelFinishEvent()

        lifecycleScope.launch {
            withContext(IO) {
                delay(1000)
                binding.cbSuggestProduct.post { binding.cbSuggestProduct.isChecked = true }
            }
        }

        binding.edtName.showKeyboard()

    }

    private fun observeViewmodelErrorMessages() = lifecycleScope.launch {
        viewModel.errorEventFlow.collect { event ->
            Snackbar.make(binding.root, event, Snackbar.LENGTH_LONG).show()
            Vibrator.error()
        }
    }

    private fun observeViewmodelFinishEvent() = lifecycleScope.launch {
        viewModel.finishEventFlow.collect {
            finish()
        }
    }

    private fun observeProduct() = lifecycleScope.launch {
        viewModel.loadProduct()
        viewModel.editingProductLD.observe(this@AddEditProductActivity) {

            it?.let {
                viewModel.editingProduct = true
                updateViewModelAndUiWithEditableProduct(it)
            }
        }
    }

    private fun updateViewModelAndUiWithEditableProduct(product: Product) = binding.apply {
        viewModel.apply {
            cbSuggestProduct.visibility = GONE

            edtName.setText(product.name)
            validatedName = product.name

            edtInfo.setText(product.info)
            validatedInfo = product.info

            edtPrice.setText(product.price.toCurrency())
            validatedPrice = product.price

            edtQuantity.setText(String.format(getString(R.string.un), product.quantity))
            validatedQuantity = product.quantity

            toolbar.tvActivityTitle.text = String.format(getString(R.string.Editar_x), product.name)
            fabSave.text = getString(R.string.Salvar_produto)
        }
    }

    /**
     * Configura o botão de salvar produto (FAB).
     */
    private fun initFabAddProduct() = binding.apply {
        fabSave.setOnClickListener {
            root.clearFocus()

            viewModel.apply {

                if (validatedName.isEmpty()) edtName.requestFocus()
                else if (validatedPrice <= 0) edtPrice.requestFocus()
                else if (validatedQuantity <= 0) edtQuantity.requestFocus()
                else tryAndSaveProduct(cbSuggestProduct.isChecked)
                root.clearFocus()

            }
        }
    }

    private fun setupInputName() {

        val edtTarget = binding.edtName
        val tvTarget = binding.tvNameError

        edtTarget.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) resetFocus(edtTarget, tvTarget)
            else {
                val term = edtTarget.text.toString()
                val result = Product.Validator.validateName(term)

                if (result.isSuccess) {
                    viewModel.validatedName = result.getOrThrow()
                    edtTarget.setText(viewModel.validatedName)
                } else {
                    viewModel.validatedName = ""
                    showError(edtTarget, tvTarget, result.exceptionOrNull()!!.message!!)
                }
            }
        }

    }

    private fun setupInputInfo() {

        val edtTarget = binding.edtInfo
        val tvTarget = binding.tvInfoError

        edtTarget.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) resetFocus(edtTarget, tvTarget)
            else {
                val term = edtTarget.text.toString()
                val result = Product.Validator.validateInfo(term)

                if (result.isSuccess) {
                    viewModel.validatedInfo = result.getOrThrow()
                    edtTarget.setText(viewModel.validatedInfo)
                } else {
                    viewModel.validatedInfo = ""
                    showError(edtTarget, tvTarget, result.exceptionOrNull()!!.message!!)
                }
            }
        }

    }

    private fun setupInputPrice() {

        val edtTarget = binding.edtPrice
        val tvTarget = binding.tvPriceError

        edtTarget.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) resetFocus(edtTarget, tvTarget)
            else {
                val term = edtTarget.text.toString().ifBlank { "-1" }.currencyToDouble()
                val result = Product.Validator.validatePrice(term)

                if (result.isSuccess) {
                    viewModel.validatedPrice = result.getOrThrow()
                    edtTarget.setText(viewModel.validatedPrice.toCurrency())
                } else {
                    viewModel.validatedPrice = -1.0
                    showError(edtTarget, tvTarget, result.exceptionOrNull()!!.message!!)
                }
            }
        }

    }

    private fun setupInputQuantity() {

        val edtTarget = binding.edtQuantity
        val tvTarget = binding.tvQuantityError

        edtTarget.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) resetFocus(edtTarget, tvTarget)
            else {
                val term = edtTarget.text.toString().ifBlank { "0" }.onlyIntegerNumbers()
                val result = Product.Validator.validateQuantity(term)

                if (result.isSuccess) {
                    viewModel.validatedQuantity = result.getOrThrow()
                    edtTarget.setText(String.format(getString(R.string.un), viewModel.validatedQuantity))
                } else {
                    viewModel.validatedQuantity = -1
                    showError(edtTarget, tvTarget, result.exceptionOrNull()!!.message!!)
                }
            }
        }

    }

    private fun resetFocus(edtTarget: AppCompatEditText, tvTarget: TextView) {
        edtTarget.setBackgroundResource(R.drawable.back_addproduct_edittext)
        tvTarget.visibility = GONE
    }

    private fun showError(targetView: View, errorView: TextView, errorMessage: String) {
        targetView.setBackgroundResource(R.drawable.back_addproduct_edittext_error)
        errorView.text = errorMessage
        errorView.visibility = VISIBLE
        Vibrator.error()
    }

    /**
     * Configura a toolbar da activity.
     */
    private fun setupToolbar() = binding.toolbar.apply {
        tvActivityTitle.text = getString(R.string.Adicionar_produto)
        ivGoBack.setOnClickListener { finish() }
        ivMenu.visibility = GONE
    }

}
