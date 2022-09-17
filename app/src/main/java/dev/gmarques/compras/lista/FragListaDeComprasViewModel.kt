package dev.gmarques.compras.lista

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.gmarques.compras.database.Database
import dev.gmarques.compras.objetos.Lista
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


class FragListaDeComprasViewModel : ViewModel() {

    private var job: Job = Job()
    var listas: ArrayList<Lista> = ArrayList()


    fun init() {

        job = viewModelScope.launch(Dispatchers.IO) {
            listas = Database.inst().getListas()
        }
    }

    //viewmodel sendo destruido
    override fun onCleared() {
        job.cancel()
        super.onCleared()
    }


}