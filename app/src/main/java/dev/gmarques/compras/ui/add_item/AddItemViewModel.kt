package dev.gmarques.compras.ui.add_item

import androidx.lifecycle.ViewModel
import dev.gmarques.compras.io.repositorios.CategoriaRepo
import dev.gmarques.compras.io.repositorios.ItemRepo
import dev.gmarques.compras.objetos.Categoria
import dev.gmarques.compras.objetos.Item

class AddItemViewModel : ViewModel() {
    internal lateinit var listaId: String
    internal val item: Item = Item()

    fun verificarEntradasEFechar() {


    }

    suspend fun itemJaExisteNaLista(nome: String) = ItemRepo.getItens(nome, listaId).isNotEmpty()

    suspend fun carregarSugestoes(nome: String): ArrayList<String> {
        val items = ItemRepo.getItens(nome)
        val nomes = ArrayList<String>()
        items.forEach { nomes.add(it.nome) }
        return nomes
    }

    suspend fun carregarCategorias(): ArrayList<Categoria> = CategoriaRepo.getCategorias() as ArrayList<Categoria>

     fun categoriaClick(categoria: Categoria) {
    }
}