package dev.gmarques.compras.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dev.gmarques.compras.data.model.ShopList
import dev.gmarques.compras.data.model.SyncRequest
import dev.gmarques.compras.data.repository.UserRepository
import dev.gmarques.compras.domain.utils.ListenerRegister


class ProfileActivityViewModel : ViewModel() {

    private lateinit var listenerRegister: ListenerRegister
    private val uiState = ProfileActivityState()

    private val _uiStateLd = MutableLiveData<ProfileActivityState>()
    val uiStateLd: LiveData<ProfileActivityState> get() = _uiStateLd

    init {
        observeSyncRequests()
    }

    override fun onCleared() {
        listenerRegister.remove()
        super.onCleared()
    }


    private fun observeSyncRequests() {
        listenerRegister = UserRepository.observeSyncRequests { requests ->
            requests.toList().also { uiState.requests = requests.toList() }
            _uiStateLd.postValue(uiState)
        }
    }


    // TODO: replicar esse design em todo o app
    class ProfileActivityState {
        var requests: List<SyncRequest>? = null
    }
}