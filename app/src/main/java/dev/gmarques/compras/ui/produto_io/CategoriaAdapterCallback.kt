package dev.gmarques.compras.ui.produto_io

import dev.gmarques.compras.entidades.Categoria

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