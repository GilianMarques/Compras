package dev.gmarques.compras.ui.main

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dev.gmarques.compras.data.data.ListenerRegister
import dev.gmarques.compras.data.data.model.ShopList
import dev.gmarques.compras.data.data.repository.ShopListRepository


class MainActivityViewModel : ViewModel() {

    init {
        observeUpdates()
    }

    private fun observeUpdates() {

        listenerRegister = ShopListRepository.observeListUpdates { lists, error ->
            if (error == null) lists.let { _listsLiveData.postValue(lists) }
            else Log.d("USUK", "ShopListRepository.getAllLists: erro obtendo snapshot e $error")
        }

    }

    fun addOrEditShopList(shopList: ShopList) {
        ShopListRepository.addOrAttShopList(shopList)
    }

    override fun onCleared() {
        listenerRegister.remove()
        super.onCleared()
    }

    fun removeShopList(shopList: ShopList) {
        ShopListRepository.removeShopList(shopList)
    }

    private lateinit var listenerRegister: ListenerRegister
    private val _listsLiveData = MutableLiveData<List<ShopList>>()
    val listsLiveData: LiveData<List<ShopList>> get() = _listsLiveData

}