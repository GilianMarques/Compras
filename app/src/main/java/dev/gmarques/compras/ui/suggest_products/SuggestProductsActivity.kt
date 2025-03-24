package dev.gmarques.compras.ui.suggest_products

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Spanned
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.activity.addCallback
import dev.gmarques.compras.ui.MyActivity
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dev.gmarques.compras.R
import dev.gmarques.compras.data.model.Product
import dev.gmarques.compras.databinding.ActivitySuggestProductsBinding
import dev.gmarques.compras.domain.model.SelectableProduct

import dev.gmarques.compras.domain.utils.ExtFun.Companion.hideKeyboard
import dev.gmarques.compras.ui.Vibrator
import kotlinx.coroutines.launch

class SuggestProductsActivity: MyActivity() {

    private lateinit var viewModel: SuggestProductsActivityViewModel
    private lateinit var binding: ActivitySuggestProductsBinding
    private lateinit var rvAdapter: SuggestionProductAdapter
    private var fabHidden: Boolean = false


    companion object {
        private const val LIST_ID = "list_id"


        fun newIntent(context: Context, list: String): Intent {
            return Intent(context, SuggestProductsActivity::class.java).apply {
                putExtra(LIST_ID, list)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val shopListId = intent.getStringExtra(LIST_ID)!!

        binding = ActivitySuggestProductsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[SuggestProductsActivityViewModel::class.java]
        viewModel.init(shopListId)

        initToolbar()
        initRecyclerView()
        initSearch()
        initFabIncludeProducts()
        observeProductsUpdates()
        observeViewmodelErrorMessages()
        setupOnBackPressed()
    }

    private fun setupOnBackPressed() {
        onBackPressedDispatcher.addCallback {
            if (binding.edtSearch.text.isNullOrEmpty()) {
                finish()
            } else {
                binding.edtSearch.setText("")
                binding.edtSearch.hideKeyboard()
            }
        }
    }

    private fun initSearch() {

        binding.edtSearch.doOnTextChanged { text, _, _, _ ->

            val term = text.toString()
            viewModel.searchProduct(term)
            binding.ivClearSearch.visibility = if (term.isEmpty()) GONE else VISIBLE
        }

        binding.ivClearSearch.setOnClickListener {
            binding.edtSearch.setText("")
            binding.edtSearch.hideKeyboard()
        }


    }

    private fun initToolbar() {

        binding.toolbar.ivGoBack.setOnClickListener { Vibrator.interaction(); this.onBackPressedDispatcher.onBackPressed() }
        binding.toolbar.tvActivityTitle.text = getString(R.string.Sugestao_de_produtos)
        binding.toolbar.ivMenu.visibility = GONE

    }

    private fun observeProductsUpdates() {
        viewModel.productsLD.observe(this) { newData ->
            rvAdapter.submitList(newData)

            if (binding.edtSearch.text.toString().isNotEmpty()) {
                when (newData.isEmpty()) {
                    true -> Vibrator.error()
                    false -> Vibrator.success()
                }
            }
        }
    }

    private fun observeViewmodelErrorMessages() = lifecycleScope.launch {
        viewModel.errorEventLD.observe(this@SuggestProductsActivity) { event ->
            Snackbar.make(binding.root, event, Snackbar.LENGTH_LONG).show()
            Vibrator.error()
        }
    }

    private fun initRecyclerView() {

        rvAdapter = SuggestionProductAdapter(
            ::adapterOnRemoveListener,
            ::adapterOnSelectionChangedListener
        )
        binding.rvProducts.layoutManager = LinearLayoutManager(this)
        binding.rvProducts.adapter = rvAdapter
    }

    private fun adapterOnRemoveListener(product: Product) {
        val msg =
            String.format(getString(R.string.Deseja_mesmo_remover_x_das_sugestoes), product.name)


        val dialogBuilder =
            MaterialAlertDialogBuilder(this).setTitle(getString(R.string.Por_favor_confirme))
                .setMessage(msg)
                .setPositiveButton(getString(R.string.Remover)) { dialog, _ ->
                    viewModel.removeSuggestionProduct(product)
                    dialog.dismiss()
                }.setNegativeButton(getString(R.string.Cancelar)) { dialog, _ ->
                    dialog.dismiss()
                }

        val dialog = dialogBuilder.create()
        dialog.show()
    }

    private fun adapterOnSelectionChangedListener(sp: SelectableProduct) {
        viewModel.updateSelectionData(sp)
    }

    private fun initFabIncludeProducts() = binding.apply {

        fabAddProduct.setOnClickListener {
            Vibrator.interaction()
            fabAddProduct.isEnabled = false
            viewModel.saveProducts()
            finish()
        }

        rvProducts.addOnScrollListener(object : OnScrollListener() {


            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0) {  // Rolando para cima - Esconde o FAB
                    if (fabHidden) return

                    fabAddProduct.animate().translationY(fabAddProduct.height.toFloat() * 2)
                        .alpha(0f).setStartDelay(100)
                        .setDuration(200L).start()
                    fabHidden = true

                } else if (dy < 0) { // Rolando para baixo - Mostra o FAB
                    if (!fabHidden) return

                    fabAddProduct.animate().translationY(0f).alpha(1f).setStartDelay(100)
                        .setDuration(200L).start()
                    fabHidden = false
                }
            }
        })
    }

}