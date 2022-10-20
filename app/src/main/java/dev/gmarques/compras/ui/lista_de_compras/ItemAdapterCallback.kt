package dev.gmarques.compras.ui.lista_de_compras

import dev.gmarques.compras.objetos.Item

interface ItemAdapterCallback {

    fun itemComprado(item: Item, position: Int)
    fun itemPressionado(item: Item, position: Int)
}