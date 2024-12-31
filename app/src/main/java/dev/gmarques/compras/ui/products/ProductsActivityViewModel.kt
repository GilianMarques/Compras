package dev.gmarques.compras.ui.products

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dev.gmarques.compras.data.model.Product
import dev.gmarques.compras.data.model.ShopList
import dev.gmarques.compras.data.repository.ProductRepository
import dev.gmarques.compras.data.repository.ShopListRepository
import dev.gmarques.compras.utils.ListenerRegister
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class ProductsActivityViewModel : ViewModel() {


    private var searchTerm: String = ""

    private lateinit var productsToBePosted: List<Product>
    private lateinit var pricesToBePosted: Pair<Double, Double>

    private var productsDatabaseListener: ListenerRegister? = null
    private var shopListDatabaseListener: ListenerRegister? = null

    private var scope: CoroutineScope? = null

    private val _productsLD = MutableLiveData<List<Product>>()
    val productsLD: LiveData<List<Product>> get() = _productsLD

    private val _shopListLD = MutableLiveData<ShopList>()
    val shopListLD: LiveData<ShopList> get() = _shopListLD

    private val _pricesLD = MutableLiveData<Pair<Double, Double>>()
    val pricesLD: LiveData<Pair<Double, Double>> get() = _pricesLD


    override fun onCleared() {
        productsDatabaseListener?.remove()
        shopListDatabaseListener?.remove()
        scope?.cancel()

        super.onCleared()
    }

    fun observeProducts() {
        if (_shopListLD.value == null) throw NullPointerException("Carrege a lista primeiro, só entao chame a função para carregar os produtos")
        if (productsDatabaseListener != null) return
        observeUpdates()
    }

    private fun observeUpdates() {
        productsDatabaseListener = ProductRepository.observeProductUpdates(_shopListLD.value!!.id) { lists, error ->
            if (error == null) lists.let {

                val filteredProducts = mutableListOf<Product>()
                var fullPrice = 0.0
                var boughtPrice = 0.0

                for (i in lists!!.indices) {

                    val product = lists[i]

                    if (searchTerm.isEmpty() || product.name.contains(searchTerm, true)) {
                        fullPrice += product.price * product.quantity
                        if (product.hasBeenBought) boughtPrice += product.price * product.quantity

                        filteredProducts.add(product)
                    }
                }

                postDataWithThrottling(filteredProducts, fullPrice to boughtPrice)
            }
        }
    }

    /**
     *Aplica o  throttling mais simples que consegui pensar pra evitar atualizaçoes repetidas na UI
     */
    private fun postDataWithThrottling(products: List<Product>, prices: Pair<Double, Double>) {

        Log.d("USUK", "ProductsActivityViewModel.postDataWithThrottling: called ")

        productsToBePosted = products
        pricesToBePosted = prices


        var delayMillis = 0L
        scope?.apply { delayMillis = 250; cancel() }
        scope = CoroutineScope(IO)
        scope!!.launch {
            delay(delayMillis)
            Log.d("USUK", "ProductsActivityViewModel.postDataWithThrottling: posting data ")
            _productsLD.postValue(productsToBePosted)
            _pricesLD.postValue(pricesToBePosted)
            productsToBePosted = emptyList()
            pricesToBePosted = Pair(0.0, 0.0)
        }

    }

    fun updateProductAsIs(updatedProduct: Product) {
        ProductRepository.addOrUpdateProduct(updatedProduct)
    }

    fun updateProductBoughtState(product: Product, isBought: Boolean) {
        val newProduct = product.copy(hasBeenBought = isBought)
        ProductRepository.addOrUpdateProduct(newProduct)
    }

    fun searchProduct(searchTerm: String) {
        this.searchTerm = searchTerm
        productsDatabaseListener?.remove()
        productsDatabaseListener = null
        observeProducts()

    }

    fun addOrUpdateShopList(shopList: ShopList) {
        ShopListRepository.addOrAttShopList(shopList)
    }

    fun removeShopList(shopList: ShopList) {
        ShopListRepository.removeShopList(shopList)
    }

    /**
     * Carrega a lista de compras, apenas a lista e seus atributos, nao inclui os produtos
     */
    fun loadList(shoplistId: String) {
        shopListDatabaseListener = ShopListRepository.observeList(shoplistId) { shopList, error ->
            if (error == null) shopList.let {
                _shopListLD.postValue(shopList!!)
            }
        }
    }

    fun removeProduct(product: Product) {
        ProductRepository.removeProduct(product)
    }

    /**
     * Força os dados a serem atualizados na UI sem recarrega-los do banco de dados
     * Isso é util pra quando o ususario troca a forma que o app deve ordenar os itens
     */
    fun repostProductData() {
        postDataWithThrottling(_productsLD.value!!, _pricesLD.value!!)
    }


}