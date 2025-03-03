package dev.gmarques.compras.ui.products

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import dev.gmarques.compras.App
import dev.gmarques.compras.R
import dev.gmarques.compras.data.model.Product
import dev.gmarques.compras.databinding.RvItemProductBinding
import dev.gmarques.compras.domain.model.ProductWithCategory
import dev.gmarques.compras.domain.utils.ExtFun.Companion.toCurrency
import dev.gmarques.compras.ui.Vibrator
import java.util.Collections

class ProductAdapter(private val darkModeEnabled: Boolean, val callback: Callback) :
    ListAdapter<ProductWithCategory, ProductAdapter.ProductViewHolder>(ProductDiffCallback()) {

    private var itemTouchHelper: ItemTouchHelper? = null
    private val utilList = mutableListOf<ProductWithCategory>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {

        val binding = DataBindingUtil.inflate<RvItemProductBinding>(
            LayoutInflater.from(parent.context), R.layout.rv_item_product, parent, false
        )

        return ProductViewHolder(binding, callback, itemTouchHelper!!)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bindData(darkModeEnabled, getItem(position))
    }

    fun moveProduct(fromPosition: Int, toPosition: Int) {
        Collections.swap(utilList, fromPosition, toPosition)
        submitList(utilList.toList())
    }

    fun updateDraggedProducts() {
        for (i in utilList.indices) {
            val prodWithCat = utilList[i]
            if (prodWithCat.product.position != i) callback.rvProductsOnDragAndDrop(i, prodWithCat.product)
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
    override fun submitList(list: List<ProductWithCategory>?) {
        utilList.clear()
        utilList.addAll(list ?: emptyList())
        super.submitList(list)
    }

    class ProductViewHolder(
        private val binding: RvItemProductBinding,
        val callback: Callback,
        private val itemTouchHelper: ItemTouchHelper,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bindData(darkModeEnabled: Boolean, productWithCategory: ProductWithCategory) {
            clearListener()

            val product = productWithCategory.product
            binding.apply {

                tvProductName.text = product.name
                tvProductInfo.text = product.info
                tvProductPrice.text = product.price.toCurrency()
                tvProductQuantity.text = String.format(App.getContext().getString(R.string.un), product.quantity)
                cbBought.isChecked = product.hasBeenBought
                tvCategoryName.text = productWithCategory.category.name
                tvProductInfo.visibility = if (product.info.isNotEmpty()) VISIBLE else GONE
            }

            setListeners(binding, product)
        }

        private fun clearListener() = binding.apply {
            cbBought.setOnCheckedChangeListener(null)
        }

        @SuppressLint("ClickableViewAccessibility")
        private fun setListeners(binding: RvItemProductBinding, product: Product) = binding.apply {
            cvChild.setOnLongClickListener {
                callback.rvProductsOnEditItemClick(product)
                true
            }

            cbBought.setOnCheckedChangeListener { _, checked ->
                Vibrator.interaction()
                callback.rvProductsOnBoughtItemClick(product, checked)
            }

            ivHandle.setOnTouchListener { it, motionEvent ->
                when (motionEvent.action) {
                    MotionEvent.ACTION_DOWN -> {
                        // Inicia o drag and drop
                        itemTouchHelper.startDrag(this@ProductViewHolder)
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

            tvProductPrice.setOnClickListener { Vibrator.interaction(); callback.rvProductsOnPriceClick(product) }
            tvProductQuantity.setOnClickListener { Vibrator.interaction(); callback.rvProductsOnQuantityClick(product) }
            tvProductInfo.setOnClickListener { Vibrator.interaction(); callback.rvProductsOnInfoClick(product) }

        }
    }

    /**
     * Compara as listas de dados pra atualizar o recyclerview
     * */
    class ProductDiffCallback : DiffUtil.ItemCallback<ProductWithCategory>() {

        override fun areItemsTheSame(oldItem: ProductWithCategory, newItem: ProductWithCategory): Boolean {
            return oldItem.product.id == newItem.product.id
        }

        override fun areContentsTheSame(oldItem: ProductWithCategory, newItem: ProductWithCategory): Boolean {
            // deve comparar toda a informação que é exibida na view pro usuario
            return oldItem.product == newItem.product
        }
    }

    interface Callback {
        fun rvProductsOnDragAndDrop(toPosition: Int, product: Product)
        fun rvProductsOnEditItemClick(product: Product)
        fun rvProductsOnPriceClick(product: Product)
        fun rvProductsOnQuantityClick(product: Product)
        fun rvProductsOnInfoClick(product: Product)
        fun rvProductsOnBoughtItemClick(product: Product, isBought: Boolean)
    }

}
