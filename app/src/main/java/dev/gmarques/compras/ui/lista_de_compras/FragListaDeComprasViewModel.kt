package dev.gmarques.compras.ui.lista_de_compras

import android.app.Application
import android.util.Log
import androidx.annotation.MainThread
import androidx.lifecycle.*
import dev.gmarques.compras.entidades.Categoria
import dev.gmarques.compras.entidades.Lista
import dev.gmarques.compras.entidades.Produto
import dev.gmarques.compras.io.repositorios.CategoriaRepo
import dev.gmarques.compras.io.repositorios.ItemRepo
import dev.gmarques.compras.io.repositorios.ListaRepo
import kotlinx.coroutines.*


class FragListaDeComprasViewModel(appContext: Application) : AndroidViewModel(appContext) {

    /**liveData pra notificar o ui controller sobre alteraçoes na lista de categorias*/
    private val _categoriasLiveData = MutableLiveData<ArrayList<Categoria>>()
    val categoriasLiveData get() = _categoriasLiveData

    /**liveData pra notificar o ui controller sobre alteraçoes na lista de itens*/
    private val _produtosLiveData = MutableLiveData<ArrayList<Produto>>()
    val produtosLiveData get() = _produtosLiveData

    private val _categoriaSelecionadaLiveData = MutableLiveData<Categoria?>()
    val categoriaSelecionadaLiveData get() = _categoriaSelecionadaLiveData

    /** lista de compras atual*/
    private val _listaLiveData = MutableLiveData<Lista>()
    val listaLiveData: LiveData<Lista> get() = _listaLiveData


    init {
        runBlocking {
            _listaLiveData.value = ListaRepo.getUtimaOuQualquerLista() ?: Lista.PADRAO
        }

        @Suppress("ControlFlowWithEmptyBody") if (Lista.PADRAO == _listaLiveData.value) {
        } // TODO: criar uma lista a partir daqui

        carregarCategoriasNaLista()
        carregarItens()
    }


    /**
     * carrega os itens da lista  de acordo com a categoria selecionada
     * no momento
     * @return os itens direto do DB
     * */
    private fun carregarItens() = viewModelScope.launch(Dispatchers.IO) {
        Log.d("USUK", "FragListaDeComprasViewModel.".plus("carregarItens() "))

        val listaId = _listaLiveData.value!!.id
        val categoria = _categoriaSelecionadaLiveData.value
        val itens = if (categoria != null) {
            ArrayList(ItemRepo.getItensNaListaPorCategoria(listaId, categoria.id))
        } else ArrayList(ItemRepo.getItensNaLista(listaId))

        ordenarLista(itens)
        _produtosLiveData.postValue(itens)
    }

    private fun carregarCategoriasNaLista() = viewModelScope.launch(Dispatchers.IO) {
        Log.d("USUK", "FragListaDeComprasViewModel.carregarCategoriasPresentesNaLista: ")

        val tempData: HashMap<String, Categoria> = HashMap()
        val itens = ItemRepo.getItensNaLista(_listaLiveData.value?.id!!)

        for (produto in itens) tempData[produto.categoriaId] =
            CategoriaRepo.getCategoriaPorId(produto.categoriaId)
        val resultado = ArrayList(tempData.values.sortedWith(compareBy { it.nome }))
        _categoriasLiveData.postValue(resultado)
    }

    private suspend fun getCategoriaDoDB(produto: Produto): Categoria =
        CategoriaRepo.getCategoriaPorId(produto.categoriaId)

    private suspend fun attItemNoBancoDeDados(produto: Produto) = ItemRepo.addOuAtualizar(produto)

    /***
     * Faz as verificaçoes e aplica as alteraçoes necessarias para refletir  a inserçao de um
     * novo produto na interface
     */
    suspend fun addItemeAplicarAlteracoes(
        produto: Produto,
    ) {

        attItemNoBancoDeDados(produto) // add ao db

        val categoria = getCategoriaDoDB(produto)
        val categorias = _categoriasLiveData.value!!

        // produto pertence a uma categoria que ainda nao faz parte da lista
        if (!categorias.contains(categoria)) {
            categorias.add(categoria)
            _categoriasLiveData.value = categorias
        } else {
            //se o produto é da sub-lista sendo exibida, mando a UI add ele nela
            val catSelecionada = _categoriaSelecionadaLiveData.value
            if (catSelecionada?.id == produto.categoriaId || catSelecionada == null) {
                val itens = _produtosLiveData.value!!
                itens.add(produto)
                ordenarLista(itens)
                _produtosLiveData.value = itens
            }
        }
    }


    /*** todo atualizar doc
     * atualiza as categorias pra refletir a atualizacao de um produto.
     * um produto atualizado pode ter sido movido para outra categoria, essa deve ser
     * adicionada na lista de categorias caso ja nao estaja la, uma categoria vazia
     * deve ser removida da lista de categorias, existem outras alteraçoes que devem
     * ser feitas e podem ser causadas pela simples alteraçao de um produto
     *
     * @param categoriaAntigaId sempre representará a categoria do produto antes da ediçao,se a categoria
     * nao foi editada, o valor dessa variavel será igual á Item.categoriaId
     * */
    @MainThread
    suspend fun itemAtualizadoPeloUsuario(
        produtoAtualizado: Produto,
        produtoOriginal: Produto,
    ) {

         attItemNoBancoDeDados(produtoAtualizado)

        // categoria mudou?
        if (produtoOriginal.categoriaId != produtoAtualizado.categoriaId) {
            carregarCategoriasNaLista().join() // trabalho pesado feito na IO
            _categoriaSelecionadaLiveData.value = getCategoriaDoDB(produtoAtualizado)
            carregarItens().join()  // trabalho pesado feito na IO
        } else {
            // o produto que o usuario editou saiu dessa lista, logo ela nao é nula
            val produtos = _produtosLiveData.value!!
            val categoriaSelecionada = _categoriaSelecionadaLiveData.value

            //Se o novo item nao estiver na tela, tem coisa errada
            if (categoriaSelecionada == null || categoriaSelecionada.id == produtoOriginal.categoriaId) {
                produtos[produtos.indexOf(produtoOriginal)] = produtoAtualizado
                ordenarLista(produtos)
                _produtosLiveData.value = produtos

            } else {
                throw java.lang.Exception("Situação estranha... a categoria selecionada deve ser nula ou  == a categoria do produtoOriginal" +
                        "se não é nenhuma das 2 o que esta definido como categoria selecionada? e porque?")
            }
        }

    }

