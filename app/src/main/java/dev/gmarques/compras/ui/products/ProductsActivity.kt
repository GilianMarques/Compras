package dev.gmarques.compras.ui.products

import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.animation.AnticipateInterpolator
import androidx.activity.addCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import dev.gmarques.compras.ui.MyActivity
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.BaseTransientBottomBar.ANIMATION_MODE_SLIDE
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_INDEFINITE
import com.google.android.material.snackbar.Snackbar
import dev.gmarques.compras.R
import dev.gmarques.compras.data.PreferencesHelper
import dev.gmarques.compras.data.PreferencesHelper.PrefsKeys
import dev.gmarques.compras.data.model.Category
import dev.gmarques.compras.data.model.Establishment
import dev.gmarques.compras.data.model.Product
import dev.gmarques.compras.data.model.ShopList
import dev.gmarques.compras.data.repository.ProductRepository
import dev.gmarques.compras.data.repository.SuggestionProductRepository
import dev.gmarques.compras.databinding.ActivityProductsBinding
import dev.gmarques.compras.domain.utils.ExtFun.Companion.currencyToDouble

import dev.gmarques.compras.domain.utils.ExtFun.Companion.hideKeyboard
import dev.gmarques.compras.domain.utils.ExtFun.Companion.toCurrency
import dev.gmarques.compras.ui.Vibrator
import dev.gmarques.compras.ui.add_edit_product.AddEditProductActivity
import dev.gmarques.compras.ui.add_edit_shop_list.AddEditShopListActivity
import dev.gmarques.compras.ui.categories.CategoriesActivity
import dev.gmarques.compras.ui.stablishments.EstablishmentsActivity
import dev.gmarques.compras.ui.stablishments.EstablishmentsActivity.Companion.SELECTED_MARKET
import dev.gmarques.compras.ui.suggest_products.SuggestProductsActivity
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.max
import kotlin.math.min

class ProductsActivity: MyActivity(), ProductAdapter.Callback, CategoryAdapter.Callback {

    private var uiState: ProductsActivityViewModel.UiState? = null
    private lateinit var establishmentResultLauncher: ActivityResultLauncher<Intent>

    private lateinit var viewModel: ProductsActivityViewModel
    private lateinit var binding: ActivityProductsBinding
    private lateinit var rvAdapterProducts: ProductAdapter
    private lateinit var rvAdapterCategories: CategoryAdapter
    private var fabHidden: Boolean = false

    // ajuda a escrolar o rv de categorias pra categoria selecionada
    private var lastAdapterPosition = 0

    private var searchJob = Job()

