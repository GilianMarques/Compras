package dev.gmarques.compras.presenter.add_edit_product

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.drawable.VectorDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
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
import dev.gmarques.compras.data.repository.CategoryRepository
import dev.gmarques.compras.databinding.ActivityAddEditProductBinding
import dev.gmarques.compras.domain.model.PriceHistory
import dev.gmarques.compras.domain.utils.ExtFun.Companion.currencyToDouble
import dev.gmarques.compras.domain.utils.ExtFun.Companion.dp
import dev.gmarques.compras.domain.utils.ExtFun.Companion.onlyIntegerNumbers
import dev.gmarques.compras.domain.utils.ExtFun.Companion.showKeyboard
import dev.gmarques.compras.domain.utils.ExtFun.Companion.toCurrency
import dev.gmarques.compras.presenter.MyActivity
import dev.gmarques.compras.presenter.PricesHistoryViewComponent
import dev.gmarques.compras.presenter.Vibrator
import dev.gmarques.compras.presenter.categories.CategoriesActivity
import dev.gmarques.compras.presenter.categories.CategoriesActivity.Companion.SELECTED_CATEGORY
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch

/**
 * Activity para adicionar ou editar produtos em uma lista.
 * Implementada seguindo o padrão MVVM e princípios de Clean Code e SOLID.
 */
class AddEditProductActivity: MyActivity() {

    private var currentState: AddEditProductActivityViewModel.UiState? = null
    private lateinit var categoryResultLauncher: ActivityResultLauncher<Intent>

    private lateinit var binding: ActivityAddEditProductBinding
    private lateinit var viewModel: AddEditProductActivityViewModel

