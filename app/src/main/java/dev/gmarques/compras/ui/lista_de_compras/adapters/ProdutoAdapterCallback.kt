package dev.gmarques.compras.ui.lista_de_compras.adapters

import dev.gmarques.compras.entidades.Produto

interface ProdutoAdapterCallback {

    fun produtoComprado(produto: Produto, comprado: Boolean)
    fun produtoRemovido(produto: Produto)
    fun editarProduto(produto: Produto)
    fun precoEditado(produto: Produto)
    fun qtdEditada(produto: Produto)
}