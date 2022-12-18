package dev.gmarques.compras.presenter.fragmentos.produto_io

import dev.gmarques.compras.domain.entidades.Categoria

interface CategoriaAdapterCallback {
    /***
     * chamado sempre que o usuario seleciona uma categoria no recyclerview
     */
    fun categoriaSelecionada(categoria: Categoria)

    /**
     * Chamado quendo o usuario quer adicionar uma nova categoria
     */
    fun adicionarCategoria()
}