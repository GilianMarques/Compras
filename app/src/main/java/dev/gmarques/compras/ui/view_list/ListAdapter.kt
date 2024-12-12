package dev.gmarques.compras.ui.view_list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import dev.gmarques.compras.R
import dev.gmarques.compras.data.data.model.ShopList
import dev.gmarques.compras.databinding.RvItemListBinding

class ListAdapter(
    private val onDragAndDrop: (fromPosition: Int, toPosition: Int) -> Unit,
) : RecyclerView.Adapter<ListAdapter.ListViewHolder>() {
    private val lists: MutableList<ShopList> = arrayListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {

        val binding = DataBindingUtil.inflate<RvItemListBinding>(
            LayoutInflater.from(parent.context), R.layout.rv_item_list, parent, false
        )

        return ListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        holder.bind(lists[position])
    }

    override fun getItemCount(): Int = lists.size

    fun moveItem(fromPosition: Int, toPosition: Int) {
        val item = lists.removeAt(fromPosition)
        lists.add(toPosition, item)
        onDragAndDrop(fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)
    }

    fun addItems(newData: List<ShopList>) {
        newData.forEach { newList ->
            lists.add(newList)
            notifyItemInserted(lists.size - 1)
        }
    }

    class ListViewHolder(private val binding: RvItemListBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(list: ShopList) {
            binding.tvListName.text = list.name
        }
    }
}
