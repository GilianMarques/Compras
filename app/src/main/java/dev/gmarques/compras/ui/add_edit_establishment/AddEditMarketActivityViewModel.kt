package dev.gmarques.compras.ui.add_edit_establishment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dev.gmarques.compras.App
import dev.gmarques.compras.R
import dev.gmarques.compras.data.model.Establishment
import dev.gmarques.compras.data.repository.EstablishmentRepository
import dev.gmarques.compras.data.repository.model.ValidatedEstablishment
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext


class AddEditEstablishmentActivityViewModel : ViewModel() {

    var editingEstablishment: Boolean = false
    var establishmentId: String? = null
    var validatedName: String = ""
    var validatedColor: Int = -1


    private val _editingEstablishmentLD = MutableLiveData<Establishment>()
    val editingEstablishmentLD: LiveData<Establishment> get() = _editingEstablishmentLD

    private val _finishEventLD = MutableLiveData<Boolean>()
    val finishEventLD: LiveData<Boolean> get() = _finishEventLD

    private val _errorEventLD = MutableLiveData<String>()
    val errorEventLD: LiveData<String> get() = _errorEventLD

    suspend fun tryAndSaveEstablishment() = withContext(IO) {

        // se adicionando estabelecimento ou se durante a ediçao o usuario trocar o nome do estabelecimento, preciso verificar se o novo nome ja nao existe
        val needCheckName = !editingEstablishment || editingEstablishmentLD.value!!.name != validatedName

        if (needCheckName) {
            val result = EstablishmentRepository.getEstablishmentsByName(validatedName,1)

            val establishmentDontExist = result.getOrNull() == null

            if (establishmentDontExist) saveEstablishment()
            else {
                val msg = String.format(App.getContext().getString(R.string.X_ja_existe), validatedName)
                _errorEventLD.postValue(msg)
            }

        } else saveEstablishment()


    }

    private fun saveEstablishment() {

        val newEstablishment = if (editingEstablishment) _editingEstablishmentLD.value!!.copy(
            name = validatedName, color = validatedColor
        )
        else Establishment(
            validatedName, validatedColor
        )

        EstablishmentRepository.addOrUpdateEstablishment(ValidatedEstablishment(newEstablishment))

        _finishEventLD.postValue(true)

    }

    suspend fun loadEstablishment() = withContext(IO) {
        establishmentId?.let {
            val establishment = EstablishmentRepository.getEstablishment(establishmentId!!)
            _editingEstablishmentLD.postValue(establishment!!)
        }
    }



}