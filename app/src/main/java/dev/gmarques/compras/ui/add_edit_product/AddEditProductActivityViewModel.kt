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
import dev.gmarques.compras.data.repository.model.ValidatedCategory
import dev.gmarques.compras.data.repository.model.ValidatedProduct
import dev.gmarques.compras.domain.utils.ExtFun.Companion.removeAccents
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlin.math.min


class AddEditProductActivityViewModel : ViewModel() {

    fun tryAndSaveProduct(saveAsSuggestion: Boolean) = viewModelScope.launch(IO) {

        // se adicionando produto ou se durante a ediçao o usuario trocar o nome do produto, preciso verificar se o novo nome ja nao existe na lista
        val needCheckName = !editingProduct || editingProductLD.value!!.name != validatedName

        if (needCheckName) {
            val result = ProductRepository.getProductByName(validatedName, listId)

            if (result.getOrNull() == null) saveProduct(saveAsSuggestion)// o produto nao existe na lista, posso salvar
            else {
                val msg = String.format(App.getContext().getString(R.string.X_ja_existe_na_lista), validatedName)
                _errorEventLD.postValue(msg)
            }

        } else saveProduct(saveAsSuggestion)

    }

    private fun saveProduct(saveAsSuggestion: Boolean) = viewModelScope.launch(IO) {

        val newProduct = if (editingProduct) _editingProductLD.value!!.copy(
            name = validatedName,
            price = validatedPrice,
            quantity = validatedQuantity,
            info = validatedInfo,
            categoryId = validatedCategory!!.id
        )
        else Product(listId, validatedCategory!!.id, validatedName, 0, validatedPrice, validatedQuantity, validatedInfo)

        ProductRepository.addOrUpdateProduct(ValidatedProduct(newProduct))

        if (editingProduct) ProductRepository.updateSuggestionProduct(editingProductLD.value!!, ValidatedProduct(newProduct))
        else if (saveAsSuggestion) ProductRepository.updateOrAddProductAsSuggestion(ValidatedProduct(newProduct))

        _finishEventLD.postValue(true)

    }

    fun loadEditingProduct() = viewModelScope.launch(IO) {
        productId?.let {
            val result = ProductRepository.getProduct(productId!!)
            _editingProductLD.postValue(result.getOrThrow())
        }
    }

    fun loadCategory(categoryId: String) = viewModelScope.launch(IO) {
        val result = CategoryRepository.getCategory(categoryId)
        _editingCategoryLD.postValue(result.getOrThrow())
    }

    fun removeCategory(category: Category) = viewModelScope.launch(IO) {
        val result = CategoryRepository.tryAndRemoveCategory(ValidatedCategory(category))
        if (result.isFailure) _errorEventLD.postValue(result.exceptionOrNull()!!.message!!)
    }

    fun loadSuggestions(term: String) = viewModelScope.launch(IO) {
        if (suggestions == null) suggestions = ProductRepository.getSuggestions()

        val filteredSuggestions = suggestions!!
            .filter { it.name.removeAccents().contains(term.removeAccents(), ignoreCase = true) }
            .sortedBy { it.name.length }

        val sub = filteredSuggestions.subList(0, min(filteredSuggestions.size, maxSuggestions))

        _suggestionsLD.postValue(sub to term)
    }

    fun loadNameSuggestions(term: String) = viewModelScope.launch(IO) {
        _nameSuggestionsLD.postValue(productNameSuggestion.getSuggestion(term, maxSuggestions).sortedBy { it.length })
    }

    private val productNameSuggestion = ProductNameSuggestion()
    private var suggestions: List<Product>? = null

    private val maxSuggestions = 5

    var canLoadSuggestion: Boolean = true
    var editingProduct: Boolean = false

    var productId: String? = null
    var listId: String = "-1"
    var validatedName: String = ""
    var validatedInfo: String = ""
    var validatedPrice: Double = -1.0
    var validatedQuantity: Int = -1
    var validatedCategory: Category? = null


    private val _editingProductLD = MutableLiveData<Product>()
    val editingProductLD: LiveData<Product> get() = _editingProductLD

    private val _suggestionsLD = MutableLiveData<Pair<List<Product>, String>>()
    val suggestionsLD: LiveData<Pair<List<Product>, String>> get() = _suggestionsLD

    private val _nameSuggestionsLD = MutableLiveData<List<String>>()
    val nameSuggestionsLD: LiveData<List<String>> get() = _nameSuggestionsLD

    private val _editingCategoryLD = MutableLiveData<Category>()
    val editingCategoryLD: LiveData<Category> get() = _editingCategoryLD

    private val _errorEventLD = MutableLiveData<String>()
    val errorEventLD: LiveData<String> get() = _errorEventLD

    private val _finishEventLD = MutableLiveData<Boolean>()
    val finishEventLD: LiveData<Boolean> get() = _finishEventLD

}