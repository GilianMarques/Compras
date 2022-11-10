package dev.gmarques.compras.ui.lista_de_compras

import android.app.Application
import android.util.Log
import androidx.annotation.MainThread
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dev.gmarques.compras.io.repositorios.CategoriaRepo
import dev.gmarques.compras.io.repositorios.ItemRepo
import dev.gmarques.compras.io.repositorios.ListaRepo
import dev.gmarques.compras.objetos.Categoria
import dev.gmarques.compras.objetos.Item
import dev.gmarques.compras.objetos.Lista
import dev.gmarques.compras.viewmodel_utils.MutableListLiveData
import kotlinx.coroutines.launch


class FragListaDeComprasViewModel(appContext: Application) : AndroidViewModel(appContext) {


    //liveData pra notificar o ui controller sobre alteraçoes na lista
    private val _categoriasLiveData = MutableListLiveData<Categoria>()
    val categoriasLiveData get() = _categoriasLiveData

    //liveData pra notificar o ui controller se a categoriaAtual ficar nula por ficar sem itens, ou outro motivo qqer
    private val _categoriaselecionadaLiveData = MutableLiveData<Int>()
    val categoriaselecionadaLiveData get() = _categoriaselecionadaLiveData

    //liveData pra notificar o ui controller se os itens forem atualizados (pra recarregar o recyclerview)
    private val _itensRecarregadosLiveData = MutableLiveData<Int>()
    val itensRecarregadosLiveData get() = _itensRecarregadosLiveData

    // lista de compras atual
    private val _listaLiveData = MutableLiveData<Lista>()
    val listaLiveData: LiveData<Lista> get() = _listaLiveData

    // lista de categorias exibida no recyclerview
    var categorias: ArrayList<Categoria> = ArrayList()

    // categoria selecionada no recyclerview de categorias
    private var categoriaSelecionada: Categoria? = null

    val itens: ArrayList<Item> = ArrayList()

    init {
        viewModelScope.launch {
            _listaLiveData.value = ListaRepo.getUtimaOuQualquerLista() ?: Lista.PADRAO

            @Suppress("ControlFlowWithEmptyBody") if (Lista.PADRAO == _listaLiveData.value) {
            } // TODO: criar uma lista a partir daqui

            carregarCategoriasPresentesNaListaENotificar()
            carregarItensENotificar()

        }
    }

    /**
     * carrega os itens da lista recebida de acordo com a categoria selecionada
     * no momento
     * @return os itens direto do DB*/
    suspend fun carregarItensENotificar() {
        //se der pau aqui tem que verificar no metodo chamador o estado da lista
        val listaId = _listaLiveData.value!!.id
        val data = if (categoriaSelecionada != null) ArrayList(ItemRepo.getItensNaListaPorCategoria(
            listaId,
            categoriaSelecionada!!.id))
        else ArrayList(ItemRepo.getItensNaLista(listaId))

        itens.clear()
        itens.addAll(data)
        ordenarLista()
        Log.d("USUK", "FragListaDeComprasViewModel.carregarItensENotificar: lendo do db ")
        // avisa que houve atualizaçao (pela MainThread)
        _itensRecarregadosLiveData.postValue(1)

    }

    @MainThread
    private suspend fun carregarCategoriasPresentesNaListaENotificar() {
        Log.d("USUK", "FragListaDeComprasViewModel.carregarCategoriasPresentesNaListaENotificar: ")
        val tempData: HashMap<String, Categoria> = HashMap()
        val itens = ItemRepo.getItensNaLista(_listaLiveData.value?.id!!)
        for (item in itens) tempData[item.categoriaId] = CategoriaRepo.getCategoria(item)

        categorias = ArrayList(tempData.values.sortedWith(compareBy { it.nome }))
        _categoriasLiveData.colecaoAtualizada()// notifica Ui
    }

    fun categoriaSelecionada(categoria: Categoria?) {
        Log.d("USUK",
            "FragListaDeComprasViewModel.".plus("categoriaSelecionada() atual ${categoriaSelecionada?.nome} nova = ${categoria?.nome}"))
        categoriaSelecionada = if (categoria == categoriaSelecionada) null else categoria
    }

