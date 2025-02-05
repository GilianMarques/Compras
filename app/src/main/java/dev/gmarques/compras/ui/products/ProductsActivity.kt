package dev.gmarques.compras.ui.products

import android.animation.ValueAnimator
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.text.Spanned
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.animation.AnticipateInterpolator
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import dev.gmarques.compras.App
import dev.gmarques.compras.R
import dev.gmarques.compras.data.model.Category
import dev.gmarques.compras.data.model.Product
import dev.gmarques.compras.data.model.ShopList
import dev.gmarques.compras.databinding.ActivityProductsBinding
import dev.gmarques.compras.domain.utils.ExtFun.Companion.currencyToDouble
import dev.gmarques.compras.domain.utils.ExtFun.Companion.formatHtml
import dev.gmarques.compras.domain.utils.ExtFun.Companion.hideKeyboard
import dev.gmarques.compras.domain.utils.ExtFun.Companion.observeOnce
import dev.gmarques.compras.domain.utils.ExtFun.Companion.toCurrency
import dev.gmarques.compras.ui.Vibrator
import dev.gmarques.compras.ui.add_edit_product.AddEditProductActivity
import dev.gmarques.compras.ui.add_edit_shop_list.AddEditShopListActivity
import dev.gmarques.compras.ui.categories.CategoriesActivity
import dev.gmarques.compras.ui.suggest_products.SuggestProductsActivity
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.max
import kotlin.math.min

class ProductsActivity : AppCompatActivity(), ProductAdapter.Callback, CategoryAdapter.Callback {

    private lateinit var viewModel: ProductsActivityViewModel
    private lateinit var binding: ActivityProductsBinding
    private lateinit var rvAdapterProducts: ProductAdapter
    private lateinit var rvAdapterCategories: CategoryAdapter
    private var fabHidden: Boolean = false
    private var lastAdapterPosition = 0 // ajuda a escrolar o rv de categorias pra categoria selecionada

