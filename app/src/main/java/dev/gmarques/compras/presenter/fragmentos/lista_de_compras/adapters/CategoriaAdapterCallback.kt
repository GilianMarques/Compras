package dev.gmarques.compras.presenter.fragmentos.lista_de_compras.adapters

import dev.gmarques.compras.presenter.entidades.CategoriaUi

interface CategoriaAdapterCallback {


    fun categoriaSelecionada(holderCategoria: CategoriaUi)
    fun categoriaPressionada(holderCategoria: CategoriaUi)
}