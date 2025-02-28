package dev.gmarques.compras.ui.markets

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.gmarques.compras.App
import dev.gmarques.compras.R
import dev.gmarques.compras.data.model.Market
import dev.gmarques.compras.data.repository.MarketRepository
import dev.gmarques.compras.data.repository.model.ValidatedMarket
import dev.gmarques.compras.domain.utils.ListenerRegister
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class ActivityMarketsViewModel : ViewModel() {

    private var listenerRegister: ListenerRegister? = null

    private val _marketsLd = MutableLiveData<List<Market>>()
    val marketsLd: LiveData<List<Market>> get() = _marketsLd

    private val _errorEventLD = MutableLiveData<String>()
    val errorEventLD: LiveData<String> get() = _errorEventLD

    init {
        loadMarkets()
    }

    override fun onCleared() {
        listenerRegister?.remove()
        super.onCleared()
    }

    private fun loadMarkets() {
        listenerRegister = MarketRepository.observeMarketUpdates { markets, error ->
            if (error == null) {

                val sorted = markets!!.sortedWith(compareBy { it.name }).sortedWith(compareBy { it.position })
                _marketsLd.postValue(sorted)

            } else _errorEventLD.postValue(App.getContext().getString(R.string.Erro_ao_carregar_mercados_x, error.message))

        }
    }

    fun removeMarket(market: Market) = viewModelScope.launch(IO) {
        val result = MarketRepository.tryAndRemoveMarket(ValidatedMarket(market))
        if (result.isFailure) _errorEventLD.postValue(result.exceptionOrNull()!!.message!!)
    }

    fun updateMarketPosition(market: Market, newIndex: Int) {
        val newMarket = market.copy(position = newIndex)
        MarketRepository.addOrUpdateMarket(ValidatedMarket(newMarket))
    }

}