    companion object {
        private const val LIST_ID = "list_id"
        private const val SUGGEST_PRODUCTS = "suggest_products"

        fun newIntent(context: Context, list: String, suggestProducts: Boolean = false): Intent {
            return Intent(context, ProductsActivity::class.java).apply {
                putExtra(LIST_ID, list)
                putExtra(SUGGEST_PRODUCTS, suggestProducts)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val shopListId = intent.getStringExtra(LIST_ID)!!
        val suggestProducts = intent.getBooleanExtra(SUGGEST_PRODUCTS, false)

        binding = ActivityProductsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[ProductsActivityViewModel::class.java]
        viewModel.init(shopListId)

        initToolbar()
        initRecyclerViewProducts()
        initRecyclerViewCategories()
        initSearch()
        initFabAddProduct()
        observeProductsUpdates()
        observePrices()
        observeShopList()
        setupOnBackPressed()
        observeCategories()
        if (suggestProducts) startActivitySuggestProduct()


    }

    private fun observeCategories() {
        viewModel.listCategoriesLD.observe(this) { categories ->
            categories?.let { rvAdapterCategories.submitList(categories) }
        }
    }

    private fun observePrices() = viewModel.pricesLD.observe(this) {
        binding.apply {


            ValueAnimator.ofFloat(tvPriceList.text.toString().currencyToDouble().toFloat(), it.first.toFloat()).apply {
                interpolator = AnticipateInterpolator()
                duration = 500
                addUpdateListener {
                    lifecycleScope.launch {
                        withContext(Main) {
                            tvPriceList.text = it.animatedValue.toString().toDouble().toCurrency()
                        }
                    }
                }
            }.start()


            ValueAnimator.ofFloat(tvPriceCart.text.toString().currencyToDouble().toFloat(), it.second.toFloat()).apply {
                interpolator = AnticipateInterpolator()
                duration = 500
                addUpdateListener {
                    lifecycleScope.launch {
                        withContext(Main) {
                            tvPriceCart.text = it.animatedValue.toString().toDouble().toCurrency()
                        }
                    }
                }
            }.start()

        }
    }

    private fun observeShopList() = viewModel.shopListLD.observe(this) {
        binding.toolbar.tvActivityTitle.text = it?.name
    }

    private fun initSearch() {

        binding.edtSearch.doOnTextChanged { text, _, _, _ ->

            val term = text.toString()
            viewModel.searchProduct(term)
            binding.ivClearSearch.visibility = if (term.isEmpty()) GONE else VISIBLE
            binding.rvCategories.visibility = if (term.isEmpty()) VISIBLE else GONE

        }

        binding.ivClearSearch.setOnClickListener {
            binding.edtSearch.setText("")
            binding.edtSearch.hideKeyboard()
        }

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

    private fun initToolbar() {
        binding.toolbar.ivGoBack.setOnClickListener { this.onBackPressedDispatcher.onBackPressed() }

        binding.toolbar.ivMenu.setOnClickListener {
            showMenuDialog()
        }

    }

    private fun observeProductsUpdates() {
        viewModel.productsLD.observe(this) { newData ->
            rvAdapterProducts.submitList(newData)

            if (binding.edtSearch.text.toString().isNotEmpty()) {
                when (newData.isEmpty()) {
                    true -> Vibrator.error()
                    false -> Vibrator.success()
                }
            }
        }
    }

    private fun initRecyclerViewProducts() {
        rvAdapterProducts = ProductAdapter(isDarkThemeEnabled(), this@ProductsActivity)

        val dragDropHelper = ProductDragDropHelperCallback(rvAdapterProducts)
        val touchHelper = ItemTouchHelper(dragDropHelper)
        rvAdapterProducts.attachItemTouchHelper(touchHelper)

        touchHelper.attachToRecyclerView(binding.rvProducts)

        binding.rvProducts.layoutManager = LinearLayoutManager(this)
        binding.rvProducts.adapter = rvAdapterProducts
    }

    private fun initRecyclerViewCategories() {
        rvAdapterCategories = CategoryAdapter(this@ProductsActivity, this@ProductsActivity)
        binding.rvCategories.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvCategories.adapter = rvAdapterCategories
    }

    private fun initFabAddProduct() = binding.apply {

        fabAddProduct.setOnClickListener {
            startActivityAddProduct()
        }
        rvProducts.addOnScrollListener(object : OnScrollListener() {


            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0) {  // Rolando para cima - Esconde o FAB
                    if (fabHidden) return

                    fabAddProduct.animate().translationY(fabAddProduct.height.toFloat() * 2).alpha(0f).setStartDelay(100)
                        .setDuration(200L).start()
                    fabHidden = true

                } else if (dy < 0) { // Rolando para baixo - Mostra o FAB
                    if (!fabHidden) return

                    fabAddProduct.animate().translationY(0f).alpha(1f).setStartDelay(100).setDuration(200L).start()
                    fabHidden = false
                }
            }
        })
    }

    private fun startActivityAddProduct() {
        viewModel.shopListLD.observeOnce(this@ProductsActivity) { shopList ->

            Vibrator.interaction()
            val intent = AddEditProductActivity.newIntentAddProduct(this@ProductsActivity, shopList!!.id)
            startActivity(intent)
        }
    }

    private fun startActivitySuggestProduct() {
        Vibrator.interaction()
        val intent = SuggestProductsActivity.newIntent(this@ProductsActivity, viewModel.shopListLD.value!!.id)
        startActivity(intent)

    }

    private fun startActivityEditProduct(product: Product) {
        viewModel.shopListLD.observeOnce(this@ProductsActivity) { shopList ->

            Vibrator.interaction()
            val intent = AddEditProductActivity.newIntentEditProduct(this@ProductsActivity, shopList!!.id, product.id)
            startActivity(intent)
        }
    }

    private fun showMenuDialog() {
        viewModel.shopListLD.observeOnce(this) {
            Vibrator.interaction()

            BsdShopListMenu.Builder(this, it!!)
                .setRenameListener { showRenameDialog() }
                .setSortListener { showSortProductsDialog() }
                .setSuggestionListener { startActivitySuggestProduct() }
                .setRemoveListener { removeList -> confirmRemove(removeList) }
                .setManageCategoriesListener { startCategoriesActivity() }
                .build().show()
        }
    }

    private fun startCategoriesActivity() {

        Vibrator.interaction()
        val intent = CategoriesActivity.newIntent(this@ProductsActivity)
        startActivity(intent)
    }

    private fun showSortProductsDialog() {
        BsdSortProducts(this) {
            viewModel.loadSortPreferences()
            //força os dados a serem recarregados, filtrados e ordenados
            viewModel.searchProduct("a")
            viewModel.searchProduct("")
        }.show()
    }

    private fun showRenameDialog() {
      startActivity(
            AddEditShopListActivity.newIntentEditShopList(
                this,
                viewModel.shopListLD.value!!.id
            )
        )
    }

    private fun confirmRemove(shopList: ShopList) {
        val msg: Spanned = String.format(getString(R.string.Deseja_mesmo_remover_x), shopList.name).formatHtml()

        val dialogBuilder = AlertDialog.Builder(this).setTitle(getString(R.string.Por_favor_confirme)).setMessage(msg)
            .setPositiveButton(getString(R.string.Remover)) { dialog, _ ->
                lifecycleScope.launch {
                    viewModel.removeShopList(shopList)
                    dialog.dismiss()
                    finish()
                }
            }.setNegativeButton(getString(R.string.Cancelar)) { dialog, _ ->
                dialog.dismiss()
            }

        val dialog = dialogBuilder.create()
        dialog.show()
    }

    private fun confirmRemove(product: Product) {
        val msg: Spanned = String.format(getString(R.string.Deseja_mesmo_remover_x), product.name).formatHtml()

        val dialogBuilder = AlertDialog.Builder(this).setTitle(getString(R.string.Por_favor_confirme)).setMessage(msg)
            .setPositiveButton(getString(R.string.Remover)) { dialog, _ ->
                viewModel.removeProduct(product)
                dialog.dismiss()
            }.setNegativeButton(getString(R.string.Cancelar)) { dialog, _ ->
                dialog.dismiss()
            }

        val dialog = dialogBuilder.create()
        dialog.show()
    }

    private fun isDarkThemeEnabled(): Boolean {
        val nightModeFlags = App.getContext().resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES
    }

    override fun rvProductsOnDragAndDrop(toPosition: Int, product: Product) {
        viewModel.updateProductPosition(product, toPosition)
    }

    override fun rvProductsOnEditItemClick(product: Product) {

        BsdEditProductPriceOrQuantity
            .Builder()
            .setActivity(this@ProductsActivity)
            .setProduct(product)
            .setEditListener {
                startActivityEditProduct(it)

            }
            .setRemoveListener {
                confirmRemove(it)

            }
            .setConfirmListener {
                viewModel.updateProductAsIs(it)
            }
            .build().show()
    }

    override fun rvProductsOnBoughtItemClick(product: Product, isBought: Boolean) {
        viewModel.updateProductBoughtState(product, isBought)
    }

    override fun rvCategoriesOnSelect(category: Category, adapterPosition: Int) {
        Vibrator.interaction()
        viewModel.filterByCategory(category)

        (binding.rvCategories.layoutManager as LinearLayoutManager).smoothScrollToPosition(
            binding.rvCategories,
            null,
            if (lastAdapterPosition < adapterPosition) min(rvAdapterCategories.itemCount - 1, adapterPosition + 1)
            else max(0, adapterPosition - 1)
        )
        lastAdapterPosition = adapterPosition
    }
}