package dev.gmarques.compras.ui.lista_de_compras

import dev.gmarques.compras.entidades.Produto

interface ItemAdapterCallback {

    fun produtoComprado(produto: Produto, comprado: Boolean, indice: Int)
    fun produtoRemovido(produto: Produto, indice: Int)
    fun editarProduto(produto: Produto, indice: Int)
    fun precoEditado(produto: Produto, indice: Int)
    fun qtdEditada(produto: Produto, indice: Int)
}