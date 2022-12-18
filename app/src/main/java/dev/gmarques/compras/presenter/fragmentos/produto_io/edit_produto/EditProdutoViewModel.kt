package dev.gmarques.compras.presenter.fragmentos.produto_io.edit_produto

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dev.gmarques.compras.App
import dev.gmarques.compras.R
import dev.gmarques.compras.domain.entidades.Categoria
import dev.gmarques.compras.domain.entidades.Produto
import dev.gmarques.compras.data.repositorios.CategoriaRepo
import dev.gmarques.compras.data.repositorios.ProdutoDispensaRepo
import dev.gmarques.compras.data.repositorios.ProdutoRepo
import dev.gmarques.compras.domain.entidades.ProdutoDispensa

class EditProdutoViewModel : ViewModel() {
    lateinit var produto: Produto
    /**Não deve sofrer qualquer tipo de alteraçao*/
    lateinit var produtoOriginal: Produto

    private val _categoriaSelecionada: MutableLiveData<Categoria> = MutableLiveData()
    val categoriaSelecionada get() = _categoriaSelecionada

    suspend fun produtoJaExisteNaLista(nome: String) =
        ProdutoRepo.getProdutosNaListaPorNome(nome, this.produto.listaId).isNotEmpty()

    suspend fun carregarSugestoes(nome: String): Pair<List<ProdutoDispensa>, ArrayList<String>> {
        val produtos = ProdutoDispensaRepo.getProdutosPorNome(nome)
        val nomes = ArrayList<String>()
        produtos.forEach { nomes.add(it.nome) }
        return produtos to nomes
    }


    /***
     * A posiçao 0 do array pode ficar com uma categoria qualquer porque sera usada para abrir
     * a tela para adicionar uma nova categoria
     * */
    suspend fun carregarCategorias() = ArrayList<Categoria>().also {

        val categoria = Categoria().apply {
            setIcone(R.drawable.vec_add)
            nome = App.get.applicationContext.getString(R.string.Nova_categoria)
        }

        it.add(categoria)
        it.addAll(CategoriaRepo.getCategorias())
    }

    fun definirCategoriaSelecionada(categoria: Categoria) {
        _categoriaSelecionada.value = categoria
    }

    suspend fun receberCategoriaDoItem() = CategoriaRepo.getCategoriaPorId(this.produto.categoriaId)

    suspend fun carregarCategoria(categoriaId: String): Categoria {
        return CategoriaRepo.getCategoriaPorId(categoriaId)
    }


}