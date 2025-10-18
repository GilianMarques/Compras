package dev.gmarques.compras.presenter.categories

import android.annotation.SuppressLint
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import dev.gmarques.compras.data.model.Category
import dev.gmarques.compras.databinding.RvItemCategoryBinding
import java.util.Collections

/**
 * Autor: Gilian
 * Data de Criação: 19/01/2025
 */

class CategoryAdapter(val callback: Callback) :
    ListAdapter<Category, CategoryAdapter.CategoryViewHolder>(CategoryDiffCallback()) {
    private lateinit var itemTouchHelper: ItemTouchHelper
    private var utilList = mutableListOf<Category>()

    inner class CategoryViewHolder(private val binding: RvItemCategoryBinding) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("ClickableViewAccessibility")
        fun bind(category: Category) {
            with(binding) {
                tvCategoryName.text = category.name
                (ivListIcon.background as GradientDrawable).also { it.mutate(); it.setColor(category.color) }

                ivRemove.setOnClickListener {
                    callback.rvCategoriesOnRemove(category)
                }

                ivEdit.setOnClickListener {
                    callback.rvCategoriesOnEditItemClick(category)
                }

                cvChild.setOnClickListener {
                    callback.rvCategoriesOnSelect(category)
                }

                ivHandle.setOnTouchListener { it, motionEvent ->

                    when (motionEvent.action) {
                        MotionEvent.ACTION_DOWN -> {
                            // Inicia o drag and drop
                            itemTouchHelper.startDrag(this@CategoryViewHolder)
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = RvItemCategoryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun getItemCount(): Int = currentList.size

    override fun submitList(list: List<Category>?) {
        utilList.clear()
        utilList.addAll(list ?: emptyList())
        super.submitList(list)
    }

    fun attachItemTouchHelper(touchHelper: ItemTouchHelper) {
        this.itemTouchHelper = touchHelper
    }

    fun moveCategory(fromPosition: Int, toPosition: Int) {
        Collections.swap(utilList, fromPosition, toPosition)
        submitList(utilList.toList())
    }

    fun updateDraggedCategories() {
        for (i in utilList.indices) {
            val category = utilList[i]
            if (category.position != i) callback.rvCategoriesOnDragAndDrop(i, category)
        }
    }

    class CategoryDiffCallback : DiffUtil.ItemCallback<Category>() {

        override fun areItemsTheSame(oldItem: Category, newItem: Category): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Category, newItem: Category): Boolean {
            // deve comparar toda a informação que é exibida na view pro usuario
            return oldItem == newItem
        }
    }

    interface Callback {
        fun rvCategoriesOnDragAndDrop(toPosition: Int, category: Category)
        fun rvCategoriesOnEditItemClick(category: Category)
        fun rvCategoriesOnSelect(category: Category)
        fun rvCategoriesOnRemove(category: Category)
    }
}
