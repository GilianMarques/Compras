package dev.gmarques.compras.ui.add_item

import androidx.lifecycle.ViewModel
import dev.gmarques.compras.io.repositorios.ItemRepo
import dev.gmarques.compras.objetos.Item

class AddItemViewModel : ViewModel() {
    internal lateinit var listaId: String
    internal val item: Item = Item()

    fun verificarEntradasEFechar() {


    }

    suspend fun itemJaExisteNaLista(nome: String) = ItemRepo.getItens(nome, listaId).isNotEmpty()


}