    companion object {
        private const val LIST_ID = "list_id"
        private const val PRODUCT_ID = "product_id"
        private const val DEF_CATEGORY = "def_category"
        private const val DEF_NAME = "def_name"

        fun newIntentAddProduct(
            context: Context,
            listId: String,
            category: Category?,
            searchTerm: String,
        ): Intent {
            return Intent(context, AddEditProductActivity::class.java).apply {
                putExtra(LIST_ID, listId)
                putExtra(DEF_NAME, searchTerm)
                putExtra(DEF_CATEGORY, category?.id)
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
        viewModel.setup(intent.getStringExtra(LIST_ID)!!, intent.getStringExtra(PRODUCT_ID))

        setupToolbar()
        setupFabAddProduct()
        setupInputName()
        setupInputInfo()
        setupInputAnnotations()
        setupInputPrice()
        setupInputQuantity()
        setupInputCategory()
        setupCbBought()
        observeUiStateChanges()
        setupActivityResultLauncher()
        setDefValuesIfAny()

        binding.cbSuggestProduct.postDelayed({
            binding.cbSuggestProduct.isChecked = true
        }, 1000)

        binding.edtName.showKeyboard()

    }

    private fun showPriceHistory(name: String) {
        binding.priceHistoryContainer.removeAllViews()

        val product = Product(name)

        val onPriceClick: (PriceHistory) -> Unit = { priceItem ->
            binding.edtPrice.requestFocus()
            binding.edtPrice.setText(priceItem.price.toCurrency())

        }

        val pricesHistory = PricesHistoryViewComponent(
            layoutInflater,
            lifecycleScope,
            product,
            onPriceClick
        )
        binding.priceHistoryContainer.addView(pricesHistory.view)
    }

    private fun setDefValuesIfAny() = lifecycleScope.launch(Main) {

        val defName = intent.getStringExtra(DEF_NAME)
        val defCategoryId = intent.getStringExtra(DEF_CATEGORY)

        defName?.let {
            binding.edtName.setText(defName)
        }

        defCategoryId?.let {
            viewModel.validatedCategory = CategoryRepository.getCategory(defCategoryId)
            setCategoryName()
        }
    }

    private fun observeUiStateChanges() {
        viewModel.uiStateLD.observe(this@AddEditProductActivity) { newState ->

            newState.editingProduct?.let {
                if (it == currentState?.editingProduct) return@let
                viewModel.loadCategory(it.categoryId)
                showPriceHistory(it.name)
            }

            newState.editingCategory?.let {
                if (it == currentState?.editingCategory) return@let
                updateViewModelAndUiWithEditableProduct(newState.editingProduct!!, it)
            }

            newState.suggestionProductAndCategory?.let {
                if (it == currentState?.suggestionProductAndCategory) return@let
                updateViewModelAndUiWithEditableProduct(it.first, it.second)
            }

            newState.productsAndNamesSuggestions.let {
                if (it == currentState?.productsAndNamesSuggestions) return@let

                if (it.isEmpty()) hideSuggestions()
                else showSuggestions(it)
            }

            newState.errorMessage.let {
                if (it == currentState?.errorMessage || it.isEmpty()) return@let

                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                Vibrator.error()
            }

            newState.finishActivity.let {
                if (it) finish()
            }

            this.currentState = newState

        }
    }

    private fun setupActivityResultLauncher() {

        categoryResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val selectedCategory =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            result.data!!.getSerializableExtra(
                                SELECTED_CATEGORY, Category::class.java
                            )
                        } else {
                            @Suppress("DEPRECATION") result.data!!.getSerializableExtra(
                                SELECTED_CATEGORY
                            ) as Category
                        }
                    viewModel.validatedCategory = selectedCategory
                    binding.edtCategory.clearFocus()
                }
            }
    }

    private fun showSuggestions(suggestions: List<Any>) {

        binding.tvSuggestion.visibility = VISIBLE
        binding.llSuggestion.removeAllViews()

        val layoutParams = LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
            .apply { marginStart = 4.dp(); marginEnd = 4.dp() }

        suggestions.forEach { suggestion ->
            val chip = Chip(this@AddEditProductActivity)
            chip.layoutParams = layoutParams
            binding.llSuggestion.addView(chip)

            if (suggestion is Product) {
                chip.text = suggestion.name
                chip.chipIcon = AppCompatResources.getDrawable(
                    this@AddEditProductActivity, R.drawable.vec_product
                )
            } else {
                val nameSuggestion = suggestion as String
                chip.text = nameSuggestion
                chip.chipIcon =
                    AppCompatResources.getDrawable(this@AddEditProductActivity, R.drawable.vec_name)
            }

            chip.setOnClickListener {
                binding.edtName.requestFocus()
                hideSuggestions()

                if (suggestion is Product) {
                    viewModel.loadSuggestionCategory(suggestion)
                    showPriceHistory(suggestion.name)
                } else {
                    val nameSuggestion = suggestion as String
                    viewModel.canLoadSuggestion = false
                    binding.edtName.setText(nameSuggestion)
                    viewModel.canLoadSuggestion = true
                    binding.edtInfo.requestFocus()

                    showPriceHistory(nameSuggestion)
                }

            }

        }
    }

    private fun hideSuggestions() {
        binding.tvSuggestion.visibility = GONE
        binding.llSuggestion.removeAllViews()
    }

    private fun updateViewModelAndUiWithEditableProduct(product: Product, category: Category) =
        binding.apply {
            viewModel.apply {

                viewModel.canLoadSuggestion = false
                edtName.setText(product.name)
                viewModel.canLoadSuggestion = true
                validatedName = product.name

                edtInfo.setText(product.info)
                validatedInfo = product.info

                edtAnnotation.setText(product.annotations)
                validatedInfo = product.annotations

                edtPrice.setText(product.price.toCurrency())
                validatedPrice = product.price

                edtQuantity.setText(String.format(getString(R.string.un), product.quantity))
                validatedQuantity = product.quantity

                edtCategory.hint = category.name
                (edtCategory.compoundDrawables[0].mutate() as? VectorDrawable)?.setTint(category.color)

                cbBought.isChecked = product.hasBeenBought
                productIsBought = product.hasBeenBought

                validatedCategory = category

                // essa funçao atualiza a UI em caso de ediçao de produto ou seleção de sugestão ao adicionar o produto, por isso faço a verificação
                val editingProduct = currentState?.editingProduct != null
                if (editingProduct) {
                    cbSuggestProduct.visibility = GONE
                    toolbar.tvActivityTitle.text =
                        String.format(getString(R.string.Editar_x), product.name)
                    fabSave.text = getString(R.string.Salvar_produto)
                }
            }
        }

    /**
     * Configura o botão de salvar produto (FAB).
     */
    private fun setupFabAddProduct() = binding.apply {
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
            if (edtTarget.hasFocus() && !text.isNullOrEmpty() && viewModel.canLoadSuggestion) {
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

    private fun setupInputAnnotations() {

        val edtTarget = binding.edtAnnotation
        val tvTarget = binding.tvAnnotationError

        edtTarget.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) resetFocus(edtTarget, tvTarget)
            else {
                val term = edtTarget.text.toString()
                val result = Product.Validator.validateAnnotations(term, this)

                if (result.isSuccess) {
                    viewModel.validatedAnnotation = result.getOrThrow()
                    edtTarget.setText(viewModel.validatedAnnotation)
                } else {
                    viewModel.validatedAnnotation = ""
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
                Log.d(
                    "USUK",
                    "AddEditProductActivity.setupInputPrice: antes ${edtTarget.text.toString()}"
                )
                val term = edtTarget.text.toString().ifBlank { "-1" }.currencyToDouble()
                Log.d("USUK", "AddEditProductActivity.setupInputPrice: depois ${term}")

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
                    edtTarget.setText(
                        String.format(
                            getString(R.string.un), viewModel.validatedQuantity
                        )
                    )
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
                categoryResultLauncher.launch(
                    CategoriesActivity.newIntent(
                        this@AddEditProductActivity, true
                    )
                )

            } else {

                if (viewModel.validatedCategory != null) {
                    setCategoryName()
                } else {
                    showError(edtTarget, tvTarget, getString(R.string.Selecione_uma_categoria))
                }
            }
        }

    }

    private fun setupCbBought() = with(binding) {
        cbBought.setOnCheckedChangeListener { _, isChecked ->
            viewModel.productIsBought = isChecked
        }
    }

    /**
     * Insere no campo correspondente da ui, nome e cor da categoria no viewwmodel
     */
    private fun setCategoryName() = binding.apply {
        edtCategory.hint = viewModel.validatedCategory!!.name
        (edtCategory.compoundDrawables[0].mutate() as? VectorDrawable)?.setTint(viewModel.validatedCategory!!.color)
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
        ivGoBack.setOnClickListener { Vibrator.interaction(); finish() }
        ivMenu.visibility = GONE
    }

}
