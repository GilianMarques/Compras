package dev.gmarques.compras.ui.lista_de_compras.adapters

import dev.gmarques.compras.entidades.helpers.CategoriaHolder

interface CategoriaAdapterCallback {


    fun categoriaSelecionada(holderCategoria: CategoriaHolder)
    fun categoriaPressionada(holderCategoria: CategoriaHolder)
}