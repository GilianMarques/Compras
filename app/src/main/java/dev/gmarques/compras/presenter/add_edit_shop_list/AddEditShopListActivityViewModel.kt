package dev.gmarques.compras.presenter.add_edit_shop_list

import androidx.core.graphics.toColorInt
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
import kotlin.random.Random


class AddEditShopListActivityViewModel : ViewModel() {

    suspend fun tryAndSaveShopList() = withContext(IO) {

        saveShopList()


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
    var validatedColor: Int = run {
        val colors = App.getContext().resources.getStringArray(R.array.pastel_colors)
            .asList().map { it.toColorInt() }
        colors[Random.nextInt(0,colors.size-1)]
    }


    private val _editingShopListLD = MutableLiveData<ShopList>()
    val editingShopListLD: LiveData<ShopList> get() = _editingShopListLD

    private val _finishEventLD = MutableLiveData<Boolean>()
    val finishEventLD: LiveData<Boolean> get() = _finishEventLD

    private val _errorEventLD = MutableLiveData<String>()
    val errorEventLD: LiveData<String> get() = _errorEventLD

}
