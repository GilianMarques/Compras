package dev.gmarques.compras.ui.products

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import dev.gmarques.compras.R
import dev.gmarques.compras.data.data.model.Product
import dev.gmarques.compras.databinding.RvItemProductBinding
import dev.gmarques.compras.utils.ExtFun.Companion.toCurrency

class ProductAdapter(
    private val onDragAndDrop: (fromPosition: Int, toPosition: Int) -> Unit,
) : ListAdapter<Product, ProductAdapter.ListViewHolder>(ProductDiffCallback()) {

    private lateinit var itemTouchHelper: ItemTouchHelper
    private val utilList = mutableListOf<Product>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {

        val binding = DataBindingUtil.inflate<RvItemProductBinding>(
            LayoutInflater.from(parent.context), R.layout.rv_item_product, parent, false
        )

        return ListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun moveItem(fromPosition: Int, toPosition: Int) {
        //Quando ha uma  açao de dragndrop, aplico a alteraçao na lista mutavel
        // entao faço uma copia imutavel dela e mando pro diffuteils atraves da funçao *submitList*
        // para que as diferenças sejam calculadas e o recyclerview atulizado automaticamente
        val item = utilList.removeAt(fromPosition)
        utilList.add(toPosition, item)
        onDragAndDrop(fromPosition, toPosition)
        submitList(utilList.toList())
    }

    fun setItemTouchHelper(itemTouchHelper: ItemTouchHelper) {
        this.itemTouchHelper = itemTouchHelper
    }

    override fun submitList(list: List<Product>?) {
        super.submitList(list)
        // faço uma copia mutavel da lista que me permitira adicionar e remover itens
        // uso isso para implementar a funcionalidade de drag and drop junto com o diff utils
        // veja  a funçao *moveItem*
        utilList.clear()
        utilList.addAll(list ?: emptyList())


    }

    inner class ListViewHolder(private val binding: RvItemProductBinding) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("ClickableViewAccessibility")
        fun bind(product: Product) {
            itemView.alpha = 0f
            itemView.animate()
                .alpha(1f)
                .setDuration(150)
                .setStartDelay(10L * adapterPosition)
                .start()


            binding.apply {

                tvProductName.text = product.name
                tvProductPrice.text = product.price.toCurrency()

                ivHandle.setOnTouchListener { it, motionEvent ->

                    when (motionEvent.action) {
                        MotionEvent.ACTION_DOWN -> {
                            // Inicia o drag and drop
                            itemTouchHelper.startDrag(this@ListViewHolder)
                            return@setOnTouchListener true
                        }
                        MotionEvent.ACTION_UP -> {
                            // Garante que performClick seja chamado para acessibilidade
                            it.performClick()
                            return@setOnTouchListener true
                        }
                        else -> {
                            // Permite que outros manipuladores tratem o evento
                            return@setOnTouchListener false
                        }
                    }
                }


            }
        }
    }

    /**
     * Compara as listas de dados pra atualizar o recyclerview
     * */
    class ProductDiffCallback : DiffUtil.ItemCallback<Product>() {

        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem == newItem
        }
    }


}