    fun getCategoriaSelecionada() = categoriaSelecionada?.id

    suspend fun addItem(item: Item) = ItemRepo.addOuAtualizar(item)

    /***
     * atualiza as categorias pra refletir a inserçao de um item
     */
    fun ajustarCategoriasItemAdicionado(item: Item): Pair<Int, Int>? {
        val categoria = CategoriaRepo.getCategoria(item)

        if (!categorias.contains(categoria)) {
            // adiciono uma categoria ao Rv de categorias se o novo item for de uma categoria nao presente na lista até o momento
            categorias.add(categoria)
            _categoriasLiveData.itemAdicionado(categoria, categorias.size - 1)// notifica Ui
        }

        if (categoriaSelecionada?.id == item.categoriaId || categoriaSelecionada == null) {
            itens.add(0, item)
            return ordenarItem(item)
        } else return null

    }

    suspend fun attItem(item: Item) = ItemRepo.addOuAtualizar(item)

    /***
     * atualiza as categorias pra refletir a atualizacao de um item
     * um item atualizado pode ter side movido para outra categoria, essa deve ser
     * adicionada na lista de categorias caso ja nao estaja la, uma categoria vazia
     * deve ser removida da lista de categorias existem outras alteraçoes que devem
     * ser feitas e podem ser causadas pela simples alteraçaod e um item, essa funçao
     * busca fazer isso de maneira simples ja que as coleçoes sao itens e categorias sao pequenas*/
    @MainThread
    suspend fun ajustarCategoriasItemEditado() {
        Log.d("USUK", "FragListaDeComprasViewModel.ajustarCategorias: ")
        //atualizo as categorias caso o item tenha tido sua categotia alterada
        carregarCategoriasPresentesNaListaENotificar()

        // se a categoriaSelecionada ja nao tem nenhum item des-seleciono ela
        if (!categorias.contains(categoriaSelecionada)) {
            categoriaSelecionada(null)
            // avisa a ui que a categoriaSelecionada agora é nula
            _categoriaselecionadaLiveData.postValue(1)
        }

    }


    suspend fun itemComprado(item: Item): Pair<Int, Int> {
        // salvar no DB
        ItemRepo.addOuAtualizar(item)
        return ordenarItem(item)
    }

    private fun ordenarItem(item: Item): Pair<Int, Int> {
        val copia = ArrayList(itens)
        ordenarLista()

        /* for (i in 0 until itens.size) {
             Log.d("USUK",
                 "FragListaDeComprasViewModel.ordenarItem: $i:   ${copia[i].nome} -> ${itens[i].nome}")
         }*/
        return copia.indexOf(item) to itens.indexOf(item)
    }

    private fun ordenarLista() {
        var data: List<Item> = itens.sortedWith(compareBy { it.nome })
        data = data.sortedWith(compareBy { it.comprado })
        itens.clear()
        itens.addAll(data)

    }

    suspend fun removerItem(item: Item): Boolean {
        item.removido = true
        ItemRepo.addOuAtualizar(item)

        if (categoriaSelecionada != null
            && categoriaSelecionada!!.id == item.categoriaId
            && ItemRepo.getItensNaListaPorCategoria(_listaLiveData.value!!.id, item.categoriaId)
                .isEmpty()
        ) {
            ajustarCategoriasItemEditado() // vai recarregar as categorias, removendo a que esta vazia
            categoriaSelecionada(null) // remove a categoria selecionada
            _categoriaselecionadaLiveData.postValue(1) // avisa a ui que a categoriaSelecionada agora é nula
            carregarItensENotificar() // recarrega todos os itens da lista do DB
            return true // tudo foi atualizado
        } else {
            itens.remove(item)
            return false // remover o item do rv manualmente
        }
    }

    suspend fun precoItem(item: Item): ArrayList<Item> =
        ArrayList(ItemRepo.getItensPorNomeExato(item,6))


}