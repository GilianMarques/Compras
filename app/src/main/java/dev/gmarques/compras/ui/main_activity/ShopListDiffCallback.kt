package dev.gmarques.compras.ui.main_activity

import androidx.recyclerview.widget.DiffUtil
import dev.gmarques.compras.data.data.model.ShopList

class ShopListDiffCallback : DiffUtil.ItemCallback<ShopList>() {


    override fun areItemsTheSame(oldItem: ShopList, newItem: ShopList): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: ShopList, newItem: ShopList): Boolean {
        return oldItem == newItem
    }
}