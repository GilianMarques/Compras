package dev.gmarques.compras.ui.add_item

import androidx.lifecycle.ViewModel
import dev.gmarques.compras.io.repositorios.CategoriaRepo
import dev.gmarques.compras.io.repositorios.ItemRepo
import dev.gmarques.compras.objetos.Categoria
import dev.gmarques.compras.objetos.Item

class AddItemViewModel : ViewModel() {
    internal lateinit var listaId: String
    internal val item: Item = Item()
    internal var categoriaSelecionada: Categoria? = null

    fun verificarEntradasEFechar() {


    }

    suspend fun itemJaExisteNaLista(nome: String) = ItemRepo.getItensNaListaPorNome(nome, listaId).isNotEmpty()

    suspend fun carregarSugestoes(nome: String): ArrayList<String> {
        val items = ItemRepo.getItensPorNome(nome)
        val nomes = ArrayList<String>()
        items.forEach { nomes.add(it.nome) }
        return nomes
    }

    suspend fun carregarCategorias(): ArrayList<Categoria> =
        CategoriaRepo.getCategorias() as ArrayList<Categoria>

    fun categoriaSelecionada(categoria: Categoria) {
        categoriaSelecionada = if (categoriaSelecionada?.equals(categoria) == true) null
        else categoria
    }
}