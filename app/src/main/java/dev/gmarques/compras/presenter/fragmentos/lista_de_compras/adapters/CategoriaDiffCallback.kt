package dev.gmarques.compras.presenter.fragmentos.lista_de_compras.adapters

import androidx.recyclerview.widget.DiffUtil
import dev.gmarques.compras.presenter.entidades.CategoriaUi

//https://blog.mindorks.com/the-powerful-tool-diff-util-in-recyclerview-android-tutorial

class CategoriaDiffCallback(
    private val listaAntiga: ArrayList<CategoriaUi>,
    private val novaLista: ArrayList<CategoriaUi>,
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = listaAntiga.size

    override fun getNewListSize(): Int = novaLista.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return listaAntiga[oldItemPosition].categoria.id == novaLista[newItemPosition].categoria.id
    }

    override fun areContentsTheSame(oldPosition: Int, newPosition: Int): Boolean =
        listaAntiga[oldPosition] == novaLista[newPosition]


    override fun getChangePayload(oldPosition: Int, newPosition: Int): Any? {
        return super.getChangePayload(oldPosition, newPosition)
    }
}