package dev.gmarques.compras.ui.lista_de_compras

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import dev.gmarques.compras.entidades.Categoria
import dev.gmarques.compras.entidades.Lista
import dev.gmarques.compras.entidades.Produto
import dev.gmarques.compras.entidades.helpers.CategoriaHolder
import dev.gmarques.compras.io.repositorios.CategoriaRepo
import dev.gmarques.compras.io.repositorios.ItemRepo
import dev.gmarques.compras.io.repositorios.ListaRepo
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main


@Suppress("UNUSED_VARIABLE")
class FragListaDeComprasViewModel(appContext: Application) : AndroidViewModel(appContext) {

    /**liveData pra notificar o ui controller sobre alteraçoes na lista de categorias*/
    private val _categoriasLiveData = MutableLiveData<ArrayList<CategoriaHolder>>()
    val categoriasLiveData get() = _categoriasLiveData

    /**liveData pra notificar o ui controller sobre alteraçoes na lista de itens*/
    private val _produtosLiveData = MutableLiveData<ArrayList<Produto>>()
    val produtosLiveData get() = _produtosLiveData

    /** lista de compras atual*/
    private val _listaLiveData = MutableLiveData<Lista>()
    val listaLiveData: LiveData<Lista> get() = _listaLiveData

    private val _valoresLiveData = MutableLiveData<Triple<Double, Double, Double>>()
    val valoresLiveData: LiveData<Triple<Double, Double, Double>> get() = _valoresLiveData

    /**
     * Esta é uma referencia à categoria selecionada dentro do array no
     * _categoriasLiveData, seu proposito é evitar iteraçoes desnecessarias
     * no array sempre que necessario acessar a categoria selecionada.
     * Alteraçoes nessa variavel se refletem na lista de categorias dentro do array.
     */
    private var categSelecHolder: CategoriaHolder? = null

    init {
        runBlocking {
            _listaLiveData.value = ListaRepo.getUtimaOuQualquerLista() ?: Lista.PADRAO
            @Suppress("ControlFlowWithEmptyBody") if (Lista.PADRAO == _listaLiveData.value) {
            } // TODO: criar uma lista a partir daqui

            carregarCategoriasNaLista()
            carregarItens()
            calcularValores()
        }


    }


    /**
     * carrega os itens da lista  de acordo com a categoria selecionada
     * no momento
     * @return os itens direto do DB
     * */
    private suspend fun carregarItens() = withContext(IO) {
        Log.d("USUK", "FragListaDeComprasViewModel.".plus("carregarItens() "))

        val listaId = _listaLiveData.value!!.id
        val holder = categSelecHolder
        val itens = if (holder != null) {
            ArrayList(ItemRepo.getItensNaListaPorCategoria(listaId, holder.categoria.id))
        } else ArrayList(ItemRepo.getItensNaLista(listaId))

        ordenarProdutos(itens)
        _produtosLiveData.postValue(itens)
    }

    private suspend fun carregarCategoriasNaLista() = withContext(IO) {
        val map: HashMap<String, CategoriaHolder> = HashMap()
        val itens = ItemRepo.getItensNaLista(_listaLiveData.value?.id!!)

        for (produto in itens) {
            val categoria = CategoriaRepo.getCategoriaPorId(produto.categoriaId)
            map[produto.categoriaId] =
                    CategoriaHolder(categoria, todosOsItensForamComprados(categoria))
        }

        val resultado = ArrayList(map.values.toList())
        ordenarCategorias(resultado)
        _categoriasLiveData.postValue(resultado)
    }

    private suspend fun receberCategoriaDoDB(produto: Produto): Categoria = withContext(IO) {
        CategoriaRepo.getCategoriaPorId(produto.categoriaId)
    }

    private suspend fun attItemNoBancoDeDados(produto: Produto) =
            withContext(IO) { ItemRepo.addOuAtualizar(produto) }

    /**
     * @return Um par com uma copia editavel de CategoriaHolder e seu indice
     * @throws Exception caso o objeto não seja encontrado na lista
     * */
    private fun receberHolderDaCategoria(alvo: Categoria): Pair<CategoriaHolder, Int> {
        val lista = _categoriasLiveData.value!!
        for (i in 0 until lista.size) {
            val holder = lista[i]
            if (holder.categoria.id == alvo.id) return holder.clonar() to i
        }

        throw java.lang.Exception("${alvo.nome} nao foi encontrada na lista" +
                "\nalvo = $alvo" +
                "\nTamanho da lista: ${_categoriasLiveData.value?.size}, Conteudo:" +
                "\n${_categoriasLiveData.value?.joinToString(separator = "\n")}")
    }

