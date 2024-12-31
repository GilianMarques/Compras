package dev.gmarques.compras.ui.main

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dev.gmarques.compras.data.model.ShopList
import dev.gmarques.compras.data.repository.ShopListRepository
import dev.gmarques.compras.utils.ListenerRegister


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

    fun addOrUpdateShopList(shopList: ShopList) {
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