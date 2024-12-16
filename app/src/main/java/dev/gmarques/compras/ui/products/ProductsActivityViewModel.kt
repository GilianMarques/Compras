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
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class ProductsActivityViewModel : ViewModel() {


    private lateinit var dataToPost: List<Product>
    private var listenerRegister: ListenerRegister? = null
    private val _productsLiveData = MutableLiveData<List<Product>>()
    val productsLiveData: LiveData<List<Product>> get() = _productsLiveData
    var awaiting = false

    override fun onCleared() {
        listenerRegister?.remove()
        super.onCleared()
    }


    fun observeProducts(shopListId: Long) {
        if (listenerRegister != null) return
        observeUpdates(shopListId)
    }

    private fun observeUpdates(shopListId: Long) {

        listenerRegister = ProductRepository.observeProductUpdates(shopListId) { lists, error ->
            if (error == null) lists.let {
                /* Uma vez que o usuário mova um produto da posição 0 para a posição 1/2/3... caso os produtos não tenham índice
                definido, o  item movido irá parar no final da lista uma vez que todos os outros índices são zero ou -1,
                 para evitar esse  comportamento é necessário definir um índice de acordo com a posição do produto na lista*/

                val mutableData = lists!!.toMutableList()
                for (i in lists.indices) {
                    var product = lists[i]

                    if (product.position == -1) product = product.copy(position = i)
                    mutableData[i] = product
                }

                postDataWithThrottling(mutableData)
            }
            else Log.d("USUK", "ProductsActivityViewModel.observeUpdates: erro obtendo snapshot e $error")
        }

    }

    /**
     *Aplica o  throttling mais simples que consegui pensar pra evitar atualizaçoes repetidas na UI
     *
     */
    private fun postDataWithThrottling(newData: MutableList<Product>) {

        dataToPost = newData.toList()
        if (!awaiting) {
            awaiting = true

            CoroutineScope(IO).launch {
                delay(500)
                withContext(Main) {
                    _productsLiveData.postValue(dataToPost)
                    awaiting = false
                    dataToPost = emptyList()
                }
            }
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
        val newProduct = product.copy(isBought = isBought)
        ProductRepository.addOrUpdateProduct(newProduct)
    }

}