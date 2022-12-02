package dev.gmarques.compras.ui.lista_de_compras

import androidx.recyclerview.widget.DiffUtil
import dev.gmarques.compras.entidades.Categoria

//https://blog.mindorks.com/the-powerful-tool-diff-util-in-recyclerview-android-tutorial

class CategoriaDiffCallback(
    private val listaAntiga: ArrayList<Categoria>,
    private val novaLista: ArrayList<Categoria>,
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = listaAntiga.size

    override fun getNewListSize(): Int = novaLista.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return listaAntiga[oldItemPosition].id == novaLista[newItemPosition].id
    }

    override fun areContentsTheSame(oldPosition: Int, newPosition: Int): Boolean =
        listaAntiga[oldPosition] === novaLista[newPosition]


    override fun getChangePayload(oldPosition: Int, newPosition: Int): Any? {
        return super.getChangePayload(oldPosition, newPosition)
    }
}