package dev.gmarques.compras.ui.products

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dev.gmarques.compras.data.data.ListenerRegister
import dev.gmarques.compras.data.data.model.Product
import dev.gmarques.compras.data.data.repository.ProductRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class ProductsActivityViewModel : ViewModel() {


    var shopListId: Long = -1
    private var searchTerm: String = ""
    private lateinit var productsToBePosted: List<Product>
    private lateinit var pricesToBePosted: Pair<Double, Double>
    private var firstLoad = true
    private var listenerRegister: ListenerRegister? = null

    private var scope = CoroutineScope(IO)

    private val _productsLiveData = MutableLiveData<List<Product>>()
    val productsLiveData: LiveData<List<Product>> get() = _productsLiveData


    private val _pricesLiveData = MutableLiveData<Pair<Double, Double>>()
    val pricesLiveData: LiveData<Pair<Double, Double>> get() = _pricesLiveData


    override fun onCleared() {
        listenerRegister?.remove()
        scope.cancel()

        super.onCleared()
    }

    fun observeProducts() {
        if (listenerRegister != null) return
        observeUpdates()
    }

    private fun observeUpdates() {

        if (shopListId == -1L) throw IllegalStateException("shopListId não foi inicializado")

        listenerRegister = ProductRepository.observeProductUpdates(shopListId) { lists, error ->
            if (error == null) lists.let {

                /* Uma vez que o usuário mova um produto da posição 0 para a posição 1/2/3... caso os produtos não tenham índice
                  definido, o  item movido irá parar no final da lista uma vez que todos os outros índices são zero ou -1,
                   para evitar esse  comportamento é necessário definir um índice de acordo com a posição do produto na lista*/

                val filteredProducts = mutableListOf<Product>()
                var fullPrice = 0.0
                var bougthPrice = 0.0

                for (i in lists!!.indices) {

                    var product = lists[i]
                    if (product.position == -1) product = product.copy(position = i)

                    if (searchTerm.isEmpty() || product.name.contains(searchTerm, true)) {
                        fullPrice += product.price * product.quantity
                        if (product.hasBeenBought) bougthPrice += product.price * product.quantity

                        filteredProducts.add(product)
                    }
                }

                postDataWithThrottling(filteredProducts, fullPrice to bougthPrice)
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

        scope.cancel().also { scope = CoroutineScope(IO) }
        scope.launch {
            delay(if (firstLoad) 0 else 250)
            Log.d("USUK", "ProductsActivityViewModel.postDataWithThrottling: posting data ")
            _productsLiveData.postValue(productsToBePosted)
            _pricesLiveData.postValue(pricesToBePosted)
            firstLoad = false
            productsToBePosted = emptyList()
            pricesToBePosted = Pair(0.0, 0.0)
        }
    }

    fun updateProductPosition(product: Product, newIndex: Int) {
        val newProduct = product.copy(position = newIndex)
        ProductRepository.addOrUpdateProduct(newProduct)
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
        listenerRegister?.remove()
        listenerRegister = null
        observeProducts()

    }


}