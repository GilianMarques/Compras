package dev.gmarques.compras.ui.products

import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import dev.gmarques.compras.R
import dev.gmarques.compras.data.model.Category
import dev.gmarques.compras.databinding.RvItemCategorySmallBinding
import dev.gmarques.compras.domain.model.CategoryWithProductsStats
import dev.gmarques.compras.domain.utils.ExtFun.Companion.dp

class CategoryAdapter(activity: AppCompatActivity, val callback: Callback) :
    ListAdapter<CategoryWithProductsStats, CategoryAdapter.CategoryWithProductStatsViewHolder>(
        CategoryDiffCallback()
    ) {


    private var allBoughtColor = -1
    private var defaultColor = -1
    private var selectionColor = -1

    init {

        allBoughtColor = ResourcesCompat.getColor(activity.resources, R.color.all_bought_color, activity.theme)
        defaultColor = ResourcesCompat.getColor(activity.resources, R.color.rv_product_item_background, activity.theme)

        val typedValueSelectionColor = TypedValue()
        activity.theme.resolveAttribute(androidx.appcompat.R.attr.colorPrimary, typedValueSelectionColor, true)
            .also { selectionColor = typedValueSelectionColor.data }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryWithProductStatsViewHolder {

        val binding = RvItemCategorySmallBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return CategoryWithProductStatsViewHolder(binding, callback)
    }

    override fun onBindViewHolder(holder: CategoryWithProductStatsViewHolder, position: Int) {
        holder.bindData(getItem(position), selectionColor, allBoughtColor, defaultColor)
    }


    class CategoryWithProductStatsViewHolder(
        private val binding: RvItemCategorySmallBinding,
        private val callback: Callback,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bindData(categoryWithStats: CategoryWithProductsStats, selectionColor: Int, allBoughtColor: Int, defaultColor: Int) {

            val category = categoryWithStats.category
            val strokeWidth = (1.dp() * 1.5).toInt()

            binding.apply {
                tvCategoryName.text = category.name
                (binding.ivListIcon.background.mutate() as GradientDrawable).setColor(category.color)

                if (categoryWithStats.totalProducts == categoryWithStats.boughtProducts) {
                    ivAllBought.visibility = VISIBLE
                } else {
                    ivAllBought.visibility = GONE
                }

                (binding.cvChild.background.mutate() as GradientDrawable).setStroke(
                    strokeWidth,
                    if (categoryWithStats.selected) selectionColor
                    else defaultColor
                )

                binding.cvChild.elevation = if (categoryWithStats.selected) 5.dp().toFloat() else 0f


                cvChild.setOnClickListener {
                    callback.rvCategoriesOnSelect(category, adapterPosition)
                }

            }
        }
    }

    /**
     * Compara as listas de dados pra atualizar o recyclerview
     * */
    class CategoryDiffCallback : DiffUtil.ItemCallback<CategoryWithProductsStats>() {

        override fun areItemsTheSame(oldItem: CategoryWithProductsStats, newItem: CategoryWithProductsStats): Boolean {
            return oldItem.category.id == newItem.category.id
        }

        override fun areContentsTheSame(oldItem: CategoryWithProductsStats, newItem: CategoryWithProductsStats): Boolean {
            return oldItem.category == newItem.category &&
                    oldItem.boughtProducts == newItem.boughtProducts &&
                    oldItem.totalProducts == newItem.totalProducts &&
                    oldItem.selected == newItem.selected
        }
    }

    interface Callback {
        fun rvCategoriesOnSelect(category: Category, adapterPosition: Int)
    }
}
