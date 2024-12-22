package dev.gmarques.compras.ui.products

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import dev.gmarques.compras.R
import dev.gmarques.compras.data.data.model.Product
import dev.gmarques.compras.databinding.RvItemProductBinding
import dev.gmarques.compras.utils.App
import dev.gmarques.compras.utils.ExtFun.Companion.toCurrency

class ProductAdapter(val callback: Callback) : ListAdapter<Product, ProductAdapter.ListViewHolder>(ProductDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {

        val binding = DataBindingUtil.inflate<RvItemProductBinding>(
            LayoutInflater.from(parent.context), R.layout.rv_item_product, parent, false
        )

        return ListViewHolder(binding, callback)
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        holder.bindData(getItem(position))
    }

    class ListViewHolder(
        private val binding: RvItemProductBinding,
        val callback: Callback,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bindData(product: Product) {
            clearListener()
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
                callback.rvProductsOnBoughtItemClick(product, checked)
            }


        }


        private fun animarView() {
            itemView.alpha = 0f
            itemView.animate().alpha(1f).setDuration(150).setStartDelay(3L * adapterPosition).start()
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
        fun rvProductsOnEditItemClick(product: Product)
        fun rvProductsOnBoughtItemClick(product: Product, isBought: Boolean)
    }

}
