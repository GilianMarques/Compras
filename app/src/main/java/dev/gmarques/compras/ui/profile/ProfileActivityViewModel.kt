package dev.gmarques.compras.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dev.gmarques.compras.data.model.SyncAccount
import dev.gmarques.compras.data.repository.UserRepository
import dev.gmarques.compras.domain.utils.ListenerRegister


class ProfileActivityViewModel : ViewModel() {

    private lateinit var lrSyncInvite: ListenerRegister
    private lateinit var lrGuests: ListenerRegister
    private lateinit var lrHost: ListenerRegister

    private var uiState: UiState = UiState()
        set(value) {
            field = value
            _liveData.postValue(field)
        }

    private val _liveData = MutableLiveData<UiState>()
    val uiStateLd: LiveData<UiState> get() = _liveData

    init {
        observeSyncInvites()
        observeGuests()
        observeHost()
    }

    override fun onCleared() {
        lrSyncInvite.remove()
        lrGuests.remove()
        lrHost.remove()

        super.onCleared()
    }

    private fun observeSyncInvites() {
        lrSyncInvite = UserRepository.observeSyncInvites { requests ->
            requests.let {
                uiState = uiState.copy(requests = it)
            }
        }
    }


    private fun observeGuests() {
        lrGuests = UserRepository.observeGuests { guests ->
            guests.let {
                uiState = uiState.copy(guests = it)
            }

        }
    }

    private fun observeHost() {


        lrHost = UserRepository.observeHost { host: MutableList<SyncAccount> ->
            host.let {
                uiState = uiState.copy(host = it)
            }
        }
    }

    // TODO: replicar esse design em todo o app
    data class UiState(
        val requests: List<SyncAccount> = emptyList(),
        val guests: List<SyncAccount> = emptyList(),
        val host: List<SyncAccount> = emptyList(),
    )

}