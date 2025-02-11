package dev.gmarques.compras.ui.profile

import android.util.Log
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

    private val uiState = ProfileActivityState()

    private val _uiStateLd = MutableLiveData<ProfileActivityState>()
    val uiStateLd: LiveData<ProfileActivityState> get() = _uiStateLd

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
            requests.toList().also { uiState.requests = requests.toList() }
            _uiStateLd.postValue(uiState)
            Log.d(
                "USUK",
                "ProfileActivityViewModel.".plus("observeSyncInvites() requests = $requests")
            )
        }
    }

    private fun observeGuests() {
        lrGuests = UserRepository.observeGuests { guests ->
            guests.toList().also { uiState.guests = guests.toList() }
            _uiStateLd.postValue(uiState)
        }
    }

    private fun observeHost() {
        lrHost = UserRepository.observeHost { host ->
            host.toList().also { uiState.host = host.toList() }
            _uiStateLd.postValue(uiState)
        }
    }

    // TODO: replicar esse design em todo o app
    class ProfileActivityState {
        var requests: List<SyncAccount>? = null
        var guests: List<SyncAccount>? = null
        var host: List<SyncAccount>? = null
    }
}