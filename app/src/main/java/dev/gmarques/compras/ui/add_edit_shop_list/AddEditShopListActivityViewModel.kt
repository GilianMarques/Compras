package dev.gmarques.compras.ui.add_edit_shop_list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dev.gmarques.compras.App
import dev.gmarques.compras.R
import dev.gmarques.compras.data.model.ShopList
import dev.gmarques.compras.data.repository.ShopListRepository
import dev.gmarques.compras.data.repository.model.ValidatedShopList
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext


class AddEditShopListActivityViewModel : ViewModel() {

    suspend fun tryAndSaveShopList() = withContext(IO) {

        // se adicionanda lista de compras ou se durante a ediçao o usuario trocar o nome, preciso verificar se o novo nome ja nao existe
        val needCheckName = !editingShopList || editingShopListLD.value!!.name != validatedName

        if (needCheckName) {
            val result = ShopListRepository.getShopListByName(validatedName)

            val shopListDontExist = result.getOrNull() == null

            if (shopListDontExist) saveShopList()
            else {
                val msg = String.format(App.getContext().getString(R.string.X_ja_existe), validatedName)
                _errorEventLD.postValue(msg)
            }

        } else saveShopList()


    }

    private fun saveShopList() {

        val newShopList = if (editingShopList) _editingShopListLD.value!!.copy(
            name = validatedName, color = validatedColor
        )
        else ShopList(
            validatedName, validatedColor
        )

        ShopListRepository.addOrUpdateShopList(ValidatedShopList(newShopList))

        _finishEventLD.postValue(true)

    }

    suspend fun loadShopList() = withContext(IO) {
        shopListId?.let {
            val result = ShopListRepository.getShopList(shopListId!!)
            _editingShopListLD.postValue(result.getOrThrow())
        }
    }

    var editingShopList: Boolean = false
    var shopListId: String? = null
    var validatedName: String = ""
    var validatedColor: Int = -1


    private val _editingShopListLD = MutableLiveData<ShopList>()
    val editingShopListLD: LiveData<ShopList> get() = _editingShopListLD

    private val _finishEventLD = MutableLiveData<Boolean>()
    val finishEventLD: LiveData<Boolean> get() = _finishEventLD

    private val _errorEventLD = MutableLiveData<String>()
    val errorEventLD: LiveData<String> get() = _errorEventLD

}