    companion object {
        private const val LIST_ID = "list_id"

        fun newIntent(context: Context, list: String): Intent {
            return Intent(context, ProductsActivity::class.java).apply {
                putExtra(LIST_ID, list)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val shopListId = intent.getStringExtra(LIST_ID)!!

        binding = ActivityProductsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[ProductsActivityViewModel::class.java]
        viewModel.init(shopListId)

        setupToolbar()
        setupRecyclerViewProducts()
        setupRecyclerViewCategories()
        setupSearch()
        setupFabAddProduct()
        observeStateChanges()
        setupOnBackPressed()
        suggestProductsIfNeeded(shopListId)
        observeEstablishmentChangeEvents()
        setupActivityResultLauncher()
    }

    private fun observeEstablishmentChangeEvents() {
        viewModel.establishmentEvent.observe(this@ProductsActivity) { establishment ->
            if (establishment != null) {
                binding.toolbar.tvActivitySubtitle.text = getString(
                    R.string.Comprando_em, establishment.name
                )
            }
        }
    }

    private fun confirmEstablishment() = with(viewModel) {

        val title =
            if (currentEstablishment == null) getString(R.string.Onde_est_fazendo_as_compras) else getString(
                R.string.Voce_esta_comprando_em, currentEstablishment!!.name
            )

        val action = if (currentEstablishment == null) getString(R.string.Definir_estabelecimento)
        else getString(R.string.Alterar)

        Vibrator.interaction()
        Snackbar.make(binding.root, title, Snackbar.LENGTH_LONG).setAction(action) {

            Vibrator.interaction()
            establishmentResultLauncher.launch(
                EstablishmentsActivity.newIntent(this@ProductsActivity, true)
            )

        }.setAnimationMode(ANIMATION_MODE_SLIDE)
            .setDuration(if (currentEstablishment == null) LENGTH_INDEFINITE else 4000).show()

        viewModel.establishmentConfirmed = true

    }

    private fun suggestProductsIfNeeded(shopListId: String) = lifecycleScope.launch(IO) {

        if (ProductRepository.getProducts(shopListId)
                .isEmpty() && SuggestionProductRepository.getSuggestions().isNotEmpty()
        ) startActivitySuggestProduct()
    }

    private fun observeStateChanges() {
        viewModel.uiStateLD.observe(this) { newState ->

            binding.toolbar.tvActivityTitle.text = newState.shopList.name

            with(newState.listCategories) {
                rvAdapterCategories.submitList(this)
            }

            with(newState.products) {

                rvAdapterProducts.submitList(this)

                if (binding.edtSearch.text.toString().isNotEmpty()) {
                    when (this.isEmpty()) {
                        true -> Vibrator.error()
                        false -> Vibrator.success()
                    }
                }
            }

            animatePrices(newState)

            this.uiState = newState
        }
    }

    private fun animatePrices(state: ProductsActivityViewModel.UiState) {

        binding.apply {

            ValueAnimator.ofFloat(
                tvPriceList.text.toString().currencyToDouble().toFloat(), state.priceFull.toFloat()
            ).apply {
                interpolator = AnticipateInterpolator()
                duration = 500
                addUpdateListener {
                    lifecycleScope.launch {
                        withContext(Main) {
                            tvPriceList.text =
                                it.animatedValue.toString().toDouble().toCurrency()
                        }
                    }
                }
            }.start()

            ValueAnimator.ofFloat(
                tvPriceCart.text.toString().currencyToDouble().toFloat(),
                state.priceBought.toFloat()
            ).apply {
                interpolator = AnticipateInterpolator()
                duration = 500
                addUpdateListener {
                    lifecycleScope.launch {
                        withContext(Main) {
                            tvPriceCart.text =
                                it.animatedValue.toString().toDouble().toCurrency()
                        }
                    }
                }
            }.start()

        }

    }

    private fun setupSearch() {

        binding.edtSearch.doOnTextChanged { text, _, _, _ ->
            searchJob.cancel()
            searchJob = Job()
            lifecycleScope.launch(searchJob) {
                delay(300)
                val term = text.toString()
                viewModel.searchProduct(term)
                binding.ivClearSearch.visibility = if (term.isEmpty()) GONE else VISIBLE
                delay(500)
                binding.rvCategories.visibility = if (term.isEmpty()) VISIBLE else GONE
            }
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

    private fun setupToolbar() {
        binding.toolbar.ivGoBack.setOnClickListener { Vibrator.interaction(); this.onBackPressedDispatcher.onBackPressed() }

        binding.toolbar.ivMenu.setOnClickListener {
            showMenuDialog()
        }

    }

    private fun setupRecyclerViewProducts() {
        rvAdapterProducts = ProductAdapter(this@ProductsActivity)

        val dragDropHelper = ProductDragDropHelperCallback(rvAdapterProducts)
        val touchHelper = ItemTouchHelper(dragDropHelper)
        rvAdapterProducts.attachItemTouchHelper(touchHelper)

        touchHelper.attachToRecyclerView(binding.rvProducts)

        binding.rvProducts.layoutManager = LinearLayoutManager(this)
        binding.rvProducts.adapter = rvAdapterProducts
    }

    private fun setupRecyclerViewCategories() {
        rvAdapterCategories = CategoryAdapter(this@ProductsActivity, this@ProductsActivity)
        binding.rvCategories.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvCategories.adapter = rvAdapterCategories
    }

    private fun setupFabAddProduct() = binding.apply {

        fabAddProduct.setOnClickListener {
            startActivityAddProduct()
        }

        rvProducts.addOnScrollListener(object : OnScrollListener() {


            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0) {  // Rolando para cima - Esconde o FAB
                    if (fabHidden) return

                    fabAddProduct.animate().translationY(fabAddProduct.height.toFloat() * 2)
                        .alpha(0f).setStartDelay(100).setDuration(200L).start()
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

    private fun startActivityAddProduct() {

        Vibrator.interaction()
        val intent = AddEditProductActivity.newIntentAddProduct(
            this@ProductsActivity,
            uiState!!.shopList.id,
            viewModel.filterCategory,
            viewModel.searchTerm
        )
        startActivity(intent)

        if (viewModel.searchTerm.isNotEmpty()) with(binding.edtSearch) {
            postDelayed({ setText("") }, 1000)
        }

    }

    private fun startActivitySuggestProduct() {
        Vibrator.interaction()
        val intent = SuggestProductsActivity.newIntent(this@ProductsActivity, uiState!!.shopList.id)
        startActivity(intent)

    }

    private fun startActivityEditProduct(product: Product) {

        Vibrator.interaction()
        val intent = AddEditProductActivity.newIntentEditProduct(
            this@ProductsActivity, uiState!!.shopList.id, product.id
        )
        startActivity(intent)
    }

    private fun showMenuDialog() {
        Vibrator.interaction()

        BsdShopListMenu.Builder(this, uiState!!.shopList).setRenameListener { editList() }
            .setSortListener { showSortProductsDialog() }
            .setSuggestionListener { startActivitySuggestProduct() }
            .setRemoveListener { removeList -> confirmRemove(removeList) }
            .setManageCategoriesListener { startCategoriesActivity() }
            .setManageEstablishmentsListener { startEstablishmentsActivity() }.build().show()
    }

    private fun startCategoriesActivity() {

        Vibrator.interaction()
        val intent = CategoriesActivity.newIntent(this@ProductsActivity)
        startActivity(intent)
    }

    private fun startEstablishmentsActivity() {

        Vibrator.interaction()
        val intent = EstablishmentsActivity.newIntent(this@ProductsActivity)
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

    private fun editList() {
        startActivity(
            AddEditShopListActivity.newIntentEditShopList(
                this, uiState!!.shopList.id
            )
        )
    }

    private fun confirmRemove(shopList: ShopList) {
        val msg = String.format(getString(R.string.Deseja_mesmo_remover_x), shopList.name)

        val dialogBuilder =
            MaterialAlertDialogBuilder(this).setTitle(getString(R.string.Por_favor_confirme))
                .setMessage(msg).setPositiveButton(getString(R.string.Remover)) { dialog, _ ->
                    tryToRemoveShopList(shopList)
                    dialog.dismiss()
                }.setNegativeButton(getString(R.string.Cancelar)) { dialog, _ ->
                    dialog.dismiss()
                }

        val dialog = dialogBuilder.create()
        dialog.show()
    }

    private fun confirmRemove(product: Product) {
        val msg = String.format(getString(R.string.Deseja_mesmo_remover_x), product.name)

        val dialogBuilder =
            MaterialAlertDialogBuilder(this).setTitle(getString(R.string.Por_favor_confirme))
                .setMessage(msg).setPositiveButton(getString(R.string.Remover)) { dialog, _ ->
                    viewModel.removeProduct(product)
                    dialog.dismiss()
                }.setNegativeButton(getString(R.string.Cancelar)) { dialog, _ ->
                    dialog.dismiss()
                }

        val dialog = dialogBuilder.create()
        dialog.show()
    }

    private fun tryToRemoveShopList(shopList: ShopList) {
        lifecycleScope.launch {

            if (viewModel.tryToRemoveShopList(shopList)) {
                Vibrator.success()
                finish()
            } else {
                Vibrator.error()
                Snackbar.make(
                    binding.root,
                    getString(R.string.Erro_removendo_lista_de_compras_tente_novamente),
                    Snackbar.LENGTH_LONG
                ).show()
            }

        }
    }


    private fun setupActivityResultLauncher() {

        establishmentResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val selectedEstablishment =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            result.data!!.getSerializableExtra(SELECTED_MARKET, Establishment::class.java)
                        } else @Suppress("DEPRECATION") result.data!!.getSerializableExtra(
                            SELECTED_MARKET
                        ) as Establishment

                    PreferencesHelper().saveValue(PrefsKeys.LAST_MARKET_USED, selectedEstablishment!!.id)
                    lifecycleScope.launch { viewModel.loadCurrentEstablishment() }
                }
            }
    }

    override fun rvProductsOnDragAndDrop(toPosition: Int, product: Product) {
        viewModel.updateProductPosition(product, toPosition)
    }

    override fun rvProductsOnEditItemClick(product: Product) {
        showEditProductDialog(product)
    }

    override fun rvProductsOnPriceClick(product: Product) {
        showEditProductDialog(product, BsdEditProduct.Focus.PRICE)
    }

    override fun rvProductsOnQuantityClick(product: Product) {
        showEditProductDialog(product, BsdEditProduct.Focus.QUANTITY)
    }

    override fun rvProductsOnInfoClick(product: Product) {
        showEditProductDialog(product, BsdEditProduct.Focus.INFO)
    }

    private fun showEditProductDialog(product: Product, focus: BsdEditProduct.Focus? = null) {
        BsdEditProduct.Builder().setCurrentEstablishment(viewModel.currentEstablishment)
            .setActivity(this@ProductsActivity).setProduct(product).setFocus(focus)
            .setEditListener {
                startActivityEditProduct(it)
            }
            .setRemoveListener {
                confirmRemove(it)
            }
            .setConfirmListener {

                if (it.hasBeenBought && it.info.isEmpty()) {
                    rvProductsOnBoughtItemClick(it, true)
                } else viewModel.updateProductAsIs(it)


            }.build().show()
    }

    override fun rvProductsOnBoughtItemClick(product: Product, isBought: Boolean) {

        if (isBought && !viewModel.establishmentConfirmed) {

            confirmEstablishment()
            // se o estabelecimento nao foi definido, atualizo o produto no db, assim a view do recyclerview é atualizada, desmarcando
            // o checkbox do produto
            viewModel.updateProductBoughtState(product, product.hasBeenBought)

        } else if (product.info.isEmpty() && isBought) BsdAddProductInfo(this@ProductsActivity) { info ->
            viewModel.updateProductBoughtState(product.copy(info = info), true)
        }.show()
        else viewModel.updateProductBoughtState(product, isBought)

    }

    override fun rvProductsOnAnnotationsClick(product: Product) {
        MaterialAlertDialogBuilder(this@ProductsActivity)
            .setTitle(getString(R.string.Anotacoes_do_produto))
            .setMessage(product.annotations)
            .setCancelable(true)
            .setPositiveButton(getString(R.string.Ok)) { dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    override fun rvCategoriesOnSelect(category: Category, adapterPosition: Int) {
        Vibrator.interaction()
        viewModel.filterByCategory(category)

        (binding.rvCategories.layoutManager as LinearLayoutManager).smoothScrollToPosition(
            binding.rvCategories, null, if (lastAdapterPosition < adapterPosition) min(
                rvAdapterCategories.itemCount - 1, adapterPosition + 1
            )
            else max(0, adapterPosition - 1)
        )
        lastAdapterPosition = adapterPosition
    }

}