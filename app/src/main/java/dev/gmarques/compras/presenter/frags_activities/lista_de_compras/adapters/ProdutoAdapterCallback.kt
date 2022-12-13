package dev.gmarques.compras.presenter.frags_activities.lista_de_compras.adapters

import dev.gmarques.compras.data.entidades.ProdutoEntidade

interface ProdutoAdapterCallback {

    fun produtoComprado(produtoEntidade: ProdutoEntidade, comprado: Boolean)
    fun produtoRemovido(produtoEntidade: ProdutoEntidade)
    fun editarProduto(produtoEntidade: ProdutoEntidade)
    fun precoEditado(produtoEntidade: ProdutoEntidade)
    fun quantidadeEditada(produtoEntidade: ProdutoEntidade)
}