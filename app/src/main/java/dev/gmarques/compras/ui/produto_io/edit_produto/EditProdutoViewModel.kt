package dev.gmarques.compras.ui.produto_io.edit_produto

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dev.gmarques.compras.App
import dev.gmarques.compras.R
import dev.gmarques.compras.entidades.Categoria
import dev.gmarques.compras.entidades.Produto
import dev.gmarques.compras.io.repositorios.CategoriaRepo
import dev.gmarques.compras.io.repositorios.ItemRepo

class EditProdutoViewModel : ViewModel() {
    lateinit var produto: Produto
    /**Não deve sofrer qualquer tipo de alteraçao*/
    lateinit var produtoOriginal: Produto

    private val _categoriaSelecionada: MutableLiveData<Categoria> = MutableLiveData()
    val categoriaSelecionada get() = _categoriaSelecionada

    suspend fun produtoJaExisteNaLista(nome: String) =
        ItemRepo.getItensNaListaPorNome(nome, this.produto.listaId).isNotEmpty()

    suspend fun carregarSugestoes(nome: String): ArrayList<String> {
        val produtos = ItemRepo.getItensPorNome(nome)
        val nomes = ArrayList<String>()
        produtos.forEach { nomes.add(it.nome) }
        return nomes
    }

    /***
     * A posiçao 0 do array pode ficar com uma categoria qualquer porque sera usada para abrir
     * a tela para adicionar uma nova categoria
     * */
    suspend fun carregarCategorias() = ArrayList<Categoria>().also {

        val categoria = Categoria().apply {
            icone = Categoria.stringIcone(R.drawable.vec_add)
            nome = App.get.applicationContext.getString(R.string.Nova_categoria)
        }

        it.add(categoria)
        it.addAll(CategoriaRepo.getCategorias())
    }

    fun definirCategoriaSelecionada(categoria: Categoria) {
        _categoriaSelecionada.value = categoria
    }

    suspend fun receberCategoriaDoItem() = CategoriaRepo.getCategoriaPorId(this.produto.categoriaId)


}