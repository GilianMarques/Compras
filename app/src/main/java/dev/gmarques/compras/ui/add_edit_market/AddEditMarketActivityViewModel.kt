package dev.gmarques.compras.ui.add_edit_market

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dev.gmarques.compras.App
import dev.gmarques.compras.R
import dev.gmarques.compras.data.model.Market
import dev.gmarques.compras.data.repository.MarketRepository
import dev.gmarques.compras.data.repository.model.ValidatedMarket
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext


class AddEditMarketActivityViewModel : ViewModel() {

    suspend fun tryAndSaveMarket() = withContext(IO) {

        // se adicionando mercado ou se durante a ediçao o usuario trocar o nome do mercado, preciso verificar se o novo nome ja nao existe
        val needCheckName = !editingMarket || editingMarketLD.value!!.name != validatedName

        if (needCheckName) {
            val result = MarketRepository.getMarketByName(validatedName)

            val marketDontExist = result.getOrNull() == null

            if (marketDontExist) saveMarket()
            else {
                val msg = String.format(App.getContext().getString(R.string.X_ja_existe), validatedName)
                _errorEventLD.postValue(msg)
            }

        } else saveMarket()


    }

    private fun saveMarket() {

        val newMarket = if (editingMarket) _editingMarketLD.value!!.copy(
            name = validatedName, color = validatedColor
        )
        else Market(
            validatedName, validatedColor
        )

        MarketRepository.addOrUpdateMarket(ValidatedMarket(newMarket))

        _finishEventLD.postValue(true)

    }

    suspend fun loadMarket() = withContext(IO) {
        marketId?.let {
            val market = MarketRepository.getMarket(marketId!!)
            _editingMarketLD.postValue(market)
        }
    }

    var editingMarket: Boolean = false
    var marketId: String? = null
    var validatedName: String = ""
    var validatedColor: Int = -1


    private val _editingMarketLD = MutableLiveData<Market>()
    val editingMarketLD: LiveData<Market> get() = _editingMarketLD

    private val _finishEventLD = MutableLiveData<Boolean>()
    val finishEventLD: LiveData<Boolean> get() = _finishEventLD

    private val _errorEventLD = MutableLiveData<String>()
    val errorEventLD: LiveData<String> get() = _errorEventLD

}