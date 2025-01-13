package dev.gmarques.compras.ui.products

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.gmarques.compras.data.PreferencesHelper
import dev.gmarques.compras.data.PreferencesHelper.PrefsDefaultValue
import dev.gmarques.compras.data.PreferencesHelper.PrefsKeys
import dev.gmarques.compras.data.model.Category
import dev.gmarques.compras.data.model.Product
import dev.gmarques.compras.data.model.ShopList
import dev.gmarques.compras.data.repository.CategoryRepository
import dev.gmarques.compras.data.repository.ProductRepository
import dev.gmarques.compras.data.repository.ShopListRepository
import dev.gmarques.compras.data.repository.SuggestionProductRepository
import dev.gmarques.compras.data.repository.model.ValidatedProduct
import dev.gmarques.compras.data.repository.model.ValidatedShopList
import dev.gmarques.compras.data.repository.model.ValidatedSuggestionProduct
import dev.gmarques.compras.domain.SortCriteria
import dev.gmarques.compras.domain.model.ProductWithCategory
import dev.gmarques.compras.domain.utils.ExtFun.Companion.removeAccents
import dev.gmarques.compras.domain.utils.ListenerRegister
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class ProductsActivityViewModel : ViewModel() {


    var filterCategory: Category? = null
    private var categories: HashMap<String, Category>? = null
    private var searchTerm: String = ""
    private lateinit var productsToBePosted: List<ProductWithCategory>

    private lateinit var pricesToBePosted: Pair<Double, Double>

    private var productsDatabaseListener: ListenerRegister? = null
    private var shopListDatabaseListener: ListenerRegister? = null
    private var categoriesDatabaseListener: ListenerRegister? = null

    private var throttlingScope: CoroutineScope? = null

    private val _productsLD = MutableLiveData<List<ProductWithCategory>>()
    val productsLD: LiveData<List<ProductWithCategory>> get() = _productsLD

    private val _listCategoriesLD = MutableLiveData<List<Category>?>()
    val listCategoriesLD: LiveData<List<Category>?> get() = _listCategoriesLD

    private val _shopListLD = MutableLiveData<ShopList?>()
    val shopListLD: LiveData<ShopList?> get() = _shopListLD

    private val _pricesLD = MutableLiveData<Pair<Double, Double>>()
    val pricesLD: LiveData<Pair<Double, Double>> get() = _pricesLD

    private var sortCriteria: SortCriteria = PrefsDefaultValue.SORT_CRITERIA
    private var sortAscending = PrefsDefaultValue.SORT_ASCENDING
    private var boughtProductsAtEnd = PrefsDefaultValue.BOUGHT_PRODUCTS_AT_END

    fun init(shoplistId: String) {

        // lista temporaria pra permitir o carregamento imediato dos produtos, esta sera substituida apos o carrregamento do banco de dados
        _shopListLD.value = ShopList(shoplistId)

        viewModelScope.launch(IO) {
            loadSortPreferences()
            observeCategoriesUpdates()
            loadList(shoplistId)
        }
    }

    /**
     * Mantem na memoria (hashmap) uma copia sempre atualizada de todas as categorias do banco de dados afim
     * de otimizar o desempenho do app ao carregar os produtos com as categorias
     *
     * Apos carregar as categorias chama a função para carregar os produtos
     */
    private fun observeCategoriesUpdates() {
        if (categoriesDatabaseListener != null) return

        categoriesDatabaseListener = CategoryRepository.observeCategoryUpdates { dbCategories, exception ->

            if (exception != null) throw exception
            if (dbCategories != null) {
                categories = dbCategories.associateBy { it.id }.toMap(HashMap())

            }
            observeProductsUpdates()

        }
    }

    override fun onCleared() {
        productsDatabaseListener?.remove()
        shopListDatabaseListener?.remove()
        categoriesDatabaseListener?.remove()
        throttlingScope?.cancel()

        super.onCleared()
    }

    /**
     * Define um listener que dispara sempre que ha alterações nos itens relativos a lista de compras atual no banco de dados
     * garantindo que a UI fique sempre atualizada
     */
    private fun observeProductsUpdates() {
        if (productsDatabaseListener != null) return
        productsDatabaseListener = ProductRepository.observeProductUpdates(_shopListLD.value!!.id) { products, error ->

            if (error != null) throw error
            if (products != null) viewModelScope.launch(IO) {

                val filteredProductsWithCategories = filterProductsAndLoadCategory(products)
                val sortedProductsWithPrices = sortProducts(filteredProductsWithCategories)

                updateCategoriesFilter(products)
                postDataWithThrottling(sortedProductsWithPrices)
            }
        }
    }

    private fun updateCategoriesFilter(products: List<Product>) {
        val noRepeatCategories = mutableSetOf<Category>()
        products.forEach { noRepeatCategories.add(categories?.get(it.categoryId)!!) }
        val updatedList = noRepeatCategories.toList()
        if (updatedList != _listCategoriesLD.value) _listCategoriesLD.postValue(noRepeatCategories.toList())
    }

    /**
     * Aplica os termos de busca do ususario, caso hajam, aproveita o loop para carregar as categorias
     */
    private fun filterProductsAndLoadCategory(lists: List<Product>?): ProductsWithPrices {

        requireNotNull(categories) { "Carregue as categorias antes de carregar os produtos." }

        val filteredProducts = mutableListOf<ProductWithCategory>()
        var fullPrice = 0.0
        var boughtPrice = 0.0

        for (i in lists!!.indices) {

            val product = lists[i]

            val nameContainsSearchTermOrNoSearchTermDefined =
                (searchTerm.isEmpty() || product.name.removeAccents().contains(searchTerm, true))
            val categoryMatchesFilterOrNoCategoryFilter =
                (filterCategory == null || product.categoryId == filterCategory?.id)

            if (nameContainsSearchTermOrNoSearchTermDefined && categoryMatchesFilterOrNoCategoryFilter) {

                fullPrice += product.price * product.quantity
                if (product.hasBeenBought) boughtPrice += product.price * product.quantity

                val prodWithCat = ProductWithCategory(
                    product,
                    categories!![product.categoryId]
                        ?: throw Exception("Carregue as categorias antes de carregar os produtos.\nProduto: $product\ncatgorias: $categories")
                )
                filteredProducts.add(prodWithCat)
            }
        }
        return ProductsWithPrices(filteredProducts, fullPrice, boughtPrice)
    }

    /**
     * Ordena os produtos da lista conforme configurado pelo usuario
     */
    private fun sortProducts(filteredProductsWithPrices: ProductsWithPrices): ProductsWithPrices {
        val newData = filteredProductsWithPrices.productsWithCategory

        var sorted = newData.sortedWith(
            compareBy({ if (boughtProductsAtEnd) it.product.hasBeenBought else false }, // Produtos comprados no final
                { if (sortCriteria == SortCriteria.NAME) it.product.name else null },
                { if (sortCriteria == SortCriteria.CATEGORY) it.category.name else null },
                { if (sortCriteria == SortCriteria.CREATION_DATE) it.product.creationDate else null },
                { if (sortCriteria == SortCriteria.POSITION) it.product.position else null })
        ).let {
            if (!sortAscending) it.reversed() else it
        }
        sorted = sorted.sortedWith(compareBy { if (boughtProductsAtEnd) it.product.hasBeenBought else false })

        return filteredProductsWithPrices.copy(productsWithCategory = sorted)
    }

    /**
     *Aplica o  throttling mais simples que consegui pensar pra evitar atualizaçoes repetidas na UI
     */
    private fun postDataWithThrottling(sortedProductsWithPrices: ProductsWithPrices) {

        productsToBePosted = sortedProductsWithPrices.productsWithCategory
        pricesToBePosted = sortedProductsWithPrices.fullPrice to sortedProductsWithPrices.boughtPrice


        var delayMillis = 0L
        throttlingScope?.apply { delayMillis = 250; cancel() }
        throttlingScope = CoroutineScope(IO)
        throttlingScope!!.launch {
            delay(delayMillis)
            _productsLD.postValue(productsToBePosted)
            _pricesLD.postValue(pricesToBePosted)
            productsToBePosted = emptyList()
            pricesToBePosted = Pair(0.0, 0.0)
        }

    }

    /**
     * Atualiza o produto no banco de dados e sua sugestao de produto relativa, caso exista
     */
    fun updateProductAsIs(updatedProduct: Product) = viewModelScope.launch(IO) {
        ProductRepository.addOrUpdateProduct(ValidatedProduct(updatedProduct))
        SuggestionProductRepository.updateSuggestionProduct(updatedProduct, ValidatedSuggestionProduct(updatedProduct))
    }

    /**
     * Atualiza o produto com o valor recebido. Nao atualiza a sugestao de produto relativa, caso exista
     */
    fun updateProductBoughtState(product: Product, isBought: Boolean) {
        val newProduct = product.copy(hasBeenBought = isBought)
        ProductRepository.addOrUpdateProduct(ValidatedProduct(newProduct))
    }

    /**
     * Funciona definindo o termo de busca numa variável global, removendo e nulificando o listener que observa o banco de dados e
     * no fim redefinindo, o listener para disparar uma atualização com os dados para então serem filtrados pelo viewmodel antes
     * de irem para a ui*/
    fun searchProduct(term: String) {

        val searchTerm = term.removeAccents()

        if (this.searchTerm == searchTerm) return

        this.searchTerm = searchTerm

        productsDatabaseListener?.remove()
        productsDatabaseListener = null

        observeProductsUpdates()
    }

    fun addOrUpdateShopList(shopList: ShopList) {
        ShopListRepository.addOrAttShopList(ValidatedShopList(shopList))
    }

    suspend fun removeShopList(shopList: ShopList) = withContext(IO) {
        shopListDatabaseListener?.remove()

        ProductRepository.removeAllProductsFromList(shopList.id)
        ShopListRepository.removeShopList(shopList)
    }

    /**
     * Carrega a lista de compras, apenas a lista e seus atributos, nao inclui os produtos
     * Após carregar a lista chama a função para carregar as categorias
     */
    private fun loadList(shoplistId: String) {
        shopListDatabaseListener = ShopListRepository.observeShopList(shoplistId) { shopList, error ->
            if (error == null && shopList != null) {
                _shopListLD.postValue(shopList)
            }
        }
    }

    fun removeProduct(product: Product) {
        ProductRepository.removeProduct(ValidatedProduct(product))
    }

    fun loadSortPreferences() {
        val prefs = PreferencesHelper()
        sortCriteria = SortCriteria.fromValue(prefs.getValue(PrefsKeys.SORT_CRITERIA, PrefsDefaultValue.SORT_CRITERIA.value))!!
        sortAscending = prefs.getValue(PrefsKeys.SORT_ASCENDING, PrefsDefaultValue.SORT_ASCENDING)
        boughtProductsAtEnd = prefs.getValue(PrefsKeys.BOUGHT_PRODUCTS_AT_END, PrefsDefaultValue.BOUGHT_PRODUCTS_AT_END)
    }

    fun filterByCategory(category: Category) {

        if (this.filterCategory == category) filterCategory = null
        else this.filterCategory = category

        productsDatabaseListener?.remove()
        productsDatabaseListener = null

        observeProductsUpdates()
    }

    fun updateProductPosition(product: Product, newIndex: Int) {
        val newProduct = product.copy(position = newIndex)
        ProductRepository.addOrUpdateProduct(ValidatedProduct(newProduct))
    }

    data class ProductsWithPrices(
        val productsWithCategory: List<ProductWithCategory>,
        val fullPrice: Double,
        val boughtPrice: Double,
    )

}