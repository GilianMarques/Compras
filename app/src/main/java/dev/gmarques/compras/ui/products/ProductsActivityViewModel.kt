package dev.gmarques.compras.ui.products

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dev.gmarques.compras.data.data.ListenerRegister
import dev.gmarques.compras.data.data.model.Product
import dev.gmarques.compras.data.data.repository.ProductRepository


class ProductsActivityViewModel : ViewModel() {


    private var listenerRegister: ListenerRegister? = null
    private val _productsLiveData = MutableLiveData<List<Product>>()
    val productsLiveData: LiveData<List<Product>> get() = _productsLiveData




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
            if (error == null) lists.let { _productsLiveData.postValue(lists) }
            else Log.d("USUK", "ProductsActivityViewModel.observeUpdates: erro obtendo snapshot e $error")
        }

    }

}