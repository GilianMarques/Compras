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
import dev.gmarques.compras.utils.App
import dev.gmarques.compras.utils.ExtFun.Companion.toCurrency
import java.util.Collections

class ProductAdapter(val callback: Callback) : ListAdapter<Product, ProductAdapter.ListViewHolder>(ProductDiffCallback()) {

    private lateinit var itemTouchHelper: ItemTouchHelper
    private val utilList = mutableListOf<Product>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {

        val binding = DataBindingUtil.inflate<RvItemProductBinding>(
            LayoutInflater.from(parent.context), R.layout.rv_item_product, parent, false
        )

        return ListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        holder.bindData(getItem(position))
    }

    /**
     * Quando ha um evento de dragndrop, aplico a alteraçao na lista mutavel
     *  entao faço uma copia imutavel dela e mando pro diffuteils atraves da funçao *submitList*
     *  para que as diferenças sejam calculadas e o recyclerview atulizado automaticamente
     * */
    fun moveProduct(fromPosition: Int, toPosition: Int) {
        Collections.swap(utilList, fromPosition, toPosition)
        submitList(utilList.toList())
    }

    /**
     * Chamado pelo DragDropHelperCallback quando o ususario termina a açao de dragNdrop noo RV
     * Atualiza no banco de dados os produtos que tiveram suas posições alteradas  após o usuário
     * soltar a view  que estava sendo arrastada.
     */
    fun updateDraggedProducts() {
        for (i in utilList.indices) {
            val product = utilList[i]
            if (product.position != i) callback.rvProductsOnDragAndDrop(i, product)
        }
    }

    /**
     * Configura a instância de [ItemTouchHelper] que será usada para iniciar eventos de arrastar
     * (drag and drop) na lista do RecyclerView.
     *
     * Essa instância é necessária para vincular o manipulador de toque (handle) de uma View ao evento
     * de arrastar. O méto_do é chamado externamente para fornecer o [ItemTouchHelper] ao adapter.
     *
     * @param itemTouchHelper A instância de [ItemTouchHelper] responsável por gerenciar os eventos de
     * arrastar e soltar no RecyclerView.
     */
    fun attachItemTouchHelper(itemTouchHelper: ItemTouchHelper) {
        this.itemTouchHelper = itemTouchHelper
         
    }

    /**
     * Envia a nova lista ao Adapter e mantém uma cópia mutável da lista para suportar
     * funcionalidades adicionais, como arrastar e soltar (drag and drop).
     *
     * A cópia mutável, armazenada em `utilList`, é usada para manipulações diretas de itens,
     * garantindo que a funcionalidade de arrastar e soltar funcione corretamente em conjunto com o DiffUtil.
     *
     * @param list A nova lista de produtos a ser exibida. Pode ser nula, caso em que uma lista vazia será usada.
     */
    override fun submitList(list: List<Product>?) {
        super.submitList(list)
        utilList.clear()
        utilList.addAll(list ?: emptyList())
    }

    inner class ListViewHolder(private val binding: RvItemProductBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bindData(product: Product) {
            animarView()

            binding.apply {


                tvProductName.text = product.name
                tvProductInfo.text = product.info
                tvProductPrice.text = (product.price * product.quantity).toCurrency()
                tvProductQuantity.text = String.format(App.getContext().getString(R.string.un), product.quantity)
                cbBought.isChecked = product.hasBeenBought
            }

            setListeners(binding, product)
        }

        @SuppressLint("ClickableViewAccessibility")
        private fun setListeners(binding: RvItemProductBinding, product: Product) = binding.apply {
            cvChild.setOnLongClickListener {
                callback.rvProductsOnEditItemClick(product)
                true
            }
            cbBought.setOnCheckedChangeListener { _, isChecked ->
                callback.rvProductsOnBoughtItemClick(product, isChecked)
            }
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


        private fun animarView() {
            itemView.alpha = 0f
            itemView.animate().alpha(1f).setDuration(150).setStartDelay(10L * adapterPosition).start()
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
            // deve comparar toda a informação que é exibida na view pro usuario
            return oldItem.name == newItem.name &&
                    oldItem.info == newItem.info &&
                    oldItem.price == newItem.price &&
                    oldItem.quantity == newItem.quantity &&
                    oldItem.hasBeenBought == newItem.hasBeenBought
        }
    }

    interface Callback {
        fun rvProductsOnDragAndDrop(toPosition: Int, product: Product)
        fun rvProductsOnEditItemClick(product: Product)
        fun rvProductsOnBoughtItemClick(product: Product, isBought: Boolean)
    }

}
