package dev.gmarques.compras.ui.add_edit_product

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.gmarques.compras.App
import dev.gmarques.compras.R
import dev.gmarques.compras.data.ProductNameSuggestion
import dev.gmarques.compras.data.model.Category
import dev.gmarques.compras.data.model.Product
import dev.gmarques.compras.data.repository.CategoryRepository
import dev.gmarques.compras.data.repository.ProductRepository
import dev.gmarques.compras.data.repository.SuggestionProductRepository
import dev.gmarques.compras.data.repository.model.ValidatedProduct
import dev.gmarques.compras.data.repository.model.ValidatedSuggestionProduct
import dev.gmarques.compras.domain.utils.ExtFun.Companion.removeAccents
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.min

class AddEditProductActivityViewModel : ViewModel() {


    private val uiState = UiState()

    private val _uiStateLD = MutableLiveData<UiState>()
    val uiStateLD: LiveData<UiState> get() = _uiStateLD

    var validatedName: String = ""
    var validatedInfo: String = ""
    var validatedPrice: Double = -1.0
    var validatedQuantity: Int = -1
    var validatedCategory: Category? = null
    var canLoadSuggestion: Boolean = true

    // objeto usado para prover sugestoes de nomes de produtos
    private val productNameSuggestion = ProductNameSuggestion()

    // lista de sugestoes de produtos do banco de dados
    private var suggestions: List<Product>? = null

    // maximo de sugestoes que devem ser exibidas na UI
    private val maxSuggestions = 5

    private var listId: String = "null"
    private var productId: String? = null

    fun tryAndSaveProduct(saveAsSuggestion: Boolean) = viewModelScope.launch(IO) {

        // se adicionando produto ou se durante a ediçao o usuario trocar o nome do produto, preciso verificar se o novo nome ja nao existe na lista
        val needCheckName =
            uiState.toEditProduct == null || uiState.toEditProduct!!.name != validatedName

        if (needCheckName) {
            val result = ProductRepository.getProductByName(validatedName, listId)

            if (result.getOrNull() == null) saveProduct(saveAsSuggestion)// o produto nao existe na lista, posso salvar
            else {
                val msg = String.format(
                    App.getContext().getString(R.string.X_ja_existe_na_lista), validatedName
                )
                uiState.errorMessage = msg
                this@AddEditProductActivityViewModel.postData()
            }

        } else saveProduct(saveAsSuggestion)

    }

    private fun saveProduct(saveAsSuggestion: Boolean) = viewModelScope.launch(IO) {

        val editingProduct = uiState.toEditProduct != null

        val newProduct = if (editingProduct) uiState.toEditProduct!!.copy(
            name = validatedName,
            price = validatedPrice,
            quantity = validatedQuantity,
            info = validatedInfo,
            categoryId = validatedCategory!!.id
        )
        else Product(
            listId,
            validatedCategory!!.id,
            validatedName,
            0,
            validatedPrice,
            validatedQuantity,
            validatedInfo
        )

        ProductRepository.addOrUpdateProduct(ValidatedProduct(newProduct))

        if (editingProduct) SuggestionProductRepository.updateSuggestionProduct(
            uiState.toEditProduct!!, ValidatedSuggestionProduct(newProduct)
        )
        else if (saveAsSuggestion) SuggestionProductRepository.updateOrAddProductAsSuggestion(
            ValidatedSuggestionProduct(newProduct)
        )

        uiState.finishActivity = true
        this@AddEditProductActivityViewModel.postData()

    }

    private fun loadEditingProduct() = viewModelScope.launch(IO) {
        productId?.let {
            val result = ProductRepository.getProduct(it)
            uiState.toEditProduct = result
            this@AddEditProductActivityViewModel.postData()
        }
    }

    private fun postData() = _uiStateLD.postValue(uiState)

    fun loadCategory(categoryId: String) = viewModelScope.launch(IO) {
        val result = CategoryRepository.getCategory(categoryId)
        uiState.toEditCategory = result.getOrThrow()
        this@AddEditProductActivityViewModel.postData()
    }

    fun loadSuggestions(term: String) = viewModelScope.launch(IO) {

        if (suggestions == null) suggestions = ProductRepository.getSuggestions()

        val filteredSuggestions = suggestions!!.filter {
            it.name.removeAccents().contains(term.removeAccents(), ignoreCase = true)
        }.sortedBy { it.name.length }

        val sub = filteredSuggestions.subList(0, min(filteredSuggestions.size, maxSuggestions))

        uiState.suggestions = sub to term
        this@AddEditProductActivityViewModel.postData()
    }

    fun loadNameSuggestions(term: String) = viewModelScope.launch(IO) {

        uiState.nameSuggestions = productNameSuggestion
            .getSuggestion(term, maxSuggestions).sortedBy { it.length }
        this@AddEditProductActivityViewModel.postData()
    }

    fun setup(listId: String, productId: String?) {

        if (this.listId != "null") return

        this.listId = listId
        this.productId = productId

        loadEditingProduct()
    }

    class UiState {

        var toEditProduct: Product? = null
        var toEditCategory: Category? = null
        var suggestions: Pair<List<Product>, String>? = null
        var nameSuggestions: List<String>? = null
        var errorMessage: String = ""
        var finishActivity = false

    }

}