    suspend fun produtoComprado(alvo: Produto, comprado: Boolean) {

        val produto = alvo.clonar()
        produto.comprado = comprado

        // salva no DB
        attItemNoBancoDeDados(produto)

        val itensLista = _produtosLiveData.value!!
        itensLista[itensLista.indexOf(alvo)] = produto
        ordenarLista(itensLista)
        _produtosLiveData.value = itensLista

        // atualizo todas as categorias
        _categoriasLiveData.value = categoriasLiveData.value

    }

    private fun ordenarLista(itens: ArrayList<Produto>) {
        var data: List<Produto> = itens.sortedWith(compareBy { it.nome })
        data = data.sortedWith(compareBy { it.comprado })
        itens.clear()
        itens.addAll(data)
    }

    /**
     * remove o produto da lista de itens, atualiza o objeto no DB e verifica quais dados precisam
     * ser atualizados nos recyclerviews de itens e categorias com base nas condiçoes de exclusao
     * Nota:  Expera-se que o produto removido, tenha sido removido pelo usuario.
     * É seguro chamar essa função de uma thread secundaria (Dispatchers.IO)
     * */
    suspend fun removerItem(produto: Produto) {

        produto.removido = true
        attItemNoBancoDeDados(produto)

        val categoriaItem = getCategoriaDoDB(produto)
        val categorias = _categoriasLiveData.value!!

        // era o ultimo produto da categoria?
        val itensDb =
            ItemRepo.getItensNaListaPorCategoria(listaLiveData.value!!.id, produto.categoriaId)
        itensDb.retainAll { it.categoriaId == produto.categoriaId } // ficam só os itens da categoria
        itensDb.removeAll { it.id == produto.id } // removo agora o produto removido pelo usuario

        if (itensDb.size == 0) {
            val indice = categorias.indexOf(categoriaItem)
            categorias.remove(categoriaItem)
            _categoriasLiveData.postValue(categorias)

            //categoria removida é a categoria selecionada? 99% das vezes sim
            if (categoriaItem.id == _categoriaSelecionadaLiveData.value?.id) {
                val indiceSeguro = indice.coerceAtMost(categorias.size - 1)
                _categoriaSelecionadaLiveData.postValue(categorias[indiceSeguro])
                carregarItens()
            }

        } else {
            val itensRv = _produtosLiveData.value!!
            itensRv.remove(produto)
            _produtosLiveData.postValue(itensRv)
            // recarrega todas as categorias para fazer com que a categoria reveja se  todos os seus itens estao comprados ou nao
            // é isso ou escrever uma logica para atualizar só a categoria em questao, considerando que a coleção de categorias
            // é sempre pequena, essa solução é totalmente valida, alem do mais quando nenhuma categoria esta selecionada
            //  todas as categorias precisam ser verificadas quando um produto é removido
            _categoriasLiveData.postValue(categorias)
        }


    }

    suspend fun buscaEsteItemEmOutrasListas(produto: Produto): ArrayList<Produto> =
        ArrayList(ItemRepo.getItensPorNomeExato(produto, 6))

    /**
     * Verifica a categoria selecionada para saber se o usuario esta selecionando
     * uma nova categoria ou apenas desselecionando a atual.
     * @see FragListaDeCompras.initRvDeCategorias
     * */
    fun selecionarCategoriaPeloUsuario(categoria: Categoria) {

        val catSelecionada = _categoriaSelecionadaLiveData.value

        if (categoria.id == catSelecionada?.id) _categoriaSelecionadaLiveData.value = null
        else _categoriaSelecionadaLiveData.value = categoria

        carregarItens()
    }

    suspend fun todosOsItensDaCategoriaForamComprados(categoria: Categoria) =
        ItemRepo.getItensNaListaPorCategoria(listaLiveData.value!!.id, categoria.id)
            .all { it.comprado }

    /**
     * Essa funçao é chamada sempre que um produto tem seu preço ou quantidade atualizados pelos dialogos
     * de ediçao rapida no fragmento de lista de compras, serve apenas para salvar o produto no banco
     * e notificar a alteraçao para a UI*/
    suspend fun aplicarPrecoOuQuantidadeeNotificar(
        alvo: Produto,
        preco: Float? = null,
        quantidade: Int? = null,
    ) {

        val clone = alvo.clonar()
        if (preco != null) clone.preco = preco
        if (quantidade != null) clone.quantidade = quantidade

        attItemNoBancoDeDados(clone)

        val itens = _produtosLiveData.value!!
        itens[itens.indexOf(alvo)] = clone
        _produtosLiveData.postValue(itens)
    }


}