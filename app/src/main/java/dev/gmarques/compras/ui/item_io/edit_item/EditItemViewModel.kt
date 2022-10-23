package dev.gmarques.compras.ui.item_io.edit_item

import androidx.lifecycle.ViewModel
import dev.gmarques.compras.io.repositorios.CategoriaRepo
import dev.gmarques.compras.io.repositorios.ItemRepo
import dev.gmarques.compras.objetos.Categoria
import dev.gmarques.compras.objetos.Item

class EditItemViewModel : ViewModel() {
    lateinit var nomeOriginal: String
    internal lateinit var item: Item
    internal var position: Int = -1
    internal var categoriaSelecionada: Categoria? = null


    suspend fun carregarCategorias(): ArrayList<Categoria> =
        CategoriaRepo.getCategorias() as ArrayList<Categoria>


    fun categoriaSelecionada(categoria: Categoria) {
        categoriaSelecionada = if (categoriaSelecionada?.equals(categoria) == true) null
        else categoria
    }

    fun carregarCategoria() = CategoriaRepo.getCategoria(item)


    suspend fun itemJaExisteNaLista(nome: String) =
        ItemRepo.getItensNaListaPorNome(nome, item.listaId).isNotEmpty()

}