package dev.gmarques.compras.ui.main

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dev.gmarques.compras.data.model.ShopList
import dev.gmarques.compras.data.repository.ShopListRepository
import dev.gmarques.compras.domain.utils.ListenerRegister


class MainActivityViewModel : ViewModel() {

    fun observeUpdates() {

        listenerRegister = ShopListRepository.observeShopListsUpdates { lists, error ->
            if (error == null) {
                val nonNullableList = lists!!
                _listsLiveData.postValue(nonNullableList)
            } else Log.d("USUK", "ShopListRepository.getAllLists: erro obtendo snapshot e $error")
        }

    }

    override fun onCleared() {
        listenerRegister?.remove()
        super.onCleared()
    }

    private var listenerRegister: ListenerRegister? = null
    private val _listsLiveData = MutableLiveData<List<ShopList>>()
    val listsLiveData: LiveData<List<ShopList>> get() = _listsLiveData

}