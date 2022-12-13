package dev.gmarques.compras.presenter.frags_activities.produto_io.add_produto

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dev.gmarques.compras.App
import dev.gmarques.compras.R
import dev.gmarques.compras.data.entidades.CategoriaEntidade
import dev.gmarques.compras.data.entidades.ProdutoEntidade
import dev.gmarques.compras.data.repositorios.CategoriaRepo
import dev.gmarques.compras.data.repositorios.ItemRepo

class AddProdutoViewModel : ViewModel() {
    internal lateinit var listaId: String
    internal val produtoEntidade: ProdutoEntidade = ProdutoEntidade()

    private val _categoriaSelecionada: MutableLiveData<CategoriaEntidade> = MutableLiveData()
    val categoriaSelecionada get() = _categoriaSelecionada

    suspend fun produtoJaExisteNaLista(nome: String) =
            ItemRepo.getItensNaListaPorNome(nome, listaId).isNotEmpty()

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
    suspend fun carregarCategorias() = ArrayList<CategoriaEntidade>().also {

        val categoria = CategoriaEntidade().apply {
            setIcone(R.drawable.vec_add)
            nome = App.get.applicationContext.getString(R.string.Nova_categoria)
        }

        it.add(categoria)
        it.addAll(CategoriaRepo.getCategorias())
    }

    fun definirCategoriaSelecionada(categoria: CategoriaEntidade) {
        _categoriaSelecionada.value = categoria
    }

}