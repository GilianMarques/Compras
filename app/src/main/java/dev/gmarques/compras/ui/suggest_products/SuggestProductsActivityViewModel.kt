package dev.gmarques.compras.ui.suggest_products

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.gmarques.compras.data.PreferencesHelper
import dev.gmarques.compras.data.PreferencesHelper.PrefsDefaultValue
import dev.gmarques.compras.data.PreferencesHelper.PrefsKeys
import dev.gmarques.compras.data.model.Product
import dev.gmarques.compras.data.repository.ProductRepository
import dev.gmarques.compras.data.repository.SuggestionProductRepository
import dev.gmarques.compras.data.repository.model.ValidatedProduct
import dev.gmarques.compras.data.repository.model.ValidatedSuggestionProduct
import dev.gmarques.compras.domain.model.SelectableProduct
import dev.gmarques.compras.domain.utils.ExtFun.Companion.removeAccents
import dev.gmarques.compras.domain.utils.ListenerRegister
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class SuggestProductsActivityViewModel : ViewModel() {


    private lateinit var shopListId: String
    private var searchTerm: String = ""

    // necessario para que os dados de seleçao não se percam caso haja uma mudança de tela ou atualização no banco de dados
    private val updatedSelectionData = HashMap<String, Pair<Boolean, Int>>()

    private lateinit var productsToBePosted: List<SelectableProduct>

    private var productsDatabaseListener: ListenerRegister? = null

    private var databaseThrottlingScope: CoroutineScope? = null

    private val _productsLD = MutableLiveData<List<SelectableProduct>>()
    val productsLD: LiveData<List<SelectableProduct>> get() = _productsLD

    private val _errorEventLD = MutableLiveData<String>()
    val errorEventLD: LiveData<String> get() = _errorEventLD


    private var sortAscending = PrefsDefaultValue.SORT_ASCENDING

    fun init(shopListId: String) {

        this.shopListId = shopListId

        viewModelScope.launch(IO) {
            loadSortPreferences()
            observeProductsUpdates()
        }
    }

    override fun onCleared() {
        databaseThrottlingScope?.cancel()
        productsDatabaseListener?.remove()
        super.onCleared()
    }

    private fun observeProductsUpdates() {
        if (productsDatabaseListener != null) return
        productsDatabaseListener = SuggestionProductRepository.observeSuggestionProductUpdates { products, error ->

            if (error != null) throw error

            if (products != null) viewModelScope.launch {

                val filteredProducts = filterAndCreateSelectableProducts(products)
                val sortedProducts = sortProducts(filteredProducts)

                postDataWithThrottling(sortedProducts)
            }

        }
    }

    /**
     * Aplica os termos de busca do ususario, caso hajam, aproveita o loop para carregar as categorias
     */
    private fun filterAndCreateSelectableProducts(lists: List<Product>?): MutableList<SelectableProduct> {

        val filteredProducts = mutableListOf<SelectableProduct>()

        for (i in lists!!.indices) {

            val product = lists[i]

            if (searchTerm.isEmpty() || product.name.removeAccents().contains(searchTerm, true)) {

                val (selected, quantity) = updatedSelectionData[product.id] ?: (false to product.quantity)
                filteredProducts.add(SelectableProduct(product, selected, quantity))
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
        databaseThrottlingScope?.apply { delayMillis = 250; cancel() }
        databaseThrottlingScope = CoroutineScope(IO)
        databaseThrottlingScope!!.launch {
            delay(delayMillis)
            _productsLD.postValue(productsToBePosted)
            productsToBePosted = emptyList()
        }

    }

    /**
     * Funciona definindo o termo de busca numa variável global, removendo e nulificando o listener que observa o banco de dados e
     * no fim redefinindo, o listener para disparar uma atualização com os dados para então serem filtrados pelo viewmodel antes
     * de irem para a ui*/
    fun searchProduct(term: String) {

        val searchTerm = term.removeAccents()

        if (this.searchTerm == searchTerm) return
        this.searchTerm = searchTerm

        reObserveProductsUpdates()
    }

    private fun reObserveProductsUpdates() {
        productsDatabaseListener?.remove()
        productsDatabaseListener = null

        observeProductsUpdates()
    }

    private fun loadSortPreferences() {
        val prefs = PreferencesHelper()
        sortAscending = prefs.getValue(PrefsKeys.SORT_ASCENDING, PrefsDefaultValue.SORT_ASCENDING)
    }

    fun saveProducts() = CoroutineScope(Job()).launch(IO) {
        var repeatedProductsCount = 0
        val currentShopListProductsNames = ProductRepository.getProducts(shopListId)

        updatedSelectionData.keys.forEach { key ->
            delay(500)
            val (selected, quantity) = updatedSelectionData[key] ?: (false to -1)

            if (selected) {
                val product = SuggestionProductRepository.getSuggestionProduct(key)

                if (currentShopListProductsNames.contains(product.name)) repeatedProductsCount++
                else {
                    val newProduct = product.copy(
                        shopListId = shopListId, quantity = quantity, hasBeenBought = false
                    )
                    saveProduct(product, newProduct)
                }
            }
        }
    }

    private suspend fun saveProduct(oldProduct: Product, newProduct: Product) {
        ProductRepository.addOrUpdateProduct(ValidatedProduct(newProduct))
        SuggestionProductRepository.updateSuggestionProduct(oldProduct, ValidatedSuggestionProduct(newProduct))
    }



    fun removeSuggestionProduct(product: Product) {
        SuggestionProductRepository.removeSuggestionProduct(ValidatedSuggestionProduct(product))
        reObserveProductsUpdates()
    }

    fun updateSelectionData(sp: SelectableProduct) {
        updatedSelectionData[sp.product.id] = sp.isSelected to sp.quantity
    }

}