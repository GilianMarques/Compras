package dev.gmarques.compras.presenter.products

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.gmarques.compras.data.PreferencesHelper
import dev.gmarques.compras.data.PreferencesHelper.PrefsDefaultValue
import dev.gmarques.compras.data.PreferencesHelper.PrefsKeys
import dev.gmarques.compras.data.model.Category
import dev.gmarques.compras.data.model.Establishment
import dev.gmarques.compras.data.model.Product
import dev.gmarques.compras.data.model.ShopList
import dev.gmarques.compras.data.repository.CategoryRepository
import dev.gmarques.compras.data.repository.EstablishmentRepository
import dev.gmarques.compras.data.repository.ProductRepository
import dev.gmarques.compras.data.repository.ShopListRepository
import dev.gmarques.compras.data.repository.SuggestionProductRepository
import dev.gmarques.compras.data.repository.model.ValidatedProduct
import dev.gmarques.compras.data.repository.model.ValidatedSuggestionProduct
import dev.gmarques.compras.domain.SortCriteria
import dev.gmarques.compras.domain.model.CategoryWithProductsStats
import dev.gmarques.compras.domain.model.ProductWithCategory
import dev.gmarques.compras.domain.utils.ExtFun.Companion.removeAccents
import dev.gmarques.compras.domain.utils.ListenerRegister
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class ProductsActivityViewModel : ViewModel() {


    /** categoria usada como filtro pelo usuario*/
    var filterCategory: Category? = null
        private set

    /** termo usado como filtro pelo usuario*/
    var searchTerm: String = ""
        private set


    var currentEstablishment: Establishment? = null
        private set

    /** pra saber se o usuario ja confirmou onde esta comprando*/
    var establishmentConfirmed = false

    /** mantem uma copia sempre atualizada das categorias no banco de dados na memoria do dispositivo*/
    private var categories: HashMap<String, Category>? = null


    private lateinit var uiState: UiState

    /** pra saber se o vm foi inicializado*/
    private var init = false

    private var throttlingJob: Job? = null

    private var productsDatabaseListener: ListenerRegister? = null
    private var shopListDatabaseListener: ListenerRegister? = null
    private var categoriesDatabaseListener: ListenerRegister? = null

    private val _uiStateLD = MutableLiveData<UiState>()
    val uiStateLD: LiveData<UiState> get() = _uiStateLD

    private val _establishmentEvent = MutableLiveData<Establishment?>()
    val establishmentEvent: LiveData<Establishment?> get() = _establishmentEvent

    fun init(shopListId: String) {

        if (init) return
        else init = true

        uiState = UiState()


        // lista temporaria pra permitir o carregamento imediato dos produtos, esta sera substituida apos o carrregamento do banco de dados
        uiState.shopList = ShopList(shopListId)
        viewModelScope.launch(IO) {
            loadSortPreferences()
            observeCategoriesUpdates()
            loadList(shopListId)
            loadCurrentEstablishment()
        }

    }

    override fun onCleared() {
        productsDatabaseListener?.remove()
        shopListDatabaseListener?.remove()
        categoriesDatabaseListener?.remove()
        throttlingJob?.cancel()

        super.onCleared()
    }

    /**
     * Mantem na memoria (hashmap) uma copia sempre atualizada de todas as categorias do banco de dados afim
     * de otimizar o desempenho do app ao carregar os produtos com as categorias
     *
     * Apos carregar as categorias chama a função para carregar os produtos
     */
    private fun observeCategoriesUpdates() {
        if (categoriesDatabaseListener != null) return

        categoriesDatabaseListener =
            CategoryRepository.observeCategoryUpdates { dbCategories, exception ->

                if (exception != null) throw exception
                if (dbCategories != null) {
                    categories = dbCategories.associateBy { it.id }.toMap(HashMap())
                    filterCategoriesWithProductsDataAndDispatchToUi(uiState.products.map { it.product })
                }
                observeProductsUpdates()

            }
    }

    /**
     * Filtra as categorias para que apenas as que existem na lista de compras (com base nos produtos da lista)
     * sejam enviados para a UI,tambem salva na categoria dados referentes aos produtos que a usam
     */
    private fun filterCategoriesWithProductsDataAndDispatchToUi(products: List<Product>) {

        requireNotNull(categories) { "Carregue as categorias antes de tentar filtrar os dados." }

        val noRepeatedCategories = HashMap<String, CategoryWithProductsStats>()

        for (i in products.indices) {

            val product = products[i]

            val categoryOnMap = noRepeatedCategories[product.categoryId]
            val totalProductsWithMatchingCategory = (categoryOnMap?.totalProducts ?: 0) + 1
            val totalBoughtProductsFromCategory =
                (categoryOnMap?.boughtProducts ?: 0) + if (product.hasBeenBought) 1 else 0

            noRepeatedCategories[product.categoryId] = CategoryWithProductsStats(
                categories!![product.categoryId]!!,
                totalProductsWithMatchingCategory,
                totalBoughtProductsFromCategory,
                product.categoryId == filterCategory?.id
            )

        }

        val updatedList = noRepeatedCategories.map { it.value }.toList()

        val sorted = updatedList.sortedWith(compareBy { it.category.name })
            .sortedWith(compareBy { it.category.position })
            .sortedWith(compareBy { it.boughtProducts == it.totalProducts })

        uiState.listCategories = sorted

        postDelayedData()

    }

    /**
     * Define um listener que dispara sempre que ha alterações nos itens relativos a lista de compras atual no banco de dados
     * garantindo que a UI fique sempre atualizada
     */
    private fun observeProductsUpdates() {
        if (productsDatabaseListener != null) return
        productsDatabaseListener =
            ProductRepository.observeProductUpdates(uiState.shopList.id) { products, error ->

                if (error != null) throw error
                if (products != null) viewModelScope.launch(IO) {

                    val filteredProductsWithCategories = filterProducts(products)
                    val sortedProductsWithPrices = sortProducts(filteredProductsWithCategories)

                    uiState.apply {
                        this.priceBought = sortedProductsWithPrices.priceBought
                        this.priceList = sortedProductsWithPrices.priceList
                        this.priceCategory = sortedProductsWithPrices.priceCategory
                        this.products = sortedProductsWithPrices.productsWithCategory
                    }

                    filterCategoriesWithProductsDataAndDispatchToUi(products)

                }
            }
    }

    /**
     * Aplica os termos de busca do usuario, caso hajam
     */
    private fun filterProducts(products: List<Product>?): ProductsWithPrices {

        requireNotNull(categories) { "Carregue as categorias antes de filtrar os produtos." }

        val filteredProducts = mutableListOf<ProductWithCategory>()
        var fullPrice = 0.0
        var boughtPrice = 0.0
        var categoryPrice = 0.0


        for (i in products!!.indices) {

            val product = products[i]

            val nameContainsSearchTermOrNoSearchTermSet =
                (searchTerm.isEmpty() || product.name.removeAccents().contains(searchTerm, true))

            val categoryMatchesFilterOrNoCategoryFilter = if (searchTerm.isNotEmpty()) true
            else (filterCategory == null || product.categoryId == filterCategory?.id)

            fullPrice += product.price * product.quantity

            if (product.hasBeenBought) boughtPrice += product.price * product.quantity
            if (product.categoryId == filterCategory?.id) categoryPrice += product.price * product.quantity

            if (nameContainsSearchTermOrNoSearchTermSet && categoryMatchesFilterOrNoCategoryFilter) {

                val prodWithCat = ProductWithCategory(
                    product,
                    categories!![product.categoryId]
                        ?: throw Exception("Carregue as categorias antes de carregar os produtos.\nProduto: $product\ncatgorias: $categories")
                )
                filteredProducts.add(prodWithCat)

            }

        }

        return ProductsWithPrices(filteredProducts, fullPrice, boughtPrice, categoryPrice)
    }

    /**
     * Ordena os produtos da lista conforme configurado pelo usuario
     */
    private fun sortProducts(filteredProductsWithPrices: ProductsWithPrices): ProductsWithPrices {

        val newData = filteredProductsWithPrices.productsWithCategory
        var sorted: List<ProductWithCategory>

        when (uiState.sortCriteria) {
            SortCriteria.NAME -> {
                sorted = newData.sortedWith(compareBy { it.product.name })

            }

            SortCriteria.CATEGORY -> {
                sorted = newData.sortedWith(compareBy { it.product.name })
                    .sortedWith(compareBy { it.category.name })
            }

            SortCriteria.CREATION_DATE -> {
                sorted = newData.sortedWith(compareBy { it.product.creationDate })
            }

            SortCriteria.POSITION -> {
                sorted = newData.sortedWith(compareBy { it.category.name })
                    .sortedWith(compareBy { it.product.position })
            }
        }
        if (uiState.sortCriteria != SortCriteria.POSITION) {
            sorted = if (!uiState.sortAscending) sorted.reversed() else sorted
        }
        sorted =
            sorted.sortedWith(compareBy { if (uiState.boughtProductsAtEnd) it.product.hasBeenBought else false })


        return filteredProductsWithPrices.copy(productsWithCategory = sorted)
    }

    /**
     *Aplica o  throttling mais simples que consegui pensar pra evitar atualizaçoes repetidas na UI
     * Essa funcionalidade se faz necessária principalmente por conta o drg and drop do rv de produtos.
     */
    private fun postDelayedData() {
        // TODO: aplicar throttling automaticamente com base na frequencia de chamadas
        val delayMillis = if (throttlingJob == null) 0L else 100L

        throttlingJob?.cancel()
        throttlingJob = Job()
        viewModelScope.launch(throttlingJob!!) {
            delay(delayMillis)
            Log.d("USUK", "ProductsActivityViewModel.".plus("postDataWithThrottling() "))
            _uiStateLD.value = uiState
        }

    }

    /**
     * Atualiza o produto no banco de dados e sua sugestao de produto relativa, caso exista
     */
    fun updateProductAsIs(updatedProduct: Product) = viewModelScope.launch(IO) {
        ProductRepository.addOrUpdateProduct(ValidatedProduct(updatedProduct))
        SuggestionProductRepository.updateSuggestionProduct(
            updatedProduct, ValidatedSuggestionProduct(updatedProduct)
        )
    }

    /**
     * Atualiza o produto com o valor recebido. Nao atualiza a sugestao de produto relativa, caso exista
     */
    fun updateProductBoughtState(product: Product, isBought: Boolean) {
        val newProduct =
            product.copy(
                hasBeenBought = isBought,
                boughtDate = System.currentTimeMillis(),
                establishmentId = currentEstablishment?.id
            )
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

    suspend fun tryToRemoveShopList(shopList: ShopList) = withContext(IO) {
        shopListDatabaseListener?.remove()
// TODO: otimizar isso com coroutines 
        val productsRemoved = ProductRepository.removeAllProductsFromShopList(shopList.id)
        if (productsRemoved) {
            ShopListRepository.removeShopList(shopList)
            return@withContext true
        } else return@withContext false
    }

    /**
     * Carrega a lista de compras, apenas a lista e seus atributos, nao inclui os produtos
     * Após carregar a lista chama a função para carregar as categorias
     */
    private fun loadList(shopListId: String) {
        shopListDatabaseListener =
            ShopListRepository.observeShopList(shopListId) { shopList, error ->
                if (error == null && shopList != null) {
                    uiState.shopList = shopList
                    Log.d(
                        "USUK",
                        "ProductsActivityViewModel.".plus("loadList() shopList = $shopList, error = $error")
                    )
                    postDelayedData()
                }
            }
    }

    fun removeProduct(product: Product) {
        ProductRepository.removeProduct(ValidatedProduct(product))
    }

    fun loadSortPreferences() {
        val prefs = PreferencesHelper()
        uiState.sortCriteria = SortCriteria.fromValue(
            prefs.getValue(
                PrefsKeys.SORT_CRITERIA, PrefsDefaultValue.SORT_CRITERIA.value
            )
        )!!
        uiState.sortAscending =
            prefs.getValue(PrefsKeys.SORT_ASCENDING, PrefsDefaultValue.SORT_ASCENDING)
        uiState.boughtProductsAtEnd = prefs.getValue(
            PrefsKeys.BOUGHT_PRODUCTS_AT_END, PrefsDefaultValue.BOUGHT_PRODUCTS_AT_END
        )
    }

    /**
     * Filtra os produtos com base na categoria selecionada
     * @param category a categoria usda no filtro, caso seja igual a categoria atualmente selecionada
     * o filtro é desativado
     */
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

    suspend fun loadCurrentEstablishment() {
        val lastEstablishmentId = PreferencesHelper().getValue(PrefsKeys.LAST_ESTABLISHMENT_USED, "")
        currentEstablishment =
            if (lastEstablishmentId.isNotEmpty()) EstablishmentRepository.getEstablishment(lastEstablishmentId) else null
        _establishmentEvent.postValue(currentEstablishment)
    }

    data class ProductsWithPrices(
        val productsWithCategory: List<ProductWithCategory>,
        val priceList: Double,
        val priceBought: Double,
        val priceCategory: Double,
    )

    class UiState {

        var sortCriteria: SortCriteria = PrefsDefaultValue.SORT_CRITERIA
        var sortAscending = PrefsDefaultValue.SORT_ASCENDING
        var boughtProductsAtEnd = PrefsDefaultValue.BOUGHT_PRODUCTS_AT_END

        var products = listOf<ProductWithCategory>()
        var listCategories = listOf<CategoryWithProductsStats>()
        lateinit var shopList: ShopList
        var priceList: Double = 0.0
        var priceCategory: Double = 0.0
        var priceBought: Double = 0.0

    }

}
