package dev.gmarques.compras.presenter.frags_activities.produto_io

import dev.gmarques.compras.data.entidades.CategoriaEntidade

interface CategoriaAdapterCallback {
    /***
     * chamado sempre que o usuario seleciona uma categoria no recyclerview
     */
    fun categoriaSelecionada(categoria: CategoriaEntidade)

    /**
     * Chamado quendo o usuario quer adicionar uma nova categoria
     */
    fun adicionarCategoria()
}