package dev.gmarques.compras.presenter.add_edit_product

import android.util.Log
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
import kotlinx.coroutines.launch
import kotlin.math.min

class AddEditProductActivityViewModel : ViewModel() {


    private var uiState = UiState()
        set(value) {
            field = value
            _mutableLiveData.postValue(field)
        }

    private val _mutableLiveData = MutableLiveData<UiState>()
    val uiStateLD: LiveData<UiState> get() = _mutableLiveData

    var validatedName: String = ""
    var validatedInfo: String = ""
    var validatedAnnotation: String = ""
    var validatedPrice: Double = -1.0
    var validatedQuantity: Int = -1
    var validatedCategory: Category? = null
    var productIsBought = false
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
            uiState.editingProduct == null || uiState.editingProduct!!.name != validatedName

        if (needCheckName) {
            val result = ProductRepository.getProductByName(validatedName, listId)

            if (result.getOrNull() == null) saveProduct(saveAsSuggestion)// o produto nao existe na lista, posso salvar
            else {
                val msg = String.format(
                    App.getContext().getString(R.string.X_ja_existe_na_lista), validatedName
                )
                uiState = uiState.copy(errorMessage = msg)
            }

        } else saveProduct(saveAsSuggestion)

    }

    private fun saveProduct(saveAsSuggestion: Boolean) = viewModelScope.launch(IO) {

        val editingProduct = uiState.editingProduct != null

        val newProduct = if (editingProduct) uiState.editingProduct!!.copy(
            name = validatedName,
            price = validatedPrice,
            quantity = validatedQuantity,
            info = validatedInfo,
            annotations = validatedAnnotation,
            categoryId = validatedCategory!!.id,
            hasBeenBought = productIsBought

        )
        else Product(
            name = validatedName,
            price = validatedPrice,
            quantity = validatedQuantity,
            info = validatedInfo,
            annotations = validatedAnnotation,
            shopListId = listId,
            categoryId = validatedCategory!!.id,
            position = 0,
            hasBeenBought = productIsBought
        )

        ProductRepository.addOrUpdateProduct(ValidatedProduct(newProduct))

        if (editingProduct) SuggestionProductRepository.updateSuggestionProduct(
            uiState.editingProduct!!, ValidatedSuggestionProduct(newProduct)
        )
        else if (saveAsSuggestion) SuggestionProductRepository.updateOrAddProductAsSuggestion(
            ValidatedSuggestionProduct(newProduct)
        )

        uiState = uiState.copy(finishActivity = true)

    }

    private fun loadEditingProduct() = viewModelScope.launch(IO) {
        productId?.let {
            val result = ProductRepository.getProduct(it)
            uiState = uiState.copy(editingProduct = result)
        }
    }

    fun loadCategory(categoryId: String) = viewModelScope.launch(IO) {
        val category = CategoryRepository.getCategory(categoryId)

        uiState = uiState.copy(editingCategory = category)
    }

    fun loadSuggestions(term: String) = viewModelScope.launch(IO) {

        if (suggestions == null) suggestions = ProductRepository.getSuggestions()

        val filteredSuggestions = suggestions!!.filter {
            it.name.removeAccents().contains(term.removeAccents(), ignoreCase = true)
        }.sortedBy { it.name.length }

        val sub = filteredSuggestions.subList(0, min(filteredSuggestions.size, maxSuggestions))

        try {
            uiState = uiState.copy(productsAndNamesSuggestions = sub.ifEmpty {
                productNameSuggestion.getSuggestion(term, maxSuggestions).sortedBy { it.length }
            })
        } catch (exception: Exception) {
            Log.d("USUK", "AddEditProductActivityViewModel.".plus("loadSuggestions() erro: $exception"))
        }

    }

    fun setup(listId: String, productId: String?) {

        if (this.listId != "null") return

        this.listId = listId
        this.productId = productId

        loadEditingProduct()
    }

    fun loadSuggestionCategory(product: Product) = viewModelScope.launch(IO) {
        val category = CategoryRepository.getCategory(product.categoryId)
        uiState = uiState.copy(suggestionProductAndCategory = product to category)

    }

    data class UiState(
        val editingProduct: Product? = null,
        val editingCategory: Category? = null,
        val suggestionProductAndCategory: Pair<Product, Category>? = null,
        val productsAndNamesSuggestions: List<Any> = emptyList(),
        val errorMessage: String = "",
        var productIsBought: Boolean = false,
        val finishActivity: Boolean = false,
    )

}
