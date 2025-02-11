package dev.gmarques.compras.ui.add_edit_product

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.drawable.VectorDrawable
import android.os.Bundle
import android.text.Spanned
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import dev.gmarques.compras.App
import dev.gmarques.compras.R
import dev.gmarques.compras.data.model.Category
import dev.gmarques.compras.data.model.Product
import dev.gmarques.compras.databinding.ActivityAddEditProductBinding
import dev.gmarques.compras.domain.utils.ExtFun.Companion.currencyToDouble
import dev.gmarques.compras.domain.utils.ExtFun.Companion.dp
import dev.gmarques.compras.domain.utils.ExtFun.Companion.formatHtml
import dev.gmarques.compras.domain.utils.ExtFun.Companion.hideKeyboard
import dev.gmarques.compras.domain.utils.ExtFun.Companion.onlyIntegerNumbers
import dev.gmarques.compras.domain.utils.ExtFun.Companion.showKeyboard
import dev.gmarques.compras.domain.utils.ExtFun.Companion.toCurrency
import dev.gmarques.compras.ui.Vibrator
import dev.gmarques.compras.ui.add_edit_category.AddEditCategoryActivity
import dev.gmarques.compras.ui.add_edit_shop_list.AddEditShopListActivity
import dev.gmarques.compras.ui.categories.CategoriesActivity
import dev.gmarques.compras.ui.categories.CategoriesActivity.Companion.SELECTED_CATEGORY
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Activity para adicionar ou editar produtos em uma lista.
 * Implementada seguindo o padrão MVVM e princípios de Clean Code e SOLID.
 */
class AddEditProductActivity : AppCompatActivity() {

    private lateinit var categoryResultLauncher: ActivityResultLauncher<Intent>

    private lateinit var binding: ActivityAddEditProductBinding
    private lateinit var viewModel: AddEditProductActivityViewModel

    companion object {
        private const val LIST_ID = "list_id"
        private const val PRODUCT_ID = "poduct_id"

        fun newIntentAddProduct(context: Context, listId: String): Intent {
            return Intent(context, AddEditProductActivity::class.java).apply {
                putExtra(LIST_ID, listId)
            }
        }

        fun newIntentEditProduct(context: Context, listId: String, productId: String): Intent {
            return Intent(context, AddEditProductActivity::class.java).apply {
                putExtra(LIST_ID, listId)
                putExtra(PRODUCT_ID, productId)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddEditProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[AddEditProductActivityViewModel::class.java]
        viewModel.listId = intent.getStringExtra(LIST_ID)!!
        viewModel.productId = intent.getStringExtra(PRODUCT_ID)


        setupToolbar()
        initFabAddProduct()
        setupInputName()
        setupInputInfo()
        setupInputPrice()
        setupInputQuantity()
        setupInputCategory()
        observeProduct()
        observeSuggestions()
        observeNameSuggestions()
        observeViewmodelErrorMessages()
        observeViewmodelFinishEvent()
        setupActivityResultLauncher()

        lifecycleScope.launch {
            withContext(IO) {
                delay(1000)
                binding.cbSuggestProduct.post { binding.cbSuggestProduct.isChecked = true }
            }
        }

        binding.edtName.showKeyboard()

    }

    private fun setupActivityResultLauncher() {

        categoryResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val selectedCategory = result.data!!.getSerializableExtra(SELECTED_CATEGORY) as Category
                viewModel.validatedCategory = selectedCategory
                binding.edtCategory.clearFocus()
            }
        }
    }

    private fun observeViewmodelErrorMessages() {
        viewModel.errorEventLD.observe(this@AddEditProductActivity) { event ->
            Snackbar.make(binding.root, event, Snackbar.LENGTH_LONG).show()
            Vibrator.error()
        }
    }

    private fun observeViewmodelFinishEvent() {
        viewModel.finishEventLD.observe(this@AddEditProductActivity) {
            finish()
        }
    }

    private fun observeProduct() = viewModel.apply {
        loadEditingProduct()
        editingProductLD.observe(this@AddEditProductActivity) {

            it?.let {
                editingProduct = true
                loadCategory(it.categoryId)
                observeCategory(it)
            }
        }
    }

    private fun observeSuggestions() {
        viewModel.suggestionsLD.observe(this@AddEditProductActivity) { result ->
            val (suggestions, term) = result
            if (suggestions.isEmpty()) viewModel.loadNameSuggestions(term)
            else showSuggestions(suggestions)
        }
    }

    private fun observeNameSuggestions() = lifecycleScope.launch {
        viewModel.nameSuggestionsLD.observe(this@AddEditProductActivity) { suggestions ->
            if (suggestions.isNotEmpty()) showSuggestions(suggestions)
            else hideSuggestions()
        }
    }

