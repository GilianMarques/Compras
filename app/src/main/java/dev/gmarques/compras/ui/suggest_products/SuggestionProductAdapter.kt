package dev.gmarques.compras.ui.suggest_products

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import dev.gmarques.compras.App
import dev.gmarques.compras.R
import dev.gmarques.compras.data.model.Product
import dev.gmarques.compras.databinding.RvItemSuggestionProductBinding
import dev.gmarques.compras.domain.model.SelectableProduct
import dev.gmarques.compras.domain.utils.ExtFun.Companion.toCurrency
import dev.gmarques.compras.ui.Vibrator
import dev.gmarques.compras.ui.suggest_products.SuggestionProductAdapter.SuggestionProductViewHolder

class SuggestionProductAdapter(private val onRemoveListener: (Product,Int) -> Unit) :
    ListAdapter<SelectableProduct, SuggestionProductViewHolder>(ProductDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SuggestionProductViewHolder {

        val binding = DataBindingUtil.inflate<RvItemSuggestionProductBinding>(
            LayoutInflater.from(parent.context), R.layout.rv_item_suggestion_product, parent, false
        )

        return SuggestionProductViewHolder(binding, onRemoveListener)
    }

    override fun onBindViewHolder(holder: SuggestionProductViewHolder, position: Int) {
        holder.bindData(getItem(position))
    }

    fun removeProduct(position: Int) {
        val newList = currentList.toMutableList()
        newList.removeAt(position)
        submitList(newList)

    }

    class SuggestionProductViewHolder(
        private val binding: RvItemSuggestionProductBinding,
        private val onRemoveListener: (Product,Int) -> Unit,
    ) :
        RecyclerView.ViewHolder(binding.root) {

        private lateinit var selectableProduct: SelectableProduct

        fun bindData(selectableProduct: SelectableProduct) {
            this.selectableProduct = selectableProduct

            clearListener()
            animarView()

            val product = selectableProduct.product
            binding.apply {

                tvProductName.text = product.name
                tvProductPrice.text = (product.price * product.quantity).toCurrency()
                quantitySelector.tvQuantity.text =
                    String.format(App.getContext().getString(R.string.un), selectableProduct.quantity)

            }

            setListeners(binding)
        }

        private fun clearListener() = binding.apply {
            cbSelected.setOnCheckedChangeListener(null)
        }

        @SuppressLint("ClickableViewAccessibility")
        private fun setListeners(binding: RvItemSuggestionProductBinding) = binding.apply {

            cbSelected.setOnCheckedChangeListener { _, checked ->
                selectableProduct.isSelected = checked
                quantitySelector.root.visibility = if (checked) VISIBLE else GONE
                ivRemove.visibility = if (checked) GONE else VISIBLE
            }

            ivRemove.setOnClickListener {
                onRemoveListener(selectableProduct.product, adapterPosition)
                Vibrator.interaction()
            }

            quantitySelector.apply {
                val reflectAction = {
                    tvQuantity.text = String.format(App.getContext().getString(R.string.un), selectableProduct.quantity)
                    tvProductPrice.text = (selectableProduct.product.price * selectableProduct.quantity).toCurrency()
                    Vibrator.interaction()
                }

                tvPlus.setOnClickListener {
                    if (selectableProduct.quantity <= Product.Validator.MAX_QUANTITY) selectableProduct.quantity++
                    reflectAction()
                }
                tvMinus.setOnClickListener {
                    if (selectableProduct.quantity > Product.Validator.MIN_QUANTITY) selectableProduct.quantity--
                    reflectAction()
                }


            }

        }


        private fun animarView() {
            itemView.alpha = 0f
            itemView.animate().alpha(1f).setDuration(150).setStartDelay(3L * adapterPosition).start()
        }
    }
}


/**
 * Compara as listas de dados pra atualizar o recyclerview
 * */
class ProductDiffCallback : DiffUtil.ItemCallback<SelectableProduct>() {

    override fun areItemsTheSame(oldItem: SelectableProduct, newItem: SelectableProduct): Boolean {
        return oldItem.product.id == newItem.product.id
    }

    override fun areContentsTheSame(oldItem: SelectableProduct, newItem: SelectableProduct): Boolean {
        // deve comparar toda a informação que é exibida na view pro usuario
        return oldItem.product == newItem.product
    }
}



