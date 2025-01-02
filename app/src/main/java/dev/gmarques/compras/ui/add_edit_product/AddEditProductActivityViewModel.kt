package dev.gmarques.compras.ui.add_edit_product

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
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class AddEditProductActivityViewModel : ViewModel() {

    fun tryAndSaveProduct(saveAsSuggestion: Boolean) {

        // se adicionando produto ou se durante a ediçao o usuario trocar o nome do produto, preciso verificar se o novo nome ja nao existe na lista
        val needCheckName = !editingProduct || editingProductLD.value!!.name != validatedName

        if (needCheckName) {
            ProductRepository.getProductByName(validatedName, listId) { result ->
                if (result.isSuccess) {
                    if (result.getOrNull() == null) saveProduct(saveAsSuggestion)// o produto nao existe na lista, posso salvar
                    else {
                        val msg = String.format(App.getContext().getString(R.string.X_ja_existe_na_lista), validatedName)
                        _errorEventFlow.tryEmit(msg)
                    }
                } else {
                    val msg = String.format(
                        App.getContext().getString(R.string.Nao_foi_possivel_verificar_se_x_ja_existe_na_lista), validatedName
                    )
                    _errorEventFlow.tryEmit(msg)
                }
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

        ProductRepository.addOrUpdateProduct(newProduct)

        if (editingProduct) ProductRepository.updateSuggestionProduct(editingProductLD.value!!, newProduct)
        else if (saveAsSuggestion) ProductRepository.updateOrAddProductAsSuggestion(newProduct)

        _finishEventFlow.tryEmit(true)

    }

    suspend fun loadEditingProduct() = withContext(IO) {
        productId?.let {
            ProductRepository.getProduct(productId!!) { result ->

                if (result.isSuccess) _editingProductLD.postValue(result.getOrThrow())
                else Log.d(
                    "USUK", "AddProductActivityViewModel.loadProduct: erro obtendo produto do firebase${result.exceptionOrNull()}"
                )
            }
        }
    }

    fun loadCategory(categoryId: String) {
        CategoryRepository.getCategory(categoryId) { result ->

            if (result.isSuccess) _editingCategoryLD.postValue(result.getOrThrow())
            else Log.d(
                "USUK", "AddProductActivityViewModel.loadCategory: erro obtendo categoria do firebase${result.exceptionOrNull()}"
            )
        }
    }

    fun removeCategory(category: Category) {
        CategoryRepository.tryAndRemoveCategory(category) { result ->
            if (result.isFailure) _errorEventFlow.tryEmit(result.exceptionOrNull()!!.message!!)
        }
    }

    fun loadSuggestions(term: String) = viewModelScope.launch(IO) {
        if (suggestions == null) suggestions = ProductRepository.getSuggestions()

        val filteredSuggestions = suggestions!!.filter { it.name.contains(term, ignoreCase = true) }.toMutableList()


        _suggestionsLD.postValue(filteredSuggestions)
    }

    fun loadNameSuggestions(term: String) = viewModelScope.launch(IO) {

        val namesSuggestion = productNameSuggestion.getSuggestion(term)
        val filteredNamesSuggestion = namesSuggestion.filter { namesSuggestion.contains(it) }

        _nameSuggestionsLD.postValue(filteredNamesSuggestion)
    }

    private val productNameSuggestion = ProductNameSuggestion()
    private var suggestions: List<Product>? = null

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

    private val _suggestionsLD = MutableLiveData<List<Product>>()
    val suggestionsLD: LiveData<List<Product>> get() = _suggestionsLD

    private val _nameSuggestionsLD = MutableLiveData<List<String>>()
    val nameSuggestionsLD: LiveData<List<String>> get() = _nameSuggestionsLD

    private val _editingCategoryLD = MutableLiveData<Category>()
    val editingCategoryLD: LiveData<Category> get() = _editingCategoryLD

    private val _errorEventFlow = MutableSharedFlow<String>(replay = 1)
    val errorEventFlow = _errorEventFlow.asSharedFlow()

    private val _finishEventFlow = MutableSharedFlow<Any?>(replay = 1)
    val finishEventFlow = _finishEventFlow.asSharedFlow()

}