    private fun showSuggestions(suggestions: List<Any>) {

        binding.tvSuggestion.visibility = VISIBLE
        binding.llSuggestion.removeAllViews()

        val layoutParams =
            LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply { marginStart = 4.dp(); marginEnd = 4.dp() }

        suggestions.forEach { suggestion ->
            val chip = Chip(this@AddEditProductActivity)
            chip.layoutParams = layoutParams
            binding.llSuggestion.addView(chip)

            if (suggestion is Product) {
                chip.text = suggestion.name
                chip.chipIcon = AppCompatResources.getDrawable(this@AddEditProductActivity, R.drawable.vec_product)
            } else {
                val nameSuggestion = suggestion as String
                chip.text = nameSuggestion
                chip.chipIcon = AppCompatResources.getDrawable(this@AddEditProductActivity, R.drawable.vec_name)
            }

            chip.setOnClickListener {
                binding.edtName.requestFocus()
                hideSuggestions()

                if (suggestion is Product) {
                    viewModel.loadCategory(suggestion.categoryId)
                    observeCategory(suggestion)
                    binding.edtName.hideKeyboard()
                } else {
                    val nameSuggestion = suggestion as String
                    viewModel.canLoadSuggestion = false
                    binding.edtName.setText(nameSuggestion)
                    viewModel.canLoadSuggestion = true
                    binding.edtInfo.requestFocus()
                }

            }

        }
    }

    private fun hideSuggestions() {
        binding.tvSuggestion.visibility = GONE
        binding.llSuggestion.removeAllViews()
    }

    private fun observeCategory(product: Product) = lifecycleScope.launch {
        viewModel.editingCategoryLD.observe(this@AddEditProductActivity) {

            it?.let {
                updateViewModelAndUiWithEditableProduct(product, it)
            }
        }
    }

    private fun updateViewModelAndUiWithEditableProduct(product: Product, category: Category) = binding.apply {
        viewModel.apply {

            viewModel.canLoadSuggestion = false
            edtName.setText(product.name)
            viewModel.canLoadSuggestion = true
            validatedName = product.name

            edtInfo.setText(product.info)
            validatedInfo = product.info

            edtPrice.setText(product.price.toCurrency())
            validatedPrice = product.price

            edtQuantity.setText(String.format(getString(R.string.un), product.quantity))
            validatedQuantity = product.quantity

            edtCategory.hint = category.name
            (edtCategory.compoundDrawables[0].mutate() as? VectorDrawable)?.setTint(category.color)

            validatedCategory = category

            if (editingProduct) {
                cbSuggestProduct.visibility = GONE
                toolbar.tvActivityTitle.text = String.format(getString(R.string.Editar_x), product.name)
                fabSave.text = getString(R.string.Salvar_produto)
            }
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
                else if (validatedCategory == null) edtCategory.requestFocus()
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
                hideSuggestions()

                val term = edtTarget.text.toString()
                val result = Product.Validator.validateName(term, App.getContext())

                if (result.isSuccess) {
                    viewModel.validatedName = result.getOrThrow()
                    edtTarget.setText(viewModel.validatedName)
                } else {
                    viewModel.validatedName = ""
                    showError(edtTarget, tvTarget, result.exceptionOrNull()!!.message!!)
                }
            }
        }

        edtTarget.doOnTextChanged { text, _, _, _ ->
            if (edtTarget.hasFocus() && !text.isNullOrEmpty() && text.length > 1 && viewModel.canLoadSuggestion) {
                viewModel.loadSuggestions(text.toString())
            } else hideSuggestions()

        }
    }

    private fun setupInputInfo() {

        val edtTarget = binding.edtInfo
        val tvTarget = binding.tvInfoError

        edtTarget.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) resetFocus(edtTarget, tvTarget)
            else {
                val term = edtTarget.text.toString()
                val result = Product.Validator.validateInfo(term, this)

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
                val result = Product.Validator.validatePrice(term, App.getContext())

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
                val result = Product.Validator.validateQuantity(term, this)

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

    private fun setupInputCategory() {

        val edtTarget = binding.edtCategory
        val tvTarget = binding.tvCaregoryError

        edtTarget.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                resetFocus(edtTarget, tvTarget)
                categoryResultLauncher.launch(CategoriesActivity.newIntent(this@AddEditProductActivity, true))

            } else {

                if (viewModel.validatedCategory != null) {
                    edtTarget.hint = viewModel.validatedCategory!!.name
                    (edtTarget.compoundDrawables[0].mutate() as? VectorDrawable)?.setTint(viewModel.validatedCategory!!.color)
                } else {
                    showError(edtTarget, tvTarget, getString(R.string.Selecione_uma_categoria))
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
        ivGoBack.setOnClickListener {Vibrator.interaction(); finish() }
        ivMenu.visibility = GONE
    }


}
