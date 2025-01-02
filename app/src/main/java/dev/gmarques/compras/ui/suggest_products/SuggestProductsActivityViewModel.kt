package dev.gmarques.compras.ui.suggest_products

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.gmarques.compras.App
import dev.gmarques.compras.R
import dev.gmarques.compras.data.PreferencesHelper
import dev.gmarques.compras.data.PreferencesHelper.PrefsDefaultValue
import dev.gmarques.compras.data.PreferencesHelper.PrefsKeys
import dev.gmarques.compras.data.model.Product
import dev.gmarques.compras.data.repository.ProductRepository
import dev.gmarques.compras.domain.model.SelectableProduct
import dev.gmarques.compras.domain.utils.ExtFun.Companion.removeAccents
import dev.gmarques.compras.domain.utils.ListenerRegister
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class SuggestProductsActivityViewModel : ViewModel() {


    private lateinit var shopListId: String
    private var searchTerm: String = ""

    private lateinit var productsToBePosted: List<SelectableProduct>

    private var productsDatabaseListener: ListenerRegister? = null

    private var throttlingScope: CoroutineScope? = null

    private val _finishEventLD = MutableLiveData<Boolean>()
    val finishEventLD: LiveData<Boolean> get() = _finishEventLD

    private val _productsLD = MutableLiveData<List<SelectableProduct>>()
    val productsLD: LiveData<List<SelectableProduct>> get() = _productsLD

    private val _errorEventLD = MutableLiveData<String>()
    val errorEventLD: LiveData<String> get() = _errorEventLD


    private var sortAscending = PrefsDefaultValue.SORT_ASCENDING

    fun init(shopListId: String) {

        this.shopListId = shopListId

        viewModelScope.launch(IO) {
            loadSortPreferences()
            loadProducts()
        }
    }

    override fun onCleared() {
        throttlingScope?.cancel()
        super.onCleared()
    }

    private fun loadProducts() {
        productsDatabaseListener = ProductRepository.observeSuggestionProductUpdates { lists, error ->

            if (error != null) throw error

            if (lists != null) viewModelScope.launch {

                val filteredProducts = filterProducts(lists)
                val sortedProducts = sortProducts(filteredProducts)

                postDataWithThrottling(sortedProducts)
            }

            productsDatabaseListener!!.remove()
        }
    }

    /**
     * Aplica os termos de busca do ususario, caso hajam, aproveita o loop para carregar as categorias
     */
    private fun filterProducts(lists: List<Product>?): MutableList<SelectableProduct> {

        val filteredProducts = mutableListOf<SelectableProduct>()

        for (i in lists!!.indices) {

            val product = lists[i]

            if (searchTerm.isEmpty() || product.name.removeAccents().contains(searchTerm, true)) {
                filteredProducts.add(SelectableProduct(product, false, product.quantity))
            }
        }
        return filteredProducts
    }

    /**
     * Ordena os produtos da lista conforme configurado pelo usuario
     */
    private fun sortProducts(newData: MutableList<SelectableProduct>): List<SelectableProduct> {

        val sorted = newData.sortedWith(compareBy { it.product.name }).let { if (!sortAscending) it.reversed() else it }

        return sorted
    }

    /**
     *Aplica o  throttling mais simples que consegui pensar pra evitar atualizaçoes repetidas na UI
     */
    private fun postDataWithThrottling(newProducts: List<SelectableProduct>) {

        productsToBePosted = newProducts

        var delayMillis = 0L
        throttlingScope?.apply { delayMillis = 250; cancel() }
        throttlingScope = CoroutineScope(IO)
        throttlingScope!!.launch {
            delay(delayMillis)
            _productsLD.postValue(productsToBePosted)
            productsToBePosted = emptyList()
        }

    }

    /**
     * Funciona definindo o termo de busca numa variável global, removendo e nulificando o listener que observa o banco de dados e
     * no fim redefinindo, o listener para disparar uma atualização com os dados para então serem filtrados pelo viewmodel antes
     * de irem para a ui*/
    fun searchProduct(searchTerm: String) {

        this.searchTerm = searchTerm

        productsDatabaseListener?.remove()
        productsDatabaseListener = null

        loadProducts()
    }

    private fun loadSortPreferences() {
        val prefs = PreferencesHelper()
        sortAscending = prefs.getValue(PrefsKeys.SORT_ASCENDING, PrefsDefaultValue.SORT_ASCENDING)
    }

    fun saveProducts(currentList: List<SelectableProduct>) = viewModelScope.launch(IO) {
        var repeatedProductsCount = 0
        val currentShopListProductsNames = ProductRepository.getProducts(shopListId)

        currentList.forEach {

            if (it.isSelected) if (!currentShopListProductsNames.contains(it.product.name)) {
                val newProduct = it.product.copy(
                    shopListId = shopListId,
                    quantity = it.quantity,
                    hasBeenBought = false
                ).withNewId()

                saveProduct(it.product, newProduct)
            } else repeatedProductsCount++


        }
        finish(repeatedProductsCount)

    }

    private fun saveProduct(oldProduct: Product, newProduct: Product) {

        ProductRepository.addOrUpdateProduct(newProduct)
        ProductRepository.updateSuggestionProduct(oldProduct, newProduct)

    }

    private suspend fun finish(repeatedProductsCount: Int) {

        if (repeatedProductsCount > 0) {
            _errorEventLD.postValue(
                App.getContext().getString(R.string.Produtos_repetidos_x_nao_foram_incluidos_na_lista, repeatedProductsCount)
            )
            delay(2500)
        }

        _finishEventLD.postValue(true)
    }

    fun removeSuggestionProduct(product: Product) {
        ProductRepository.removeSuggestionProduct(product)
    }

}