    /**
     * Verifica se a categoria recebida existe na lista de categorias sendo exibidea para o usuario
     * Atente-se ao nome da função.
     * @return false se a categoria existe na lista caso contrario true
     * */
    private fun categoriaNaoExisteNalista(categoria: Categoria): Boolean {
        for (holder in categoriasLiveData.value!!) if (categoria.id == holder.categoria.id) return false
        return true
    }

    private fun ordenarProdutos(itens: ArrayList<Produto>) {
        var data: List<Produto> = itens.sortedWith(compareBy { it.nome })
        data = data.sortedWith(compareBy { it.comprado })
        itens.clear()
        itens.addAll(data)
    }

    private fun ordenarCategorias(itens: ArrayList<CategoriaHolder>) {
        var data: List<CategoriaHolder> = itens.sortedWith(compareBy { it.categoria.nome })
        data = data.sortedWith(compareBy { it.itensComprados })
        itens.clear()
        itens.addAll(data)
    }

    /**
     * Verifica se todos os itens de uma determibnada categoria foram comprados.
     * Os itens verificados sao carregados diretamente do Banco de dados.
     * */
    private suspend fun todosOsItensForamComprados(categoria: Categoria) =
            ItemRepo.getItensNaListaPorCategoria(listaLiveData.value!!.id, categoria.id)
                .all { it.comprado }

    /**
     * Faz as verificaçoes e aplica as alteraçoes necessarias para refletir a inserçao de um
     * novo produto na interface
     */
    fun addProduto(
        produto: Produto,
    ) = viewModelScope.launch(IO) {

        attItemNoBancoDeDados(produto) // add ao db

        val categoriaDoProduto = receberCategoriaDoDB(produto)

        // modifico uma copia da lista para ser comparada com a versao anterior pelo DIfUtils no adapter
        val categorias = ArrayList(_categoriasLiveData.value!!)

        // produto pertence a uma categoria que ainda nao faz parte da lista
        if (categoriaNaoExisteNalista(categoriaDoProduto)) {
            val holder = CategoriaHolder(categoriaDoProduto)
            categorias.add(holder)
            ordenarCategorias(categorias)
            withContext(Main) {
                _categoriasLiveData.value = categorias
                selecionarCategoria(holder)
            }
        } else {

            // a categoria do produto ja esta sendo exibida pro usuario entao preciso atualizar
            // a UI pra caso a categoria ja nao tenha mais todos os seus itens comprados
            val (holder: CategoriaHolder, indice: Int) = receberHolderDaCategoria(categoriaDoProduto)
            holder.itensComprados = todosOsItensForamComprados(categoriaDoProduto)
            categorias[indice] = holder

            withContext(Main) {
                _categoriasLiveData.value = categorias
            }

            //se o novo produto é da sub-lista sendo exibida, atualizo a UI
            if (categSelecHolder?.categoria?.id == produto.categoriaId || categSelecHolder == null) {
                val itens = ArrayList(_produtosLiveData.value!!)
                itens.add(produto)
                ordenarProdutos(itens)
                _produtosLiveData.postValue(itens)
                //se nao seleciono a categoria dele
            } else selecionarCategoria(receberHolderDaCategoria(categoriaDoProduto).first)
        }
        calcularValores()
    }

    fun attProduto(
        produtoAtualizado: Produto,
        produtoOriginal: Produto,
    ) = viewModelScope.launch {

        attItemNoBancoDeDados(produtoAtualizado)

        // categoria mudou?
        if (produtoOriginal.categoriaId != produtoAtualizado.categoriaId) {
            carregarCategoriasNaLista()// trabalho pesado feito na IO
            val categoria = receberCategoriaDoDB(produtoAtualizado)
            selecionarCategoria(receberHolderDaCategoria(categoria).first)
        } else {
            // o produto que o usuario editou saiu dessa lista, logo ela nao é nula
            val produtos = _produtosLiveData.value!!
            val categoriaSelecionada = categSelecHolder?.categoria

            if (categoriaSelecionada == null || categoriaSelecionada.id == produtoOriginal.categoriaId) {
                produtos[produtos.indexOf(produtoOriginal)] = produtoAtualizado
                ordenarProdutos(produtos)
                _produtosLiveData.value = produtos

            } else {
                //Se o novo item nao estiver na tela, tem coisa errada
                throw java.lang.Exception("Situação estranha... a categoria selecionada deve ser nula ou  == a categoria do produtoOriginal" + "se não é nenhuma das 2 o que esta definido como categoria selecionada? e porque?")
            }
        }
        if (produtoAtualizado.valorTotal() != produtoOriginal.valorTotal()) calcularValores()
    }

