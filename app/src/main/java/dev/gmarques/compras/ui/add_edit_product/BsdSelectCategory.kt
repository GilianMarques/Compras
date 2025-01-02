package dev.gmarques.compras.ui.add_edit_product

import android.annotation.SuppressLint
import android.app.Activity
import android.content.DialogInterface
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import dev.gmarques.compras.R
import dev.gmarques.compras.data.model.Category
import dev.gmarques.compras.data.repository.CategoryRepository
import dev.gmarques.compras.databinding.BsdSelectCategoryDialogBinding
import dev.gmarques.compras.databinding.RvItemCategoryBinding
import dev.gmarques.compras.domain.utils.ListenerRegister
import dev.gmarques.compras.ui.Vibrator

class BsdSelectCategory private constructor(
    private val targetActivity: Activity,
    private val onConfirmListener: (Category) -> Unit,
    private val onEditListener: ((Category) -> Unit),
    private val onRemoveListener: ((Category) -> Unit),
    private val onAddListener: () -> Unit,
    private val onDismissListener: () -> Unit,

    ) : DialogInterface.OnDismissListener {

    private lateinit var adapter: CategoryAdapter
    private lateinit var listenerRegister: ListenerRegister
    private var binding = BsdSelectCategoryDialogBinding.inflate(targetActivity.layoutInflater)
    private val dialog: BottomSheetDialog = BottomSheetDialog(targetActivity)

    init {
        dialog.setContentView(binding.root)
        setupRecyclerview()
        loadCategories()
        setupFabAddCategory()
    }

    private fun setupFabAddCategory() {
        binding.fabAdd.setOnClickListener {
            onAddListener()
        }
    }

    private fun loadCategories() {
        listenerRegister = CategoryRepository.observeCategoryUpdates { categories, error ->
            if (error == null) adapter.submitList(categories!!)
            else {
                Vibrator.error()
                Snackbar.make(
                    binding.root,
                    targetActivity.getString(R.string.Erro_ao_carregar_categorias_x, error.message), Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun setupRecyclerview() {

        adapter = CategoryAdapter(
            onCategorySelected = ::onCategorySelected,
            onEditListener = ::onEdit,
            onRemoveListener = ::onRemove
        )

        binding.rv.adapter = adapter
        binding.rv.layoutManager = LinearLayoutManager(targetActivity)
    }

    private fun onCategorySelected(category: Category) {
        dialog.dismiss()
        onConfirmListener(category)
    }

    private fun onEdit(category: Category) {
        onEditListener(category)
    }

    private fun onRemove(category: Category) {
        onRemoveListener(category)
    }


    fun show() {
        dialog.setOnDismissListener(this)
        dialog.show()

        val behavior = BottomSheetBehavior.from(binding.root.parent as View)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    class CategoryAdapter(
        private val onCategorySelected: (Category) -> Unit,
        private val onEditListener: ((Category) -> Unit),
        private val onRemoveListener: ((Category) -> Unit),
    ) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {
        private var categories: List<Category> = emptyList()

        inner class CategoryViewHolder(private val binding: RvItemCategoryBinding) : RecyclerView.ViewHolder(binding.root) {
            fun bind(category: Category) {
                with(binding) {
                    tvCategoryName.text = category.name
                    (ivListIcon.background as GradientDrawable).also { it.mutate(); it.setColor(category.color) }

                    ivRemove.setOnClickListener {
                        onRemoveListener.invoke(category)
                    }

                    ivEdit.setOnClickListener {
                        onEditListener.invoke(category)
                    }

                    cvChild.setOnClickListener {
                        onCategorySelected(category)
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
            holder.bind(categories[position])
        }

        override fun getItemCount(): Int = categories.size

        @SuppressLint("NotifyDataSetChanged")
        fun submitList(categories: List<Category>) {
            this.categories = categories
            notifyDataSetChanged()
        }
    }

    class Builder(private val activity: Activity) {
        private lateinit var onConfirmListener: (Category) -> Unit
        private lateinit var onEditListener: (Category) -> Unit
        private lateinit var onRemoveListener: (Category) -> Unit
        private lateinit var onAddListener: () -> Unit
        private lateinit var onDismissListener: () -> Unit

        fun setOnConfirmListener(listener: (Category) -> Unit): Builder {
            this.onConfirmListener = listener
            return this
        }

        fun setOnAddListener(listener: () -> Unit): Builder {
            this.onAddListener = listener
            return this
        }

        fun setOnEditListener(listener: (Category) -> Unit): Builder {
            this.onEditListener = listener
            return this
        }

        fun setOnRemoveListener(listener: (Category) -> Unit): Builder {
            this.onRemoveListener = listener
            return this
        }

        fun build(): BsdSelectCategory {

            return BsdSelectCategory(
                targetActivity = activity,
                onConfirmListener = onConfirmListener,
                onEditListener = onEditListener,
                onRemoveListener = onRemoveListener,
                onAddListener = onAddListener,
                onDismissListener = onDismissListener
            )
        }

        fun setOnDismissListener(listener: () -> Unit): Builder {
            this.onDismissListener = listener
            return this
        }
    }

    override fun onDismiss(p0: DialogInterface?) {
        listenerRegister.remove()
        onDismissListener()
    }

    fun dismissBottomSheetDialog() {
        dialog.dismiss()
    }
}
