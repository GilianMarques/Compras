package dev.gmarques.compras.ui.lista_de_compras

import android.app.Application
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class FragListaDeComprasViewModel(appContext: Application) : AndroidViewModel(appContext) {

    //liveData pra notificar o uicontroller sobre alteraçoes na lista
    private val _itensLiveData = MutableListLiveData<Item>()
    val itensLiveData get() = _itensLiveData

    //liveData pra notificar o uicontroller sobre alteraçoes na lista
    private val _categoriasLiveData = MutableListLiveData<Categoria>()
    val categoriasLiveData get() = _categoriasLiveData

    // lista de compras atual
    private val _listaLiveData = MutableLiveData<Lista>()
    val listaLiveData: LiveData<Lista> get() = _listaLiveData

    // lista de categorias exibida no recyclerview
    var categorias: ArrayList<Categoria> = ArrayList()

    // lista dos itens sendo exibidos
    var itens: ArrayList<Item> = ArrayList()

    // categoria selecionada no recyclerview de categorias
    private var categoriaSelecionada: Categoria? = null


    init {
        viewModelScope.launch(Dispatchers.IO) {
            val lista = ListaRepo.getUtimaOuQualquerLista() ?: Lista.PADRAO

            if (Lista.PADRAO == lista) {

                // TODO: criar uma lista a partir daqui
            }

            carregarItens(lista.id)
            carregarCategorias(lista.id)

            withContext(Dispatchers.Main) {
                _listaLiveData.value = lista
                _itensLiveData.colecaoAtualizada()// notifica Ui
                _categoriasLiveData.colecaoAtualizada()// notifica Ui
            }
        }
    }

    private suspend fun carregarItens(listaId: String = _listaLiveData.value?.id!!) {
        itens = if (categoriaSelecionada != null)
            ArrayList(ItemRepo.getItensNaListaPorCategoria(listaId, categoriaSelecionada!!.id))
        else ArrayList(ItemRepo.getItensNaLista(listaId))

        ordenarLista()
    }

    private suspend fun carregarCategorias(listaId: String = _listaLiveData.value?.id!!) {
        val tempData: HashMap<String, Categoria> = HashMap()
        val itens = ItemRepo.getItensNaLista(listaId)
        for (item in itens) tempData[item.categoriaId] = CategoriaRepo.getCategoria(item)

        categorias = ArrayList(tempData.values)
    }


    fun categoriaSelecionada(categoria: Categoria) = viewModelScope.launch {
        categoriaSelecionada = if (categoria == categoriaSelecionada) null else categoria
        carregarItens()
        _itensLiveData.colecaoAtualizada()
    }

    fun getCategoriaSelecionada() = categoriaSelecionada?.id


    fun addItem(item: Item) {

        ItemRepo.addOuAtualizar(item)
        val categoria = CategoriaRepo.getCategoria(item)

        if (categoriaSelecionada?.id == item.categoriaId || categoriaSelecionada == null) {
            itens.add(0, item)
            _itensLiveData.itemAdicionado(item, 0)
        }

        // adiciono uma categoria ao Rv de categorias se o novo item for de uma categoria nao presente na lista até o momento
        if (!categorias.contains(categoria)) {
            categorias.add(categoria)
            _categoriasLiveData.itemAdicionado(categoria, categorias.size - 1)// notifica Ui
        }

    }

    fun attItem(item: Item, pos: Int) {

        ItemRepo.addOuAtualizar(item)

        val categoria = CategoriaRepo.getCategoria(item)

        if (categoriaSelecionada?.id == item.categoriaId || categoriaSelecionada == null) {
            itens[pos] = item
            _itensLiveData.itemAtualizado(item, pos)
        } else if ( categoriaSelecionada!!.id != item.categoriaId) {
            // se o item nao pertence mais a essa categoria remove ele da lista
            itens.removeAt(pos)
            _itensLiveData.itemRemovido(item, pos)
        }

        // adiciono uma categoria ao Rv de categorias se o novo item for de uma categoria nao presente na lista até o momento
        if (!categorias.contains(categoria)) {
            categorias.add(categoria)
            _categoriasLiveData.itemAdicionado(categoria, categorias.size - 1)// notifica Ui
        }

    }

    fun itemComprado(item: Item): Pair<Int, Int> {
        // salvar no DB
        ItemRepo.addOuAtualizar(item)

        /*essa funçao sera diferente quando a alteraçao for feita na nuvem pq o item pode nao estar na tela desse aparelho quando a outra pessoa fizer a alteraçao*/
        /*mover item pro final da lista*/
        val copia = ArrayList(itens)
        ordenarLista()
        return copia.indexOf(item) to itens.indexOf(item)
    }

    private fun ordenarLista() {
        var x: List<Item> = itens.sortedWith(compareBy { it.nome })
        x = x.sortedWith(compareBy { it.comprado })
        itens.clear()
        itens.addAll(x)

    }


}