    fun produtoComprado(produtoOriginal: Produto, comprado: Boolean) = viewModelScope.launch {

        val novoProduto = produtoOriginal.clonar()
        novoProduto.comprado = comprado

        // salva no DB
        attItemNoBancoDeDados(novoProduto)

        //atualizo a lista de itens
        val itensLista = ArrayList(_produtosLiveData.value!!)
        itensLista[itensLista.indexOf(produtoOriginal)] = novoProduto
        ordenarProdutos(itensLista)
        _produtosLiveData.value = itensLista

        // atualizo a categoria
        val categorias = ArrayList(_categoriasLiveData.value!!)
        val (holder: CategoriaHolder, indice: Int) = receberHolderDaCategoria(receberCategoriaDoDB(novoProduto))
        holder.itensComprados = todosOsItensForamComprados(holder.categoria)

        categorias[indice] = holder
        ordenarCategorias(categorias)
        _categoriasLiveData.value = categorias

        calcularValores()
    }

    /**
     * remove o produto da lista de itens, atualiza o objeto no DB e verifica quais dados precisam
     * ser atualizados nos recyclerviews de itens e categorias com base nas condiçoes de exclusao
     * Nota:  Expera-se que o produto removido, tenha sido removido pelo usuario.
     * TODO: corrigir comportamento de exclusao de ultimo produto
     * */
    fun removerProduto(produto: Produto) = viewModelScope.launch(IO) {

        produto.removido = true

        attItemNoBancoDeDados(produto)

        val categoriaDoProduto = receberCategoriaDoDB(produto)
        val categorias = ArrayList(_categoriasLiveData.value!!)

        val itensDb = ItemRepo
            .getItensNaListaPorCategoria(listaLiveData.value!!.id, produto.categoriaId)
        itensDb.retainAll { it.categoriaId == produto.categoriaId } // ficam só os itens da categoria
        itensDb.removeAll { it.id == produto.id } // removo agora o produto removido pelo usuario

        // era o ultimo produto da categoria?
        if (itensDb.size == 0) {
            val (holder: CategoriaHolder, indice: Int) = receberHolderDaCategoria(categoriaDoProduto)
            categorias.remove(holder)
            withContext(Main) { _categoriasLiveData.value = categorias }
            // ao remover o ultimo produto de uma categoria, uma outra categoria sera
            // definida como selecionada mesmo se nao tivesse nenhuma categoria selecionada
            // antes da exclusao. Esse nao é o comportamento ideal e sera corrigido no futuro.
            val indiceSeguro = indice.coerceAtMost(categorias.size - 1)
            selecionarCategoria(categorias[indiceSeguro])

        } else {
            //  removo o item da lista
            val itensRv = ArrayList(_produtosLiveData.value!!)
            itensRv.remove(produto)
            _produtosLiveData.postValue(itensRv)

            // verifico se todos os itens foram comprados
            val (holder: CategoriaHolder, indice: Int) = receberHolderDaCategoria(categoriaDoProduto)
            holder.itensComprados = todosOsItensForamComprados(categoriaDoProduto)
            categorias[indice] = holder

            _categoriasLiveData.postValue(categorias)
        }

        calcularValores()
    }

    suspend fun buscarItemEmOutrasListas(produto: Produto): ArrayList<Produto> =
            ArrayList(ItemRepo.getItensPorNomeExato(produto, 6))

