package dev.gmarques.compras.presenter.categories

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View.GONE
import dev.gmarques.compras.presenter.MyActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dev.gmarques.compras.R
import dev.gmarques.compras.data.model.Category
import dev.gmarques.compras.databinding.ActivityCategoriesBinding

import dev.gmarques.compras.presenter.Vibrator
import dev.gmarques.compras.presenter.add_edit_category.AddEditCategoryActivity

class CategoriesActivity: MyActivity(), CategoryAdapter.Callback {

    private var fabHidden: Boolean = false
    private var selectionMode: Boolean = false
    private lateinit var binding: ActivityCategoriesBinding
    private lateinit var viewModel: ActivityCategoriesViewModel
    private lateinit var adapter: CategoryAdapter

    companion object {

        const val SELECTION_MODE = "selection_mode"
        const val SELECTED_CATEGORY = "selected_category"

        fun newIntent(context: Context, selectionMode: Boolean = false): Intent {
            return Intent(context, CategoriesActivity::class.java).apply {
                putExtra(SELECTION_MODE, selectionMode)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCategoriesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[ActivityCategoriesViewModel::class.java]
        selectionMode = intent.getBooleanExtra(SELECTION_MODE, false)
        setupToolbar()
        setupRecyclerview()
        observeCategories()
        setupFabAddCategory()
        observeViewmodelErrorMessages()
    }

    /**
     * Configura a toolbar da activity.
     */
    private fun setupToolbar() = binding.toolbar.apply {
        tvActivityTitle.text =
            if (selectionMode) getString(R.string.Selecionar_categoria) else getString(R.string.Gerenciar_categorias)
        ivGoBack.setOnClickListener { Vibrator.interaction();finish() }
        ivMenu.visibility = GONE
    }

    private fun setupFabAddCategory() = binding.apply {
        fabAdd.setOnClickListener {
            startActivityAddCategory()
        }



        rv.addOnScrollListener(object : OnScrollListener() {


            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0) {  // Rolando para cima - Esconde o FAB
                    if (fabHidden) return

                    fabAdd.animate().translationY(fabAdd.height.toFloat() * 2).alpha(0f).setStartDelay(100).setDuration(200L)
                        .start()
                    fabHidden = true

                } else if (dy < 0) { // Rolando para baixo - Mostra o FAB
                    if (!fabHidden) return

                    fabAdd.animate().translationY(0f).alpha(1f).setStartDelay(100).setDuration(200L).start()
                    fabHidden = false
                }
            }
        })
    }

    private fun observeCategories() {
        viewModel.categoriesLd.observe(this) {
            adapter.submitList(it)
        }
    }

    private fun setupRecyclerview() {

        adapter = CategoryAdapter(this)

        val dragDropHelper = CategoryDragDropHelperCallback(adapter)
        val touchHelper = ItemTouchHelper(dragDropHelper)
        adapter.attachItemTouchHelper(touchHelper)

        touchHelper.attachToRecyclerView(binding.rv)

        binding.rv.adapter = adapter
        binding.rv.layoutManager = LinearLayoutManager(this@CategoriesActivity)


    }

    private fun observeViewmodelErrorMessages() {
        viewModel.errorEventLD.observe(this@CategoriesActivity) { event ->
            Snackbar.make(binding.root, event, Snackbar.LENGTH_LONG).show()
            Vibrator.error()
        }
    }

    private fun startActivityAddCategory() {

        Vibrator.interaction()
        val intent = AddEditCategoryActivity.newIntentAddCategory(this@CategoriesActivity)
        startActivity(intent)
    }


    override fun rvCategoriesOnDragAndDrop(toPosition: Int, category: Category) {
        viewModel.updateCategoryPosition(category,toPosition)
    }

    override fun rvCategoriesOnEditItemClick(category: Category) {
        Vibrator.interaction()
        val intent = AddEditCategoryActivity.newIntentEditCategory(this@CategoriesActivity, category.id)
        startActivity(intent)
    }

    override fun rvCategoriesOnSelect(category: Category) {
        if (!selectionMode) return
        Vibrator.interaction()

        val resultIntent = Intent().apply { putExtra(SELECTED_CATEGORY, category) }
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

    override fun rvCategoriesOnRemove(category: Category) {
        val msg = String.format(getString(R.string.Deseja_mesmo_remover_x), category.name)

        val dialogBuilder = MaterialAlertDialogBuilder(this).setTitle(getString(R.string.Por_favor_confirme)).setMessage(msg)
            .setPositiveButton(getString(R.string.Remover)) { dialog, _ ->
                viewModel.removeCategory(category)
                dialog.dismiss()
            }.setNegativeButton(getString(R.string.Cancelar)) { dialog, _ ->
                dialog.dismiss()
            }

        val dialog = dialogBuilder.create()
        dialog.show()
    }


}
