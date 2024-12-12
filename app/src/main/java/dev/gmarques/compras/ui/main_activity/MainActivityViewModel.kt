package dev.gmarques.compras.ui.main_activity

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dev.gmarques.compras.data.data.model.ShopList
import dev.gmarques.compras.data.data.repository.ShopListRepository


class MainActivityViewModel : ViewModel() {

    init {
        observeUpdates()
    }

    private fun observeUpdates() {

        ShopListRepository.observeListUpdates { lists, error ->
            if (error == null) lists.let { _listsLiveData.postValue(lists) }
            else Log.d("USUK", "ShopListRepository.getAllLists: erro obtendo snapshot e $error")
        }

    }

    fun addShopList(shopList: ShopList) {
        ShopListRepository.addList(shopList)
    }


    private val _listsLiveData = MutableLiveData<List<ShopList>>()
    val listsLiveData: LiveData<List<ShopList>> get() = _listsLiveData

}