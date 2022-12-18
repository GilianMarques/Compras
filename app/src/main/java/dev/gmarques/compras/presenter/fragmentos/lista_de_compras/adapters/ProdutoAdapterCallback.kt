package dev.gmarques.compras.presenter.fragmentos.lista_de_compras.adapters

import dev.gmarques.compras.domain.entidades.Produto

interface ProdutoAdapterCallback {

    fun produtoComprado(produto: Produto, comprado: Boolean)
    fun produtoRemovido(produto: Produto)
    fun editarProduto(produto: Produto)
    fun precoEditado(produto: Produto)
    fun quantidadeEditada(produto: Produto)
}