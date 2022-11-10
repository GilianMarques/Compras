package dev.gmarques.compras.ui.lista_de_compras

import dev.gmarques.compras.objetos.Item

interface ItemAdapterCallback {

    fun itemComprado(itemAtualizado: Item, position: Int)
    fun itemRemovido(item: Item, position: Int)
    fun editarItem(item: Item, position: Int)
    fun precoEditado(item: Item, position: Int)
    fun qtdEditada(item: Item, position: Int)
}