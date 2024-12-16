package dev.gmarques.compras.ui.products

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
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
    fun dragProducts(fromPosition: Int, toPosition: Int) {

        val product = utilList.removeAt(fromPosition)
        utilList.add(toPosition, product)
        submitList(utilList.toList())
    }


    /**
     * Atualiza no banco de dados os produtos que tiveram suas posições alteradas  após o usuário
     * soltar a view  que estava sendo arrastada.
     *
     * Esta função percorre um intervalo de índices definidos pelos valores `biggerValue` e `smallerValue`,
     * chamando um callback para atualizar os produtos correspondentes no banco de dados.
     *
     * @param biggerValue O maior índice dentro do intervalo afetado.
     * @param smallerValue O menor índice dentro do intervalo afetado.
     * @throws IllegalArgumentException Se `biggerValue` for menor que `smallerValue`.
     */
    fun updateDraggedProducts(biggerValue: Int, smallerValue: Int) {
        require(biggerValue >= smallerValue) { "biggerValue deve ser maior ou igual a smallerValue" }

        for (i in smallerValue..biggerValue) {
            callback.rvProductsOnDragAndDrop(i, getItem(i))
        }
    }


    fun setItemTouchHelper(itemTouchHelper: ItemTouchHelper) {
        this.itemTouchHelper = itemTouchHelper
    }

    /**
     * Submete uma nova lista ao Adapter e mantém uma cópia mutável da lista para suportar
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

        private var hideableViews = emptyList<View>()

        fun bindData(product: Product) {
            animarView()

            binding.apply {

                hideableViews = listOf(tvEditPriceUnity, tvEditQuantity, tvEditItem, divider)

                tvProductName.text = product.name
                tvProductInfo.text = product.info
                tvProductPrice.text = (product.price * product.quantity).toCurrency()
                tvProductQuantity.text = String.format(App.getContext().getString(R.string.un), product.quantity)
                tvEditQuantity.text = tvProductQuantity.text
                tvEditPriceUnity.text = product.price.toCurrency()
                cbBought.isChecked = product.isBought
            }

            setListeners(binding, product)
        }

        @SuppressLint("ClickableViewAccessibility")
        private fun setListeners(binding: RvItemProductBinding, product: Product) = binding.apply {
            cvChild.setOnLongClickListener {
                toggleHideableViews()
                true
            }

            tvEditItem.setOnClickListener {
                toggleHideableViews()
                callback.rvProductsOnEditItemClick(product)
            }

            tvEditQuantity.setOnClickListener {
                toggleHideableViews()
                callback.rvProductsOnEditQuantityClick(product)
            }

            tvEditPriceUnity.setOnClickListener {
                toggleHideableViews()
                callback.rvProductsOnEditPriceClick(product)
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

        private fun toggleHideableViews() {
            hideableViews.forEach { it.visibility = if (it.visibility == GONE) VISIBLE else GONE }
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
            return oldItem == newItem
        }
    }

    interface Callback {
        fun rvProductsOnDragAndDrop(toPosition: Int, product: Product)
        fun rvProductsOnEditPriceClick(product: Product)
        fun rvProductsOnEditQuantityClick(product: Product)
        fun rvProductsOnEditItemClick(product: Product)
        fun rvProductsOnBoughtItemClick(product: Product, isBought: Boolean)
    }

}
