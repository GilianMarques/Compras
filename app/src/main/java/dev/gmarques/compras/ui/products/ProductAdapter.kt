package dev.gmarques.compras.ui.products

import android.annotation.SuppressLint
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import dev.gmarques.compras.App
import dev.gmarques.compras.R
import dev.gmarques.compras.data.model.Product
import dev.gmarques.compras.databinding.RvItemProductBinding
import dev.gmarques.compras.domain.model.ProductWithCategory
import dev.gmarques.compras.domain.utils.ExtFun.Companion.adjustSaturation
import dev.gmarques.compras.domain.utils.ExtFun.Companion.toCurrency
import dev.gmarques.compras.ui.Vibrator
import dev.gmarques.compras.ui.products.ProductAdapter.ProductViewHolder

class ProductAdapter(val callback: Callback) :
    ListAdapter<ProductWithCategory, ProductViewHolder>(ProductDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {

        val binding = DataBindingUtil.inflate<RvItemProductBinding>(
            LayoutInflater.from(parent.context), R.layout.rv_item_product, parent, false
        )

        return ProductViewHolder(binding, callback)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bindData(getItem(position))
    }

    class ProductViewHolder(
        private val binding: RvItemProductBinding,
        val callback: Callback,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bindData(productWithCategory: ProductWithCategory) {
            clearListener()

            val product = productWithCategory.product
            binding.apply {

                tvProductName.text = product.name
                tvProductInfo.text = product.info
                tvProductPrice.text = (product.price * product.quantity).toCurrency()
                tvProductQuantity.text = String.format(App.getContext().getString(R.string.un), product.quantity)
                cbBought.isChecked = product.hasBeenBought
                (ivHandle.drawable as GradientDrawable).setColor(productWithCategory.category.color.adjustSaturation(2f))

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
        fun rvProductsOnEditItemClick(product: Product)
        fun rvProductsOnBoughtItemClick(product: Product, isBought: Boolean)
    }

}