    /**
     * Verifica a categoria selecionada para saber se o usuario esta selecionando
     * uma nova categoria ou apenas desselecionando a atual.
     * @see FragListaDeCompras.initRvDeCategorias
     * */
    fun selecionarCategoriaPeloUsuario(novaSelecao: CategoriaHolder) = viewModelScope.launch(IO) {

        val categorias = _categoriasLiveData.value!!.map { it.clonar() } // deep copy

        if (novaSelecao.categoria.id == categSelecHolder?.categoria?.id) {
            // desseleciono todas as categorias
            categorias.forEach { it.selecionada = false }
                .also { categSelecHolder = null }

        } else {
            for (holder in categorias) {
                holder.selecionada = holder.categoria.id == novaSelecao.categoria.id
                if (holder.selecionada) categSelecHolder = holder
            }
        }

        _categoriasLiveData.postValue(ArrayList(categorias))
        carregarItens()
        calcularValores()

    }

    private suspend fun selecionarCategoria(novaSelecao: CategoriaHolder) = withContext(IO) {
        if (novaSelecao.categoria.id == categSelecHolder?.categoria?.id) return@withContext

        val categorias = _categoriasLiveData.value!!.map { it.clonar() } // deep copy

        for (holder in categorias) {
            holder.selecionada = holder.categoria.id == novaSelecao.categoria.id
            if (holder.selecionada) categSelecHolder = holder
        }

        _categoriasLiveData.postValue(ArrayList(categorias))

        carregarItens()
        calcularValores()

    }

    /**
     * Essa funçao é chamada sempre que um produto tem seu preço ou quantidade atualizados pelos dialogos
     * de ediçao rapida no fragmento de lista de compras, serve apenas para salvar o produto no banco
     * e notificar a alteraçao para a UI*/
    suspend fun aplicarPrecoOuQuantidadeeNotificar(
        alvo: Produto,
        preco: Float? = null,
        quantidade: Int? = null,
    ) = withContext(IO) {

        val clone = alvo.clonar()
        if (preco != null) clone.preco = preco
        if (quantidade != null) clone.quantidade = quantidade

        attItemNoBancoDeDados(clone)

        val itens = _produtosLiveData.value!!
        itens[itens.indexOf(alvo)] = clone
        _produtosLiveData.postValue(itens)
        calcularValores()
    }

    fun indiceDaCategoriaSelecionada() =
            if (categSelecHolder != null) receberHolderDaCategoria(categSelecHolder!!.categoria).second else -1

    /**
     * Calcula os valores dos itens na lista de compras atual
     * Despacha um Triple com a soma dos valores dos produtos por (COMPRADOS - CATEGORIA - LISTA)
     * */
    private suspend fun calcularValores() = withContext(IO) {

        val produtosDaCategoria = ArrayList<Produto>()
        val produtosComprados = ArrayList<Produto>()
        val produtosDaLista = ItemRepo.getItensNaLista(_listaLiveData.value?.id!!)

        for (produto in produtosDaLista) {
            if (produto.categoriaId == categSelecHolder?.categoria?.id) produtosDaCategoria.add(produto)
            if (produto.comprado) produtosComprados.add(produto)
        }


        val valores = Triple(
            produtosComprados.sumOf { it.valorTotal() },
            produtosDaCategoria.sumOf { it.valorTotal() },
            produtosDaLista.sumOf { it.valorTotal() },
        )

        _valoresLiveData.postValue(valores)
    }

    fun categoriaEditada(novaCategoria: Categoria, categoriaOriginal: Categoria) =
            viewModelScope.launch(IO) {

                CategoriaRepo.addAttCategoria(novaCategoria)

                val categorias = ArrayList(_categoriasLiveData.value!!)
                val (holderOriginal: CategoriaHolder, indice: Int) = receberHolderDaCategoria(categoriaOriginal)
                holderOriginal.categoria = novaCategoria
                categorias[indice] = holderOriginal
                ordenarCategorias(categorias)
                _categoriasLiveData.postValue(categorias)
            }

    fun removerCategoria(alvo: CategoriaHolder) = viewModelScope.launch(IO)
    {
        // marco a categoria como removida e salvo no db
        val holderCategoria = alvo.clonar()
        holderCategoria.categoria.removido = true
        CategoriaRepo.addAttCategoria(holderCategoria.categoria)

        // atualizo a interface
        val categorias = ArrayList(_categoriasLiveData.value!!)
        val (holderOriginal, indice) = receberHolderDaCategoria(alvo.categoria)
        categorias.removeAt(indice)
        withContext(Main) { _categoriasLiveData.value = categorias }

    }

    suspend fun categoriaEstaEmUso(categoria: Categoria): Boolean {
        return ItemRepo.getItensDaCategoria(categoria.id,1).isNotEmpty()

    }


}