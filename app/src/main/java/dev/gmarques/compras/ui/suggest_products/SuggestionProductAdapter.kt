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

class SuggestionProductAdapter(
    private val onRemoveListener: (Product) -> Unit,
    private val onSelectionChangedListener: (Product, Boolean, Int) -> Unit,
) :
    ListAdapter<SelectableProduct, SuggestionProductViewHolder>(ProductDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SuggestionProductViewHolder {

        val binding = DataBindingUtil.inflate<RvItemSuggestionProductBinding>(
            LayoutInflater.from(parent.context), R.layout.rv_item_suggestion_product, parent, false
        )

        return SuggestionProductViewHolder(binding, onRemoveListener, onSelectionChangedListener)
    }

    override fun onBindViewHolder(holder: SuggestionProductViewHolder, position: Int) {
        holder.bindData(getItem(position))
    }

    class SuggestionProductViewHolder(
        private val binding: RvItemSuggestionProductBinding,
        private val onRemoveListener: (Product) -> Unit,
        private val onSelectionDataChangedListener: (Product, Boolean, Int) -> Unit,
    ) :
        RecyclerView.ViewHolder(binding.root) {

        private lateinit var selectableProduct: SelectableProduct

        fun bindData(selectableProduct: SelectableProduct) {
            this.selectableProduct = selectableProduct

            clearListener()
            animate()

            val product = selectableProduct.product
            binding.apply {

                cbSelected.isChecked = selectableProduct.isSelected
                tvProductName.text = product.name
                quantitySelector.tvQuantity.text =
                    String.format(App.getContext().getString(R.string.un), selectableProduct.quantity)

            }
            updateItemView(selectableProduct.isSelected)
            setListeners(binding)
        }

        private fun clearListener() = binding.apply {
            cbSelected.setOnCheckedChangeListener(null)
        }

        @SuppressLint("ClickableViewAccessibility")
        private fun setListeners(binding: RvItemSuggestionProductBinding) = binding.apply {

            /* Permite que o usuario altere a quantidade de produto rapidamente sem precisar esperar que cada alteração seja
            salva no db e recarregada, ainda permite que a alteraçao feita seja refletida imediatamente na ui já que as atualizações
            do db tem throttling, o que tambem garante que de todas as  alteraçõe feitas rapidamente só a mais recente será refletida  */
            var mutableQuantity = selectableProduct.quantity

            cbSelected.setOnCheckedChangeListener { _, checked ->
                onSelectionDataChangedListener(selectableProduct.product, checked, selectableProduct.quantity)
                updateItemView(checked)
            }

            ivRemove.setOnClickListener {
                onRemoveListener(selectableProduct.product)
                Vibrator.interaction()
            }

            quantitySelector.apply {
                val reflectAction = {
                    tvQuantity.text = String.format(App.getContext().getString(R.string.un), mutableQuantity)
                    tvProductPrice.text = (selectableProduct.product.price * mutableQuantity).toCurrency()
                    Vibrator.interaction()
                }

                tvPlus.setOnClickListener {
                    if (mutableQuantity <= Product.Validator.MAX_QUANTITY) {
                        reflectAction()
                        onSelectionDataChangedListener(
                            selectableProduct.product,
                            selectableProduct.isSelected,
                            mutableQuantity++
                        )
                    }
                }

                tvMinus.setOnClickListener {
                    if (mutableQuantity >= Product.Validator.MIN_QUANTITY) {
                        reflectAction()
                        onSelectionDataChangedListener(
                            selectableProduct.product,
                            selectableProduct.isSelected,
                            mutableQuantity--
                        )
                    }
                }
            }

        }


        private fun updateItemView(checked: Boolean) = binding.apply {
            if (checked) {
                tvProductPrice.text = (selectableProduct.product.price * selectableProduct.quantity).toCurrency()

                quantitySelector.root.visibility = VISIBLE
                ivRemove.visibility = GONE

            } else {
                tvProductPrice.text = (0.0).toCurrency()
                quantitySelector.root.visibility = GONE
                ivRemove.visibility = VISIBLE

            }
        }

        private fun animate() {
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
                && oldItem.isSelected == newItem.isSelected
                && oldItem.quantity == newItem.quantity
    }
}



