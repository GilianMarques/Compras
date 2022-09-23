package dev.gmarques.compras.lista

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.gmarques.compras.database.Database
import dev.gmarques.compras.objetos.Item
import dev.gmarques.compras.objetos.Lista
import dev.gmarques.compras.viewmodel_utils.MutableListLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class FragListaDeComprasViewModel : ViewModel() {


    private val _itensLiveData = MutableListLiveData<Item>()
    val itensLiveData: LiveData<MutableListLiveData.DadosEvento<Item>>
        get() = _itensLiveData

    var lista = Lista()
        private set

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val data = Database.inst().getListas()[0]

            withContext(Dispatchers.Main) {
                lista = data
                _itensLiveData.listaAtualizada()// TODO: chamar em qqer thread 

            }
        }

    }

    fun itemClick(item: Item, posicao: Int) {
        Log.d("USUK: FragListaDeComprasViewModel",
            "itemClick() called with: item = ${item.nome}, posicao = $posicao")
    }

    fun addItem() {
        val mItem = Item()
        with(mItem) {
            nome = "item${lista.itens.size + 1}"
            qtd = lista.itens.size + 1
            preco = lista.itens.size + 1 / 3.14f
        }

        lista.itens.add(3, mItem)
        _itensLiveData.itemAdicionado(mItem, 3)
    }


}