package dev.gmarques.compras.ui.products

import android.animation.ValueAnimator
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Spanned
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.animation.AnticipateInterpolator
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import dev.gmarques.compras.R
import dev.gmarques.compras.data.model.Product
import dev.gmarques.compras.data.model.ShopList
import dev.gmarques.compras.databinding.ActivityProductsBinding
import dev.gmarques.compras.ui.Vibrator
import dev.gmarques.compras.ui.add_edit_product.AddEditProductActivity
import dev.gmarques.compras.ui.main.BsdAddOrEditShopList
import dev.gmarques.compras.utils.ExtFun.Companion.currencyToDouble
import dev.gmarques.compras.utils.ExtFun.Companion.formatHtml
import dev.gmarques.compras.utils.ExtFun.Companion.observeOnce
import dev.gmarques.compras.utils.ExtFun.Companion.toCurrency
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProductsActivity : AppCompatActivity(), ProductAdapter.Callback {

    private lateinit var viewModel: ProductsActivityViewModel
    private lateinit var binding: ActivityProductsBinding
    private lateinit var rvAdapter: ProductAdapter
    private var fabHidden: Boolean = false


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

        val shoplistId = intent.getStringExtra(LIST_ID)!!

        binding = ActivityProductsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[ProductsActivityViewModel::class.java]
        viewModel.init(shoplistId)

        initToolbar()
        initRecyclerView()
        initSearch()
        initFabAddProduct()
        observeProductsUpdates()
        observePrices()
        observeShopList()

        /*   runBlocking {
               repeat(15) {
                   delay(100)
                   val x = Product(
                       shopListId,
                       "produto #$it",
                       -1,
                       Random.nextDouble(499.967),
                       Random.nextInt(5),
                       "info referente ao produto #$it",
                       true
                   )
                       ProductRepository.addOrUpdateProduct(x)
               }
           }*/
        // startActivityAddProduct() // TODO: remover

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
        binding.toolbar.tvActivityTitle.text = it.name
    }

    @Suppress("UNUSED_ANONYMOUS_PARAMETER")
    private fun initSearch() {

        binding.edtSearch.doOnTextChanged { text, start, before, count ->

            val term = text.toString()
            // if (term.length in 1..2) return@doOnTextChanged

            rvAdapter.submitList(emptyList())
            viewModel.searchProduct(term)

            binding.ivClearSearch.visibility = if (term.isEmpty()) GONE else VISIBLE
        }

        binding.ivClearSearch.setOnClickListener {
            binding.edtSearch.setText("")
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(binding.edtSearch.windowToken, 0)
        }

    }

    private fun initToolbar() {
        binding.toolbar.ivGoBack.setOnClickListener {
            finish()
        }

        binding.toolbar.ivMenu.setOnClickListener {
            showMenuDialog()
        }

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

    private fun initRecyclerView() {

        rvAdapter = ProductAdapter(this@ProductsActivity)
        binding.rvProducts.layoutManager = LinearLayoutManager(this)
        binding.rvProducts.adapter = rvAdapter
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
            val intent = AddEditProductActivity.newIntentAddProduct(this@ProductsActivity, shopList.id)
            startActivity(intent)
        }
    }

    private fun startActivityEditProduct(product: Product) {
        viewModel.shopListLD.observeOnce(this@ProductsActivity) { shopList ->

            Vibrator.interaction()
            val intent = AddEditProductActivity.newIntentEditProduct(this@ProductsActivity, shopList.id, product.id)
            startActivity(intent)
        }
    }

    private fun showMenuDialog() {
        viewModel.shopListLD.observeOnce(this) {
            Vibrator.interaction()

            BsdShopListMenu(this, it, { renameList ->
                showRenameDialog(renameList)
            }, { removeList ->
                confirmRemove(removeList)
            }, {
                showSortProductsDialog()
            }).show()
        }
    }

    private fun showSortProductsDialog() {
        BsdSortProducts(this) {
            viewModel.loadPreferences()
            viewModel.searchProduct("")//força os dados a serem recarregados, filtrados e ordenados
        }.show()
    }

    private fun showRenameDialog(renameList: ShopList) {
        BsdAddOrEditShopList(this, renameList).setOnConfirmListener { shopList ->
            viewModel.addOrUpdateShopList(shopList)
        }.show()
    }

    private fun confirmRemove(shopList: ShopList) {
        val msg: Spanned = String.format(getString(R.string.Deseja_mesmo_remover_x), shopList.name).formatHtml()

        val dialogBuilder = AlertDialog.Builder(this).setTitle(getString(R.string.Por_favor_confirme)).setMessage(msg)
            .setPositiveButton(getString(R.string.Remover)) { dialog, _ ->
                viewModel.removeShopList(shopList)
                dialog.dismiss()
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
}