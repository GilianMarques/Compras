package dev.gmarques.compras.ui.lista_de_compras

import androidx.recyclerview.widget.DiffUtil
import dev.gmarques.compras.objetos.Item


class ItemDiffUtilCallback : DiffUtil.ItemCallback<Item>() {

    override fun areItemsTheSame(oldItem: Item, newItem: Item): Boolean = oldItem == newItem

    override fun areContentsTheSame(oldItem: Item, newItem: Item): Boolean = oldItem == newItem

}