package dev.gmarques.compras.ui.main

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dev.gmarques.compras.data.model.ShopList
import dev.gmarques.compras.data.repository.ShopListRepository
import dev.gmarques.compras.domain.utils.ListenerRegister


class MainActivityViewModel : ViewModel() {


    init {
        observeShopLists()
    }

    private fun observeShopLists() {

        listenerRegister = ShopListRepository.observeShopListsUpdates { lists, error ->
            if (error == null) {
                val sortedList = sortLists(lists!!)
                _listsLiveData.postValue(sortedList)
            } else Log.d("USUK", "ShopListRepository.getAllLists: erro obtendo snapshot e $error")
        }

    }

    private fun sortLists(lists: List<ShopList>): List<ShopList> {
        return lists.sortedByDescending { it.creationDate }
    }

    override fun onCleared() {
        listenerRegister?.remove()
        super.onCleared()
    }

    private var listenerRegister: ListenerRegister? = null
    private val _listsLiveData = MutableLiveData<List<ShopList>>()
    val listsLiveData: LiveData<List<ShopList>> get() = _listsLiveData

}