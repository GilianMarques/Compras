package dev.gmarques.compras.ui.main

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import dev.gmarques.compras.R
import dev.gmarques.compras.data.model.ShopList
import dev.gmarques.compras.databinding.RvItemShopListBinding
import dev.gmarques.compras.domain.utils.ExtFun.Companion.adjustSaturation
import kotlin.math.min

class ShopListAdapter(
    val darkModeEnable: Boolean,
    private val onItemClick: (ShopList) -> Any,
    private val onLongItemClick: (ShopList) -> Any,
) :
    ListAdapter<ShopList, ShopListAdapter.ListViewHolder>(ShopListDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {

        val binding = DataBindingUtil.inflate<RvItemShopListBinding>(
            LayoutInflater.from(parent.context), R.layout.rv_item_shop_list, parent, false
        )


        return ListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ListViewHolder(private val binding: RvItemShopListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(shopList: ShopList) = binding.apply {

            animate()
            tvListName.text = shopList.name

            cvChild.setOnClickListener { onItemClick(shopList) }
            cvChild.setOnLongClickListener { onLongItemClick(shopList); return@setOnLongClickListener true }

            val saturatedColor = shopList.color.adjustSaturation(1.5f)
            ivListIcon.background?.mutate()?.apply {
                if (this is GradientDrawable) setColor(saturatedColor)
            }

            if (!darkModeEnable) (cvChild.background as? GradientDrawable)!!.apply {
                mutate()
                setColor(shopList.color)
                setStroke(0, Color.TRANSPARENT)
            }
        }

        private fun animate() {
            itemView.clearAnimation()
            itemView.alpha = 0f
            itemView.animate().alpha(1f).setDuration(450)
                .setStartDelay(min(50L * adapterPosition, 500)).start()
        }

    }

    class ShopListDiffCallback : DiffUtil.ItemCallback<ShopList>() {


        override fun areItemsTheSame(oldItem: ShopList, newItem: ShopList): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ShopList, newItem: ShopList): Boolean {
            return oldItem.name == newItem.name
                    && oldItem.color == newItem.color
        }
    }

}
