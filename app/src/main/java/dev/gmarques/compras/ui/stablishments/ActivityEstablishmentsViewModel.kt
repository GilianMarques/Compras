package dev.gmarques.compras.ui.stablishments

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.gmarques.compras.App
import dev.gmarques.compras.R
import dev.gmarques.compras.data.model.Establishment
import dev.gmarques.compras.data.repository.EstablishmentRepository
import dev.gmarques.compras.data.repository.model.ValidatedEstablishment
import dev.gmarques.compras.domain.utils.ListenerRegister
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class ActivityEstablishmentsViewModel : ViewModel() {

    private var listenerRegister: ListenerRegister? = null

    private val _establishmentsLd = MutableLiveData<List<Establishment>>()
    val establishmentsLd: LiveData<List<Establishment>> get() = _establishmentsLd

    private val _errorEventLD = MutableLiveData<String>()
    val errorEventLD: LiveData<String> get() = _errorEventLD

    init {
        loadEstablishments()
    }

    override fun onCleared() {
        listenerRegister?.remove()
        super.onCleared()
    }

    private fun loadEstablishments() {
        listenerRegister = EstablishmentRepository.observeEstablishmentUpdates { establishments, error ->
            if (error == null) {

                val sorted = establishments!!.sortedWith(compareBy { it.name }).sortedWith(compareBy { it.position })
                _establishmentsLd.postValue(sorted)

            } else _errorEventLD.postValue(App.getContext().getString(R.string.Erro_ao_carregar_estabelecimentos_x, error.message))

        }
    }

    fun removeEstablishment(establishment: Establishment) = viewModelScope.launch(IO) {
        val result = EstablishmentRepository.tryAndRemoveEstablishment(ValidatedEstablishment(establishment))
        if (result.isFailure) _errorEventLD.postValue(result.exceptionOrNull()!!.message!!)
    }

    fun updateEstablishmentPosition(establishment: Establishment, newIndex: Int) {
        val newEstablishment = establishment.copy(position = newIndex)
        EstablishmentRepository.addOrUpdateEstablishment(ValidatedEstablishment(newEstablishment))
    }

}
