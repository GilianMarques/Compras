package dev.gmarques.compras.ui.lista_de_compras

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import dev.gmarques.compras.databinding.RvItemViewBinding
import dev.gmarques.compras.objetos.Item

class ItemAdapter(
    private var itens: MutableList<Item>,
    private val clickCallback: (Item, Int) -> Unit,
) : Adapter<ItemAdapter.ViewHolder>() {


    class ViewHolder(private val bindingView: RvItemViewBinding, val click: (Item, Int) -> Unit) :
        RecyclerView.ViewHolder(bindingView.root) {

        fun bind(item: Item, position: Int) {
            bindingView.item = item
            bindingView.root.setOnClickListener {
                click(item, position)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RvItemViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding, clickCallback)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(itens[position], position)


    override fun getItemCount() = itens.size

    @SuppressLint("NotifyDataSetChanged")
    fun attLista(itens: ArrayList<Item>) {
        this.itens = itens
        notifyDataSetChanged()
    }

    fun addItem(item: Item, posicao: Int) {
        itens.add(posicao, item)
        notifyItemInserted(posicao)
    }

    fun attIem(item: Item, posicao: Int) {
        itens[posicao] = item
        notifyItemChanged(posicao)
    }

    fun removerItem(posicao: Int) {
        itens.removeAt(posicao)
        notifyItemRemoved(posicao)
    }
}