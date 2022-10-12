package dev.gmarques.compras.ui.lista_de_compras

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dev.gmarques.compras.io.database.RoomDb
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

    // lista itens exibida no recyclerview, qdo uma categoria é selecionada
    // os itens dessa lista sofrem alteração
    var itensCategoria: ArrayList<Item> = ArrayList()

    // lista de todos os itens
    private var todosItens: ArrayList<Item> = ArrayList()

    // categoria selecionada no recyclerview de categorias
    private var categoriaSelecionada: Categoria? = null

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val lista = ListaRepo.getUtimaOuQualquerLista() ?: Lista.PADRAO

            if (Lista.PADRAO == lista) {

                // TODO: criar uma lista a partir daqui
            }

            todosItens = ArrayList(RoomDb.getInstancia().itemDao().getTodos(lista.id))
            itensCategoria = ArrayList(todosItens)
            categorias = carregarCategorias()

            withContext(Dispatchers.Main) {


                _listaLiveData.value = lista
                _itensLiveData.colecaoAtualizada()// notifica Ui
                _categoriasLiveData.colecaoAtualizada()// notifica Ui
            }
        }
    }

    private fun carregarCategorias(): java.util.ArrayList<Categoria> {
        val tempData: HashMap<String, Categoria> = HashMap()
        for (item in itensCategoria) tempData[item.categoriaId] = CategoriaRepo.getCategoria(item)
        return ArrayList(tempData.values)
    }

    fun itemClick(item: Item, posicao: Int) {
        Log.d("USUK",
            "FragListaDeComprasViewModel.".plus("itemClick() item = $item, posicao = $posicao"))
    }

    fun categoriaClick(categoria: Categoria) {
        categoriaSelecionada = if (categoria == categoriaSelecionada) null else categoria

        itensCategoria.clear()

        if (categoriaSelecionada != null) {
            for (item: Item in todosItens)
                if (item.categoriaId == categoria.id) itensCategoria.add(item)
        } else itensCategoria = ArrayList(todosItens)

        _itensLiveData.colecaoAtualizada()

    }

    fun addItem() {

    }

    private fun addItem(item: Item) {

        todosItens.add(0, item)

        if (categoriaSelecionada?.id == item.categoriaId || categoriaSelecionada == null) {
            itensCategoria.add(0, item)
            _itensLiveData.itemAdicionado(item, 0)
        }
        ItemRepo.addOuAtualizar(item)

    }

}