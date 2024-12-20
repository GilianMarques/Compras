package dev.gmarques.compras.ui.products

import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.animation.AnticipateInterpolator
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import dev.gmarques.compras.data.data.model.Product
import dev.gmarques.compras.databinding.ActivityProductsBinding
import dev.gmarques.compras.ui.Vibrator
import dev.gmarques.compras.utils.ExtFun.Companion.currencyToDouble
import dev.gmarques.compras.utils.ExtFun.Companion.toCurrency
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlin.random.Random

class ProductsActivity : AppCompatActivity(), ProductAdapter.Callback {

    private lateinit var viewModel: ProductsActivityViewModel
    private lateinit var binding: ActivityProductsBinding
    private lateinit var rvAdapter: ProductAdapter
    private var fabHidden: Boolean = false

    companion object {
        private const val LIST_ID = "list_id"
        private const val LIST_NAME = "list_name"
        private const val LIST_COLOR = "list_color"

        fun newIntent(context: Context, name: String, color: Int, id: Long): Intent {
            return Intent(context, ProductsActivity::class.java).apply {
                putExtra(LIST_ID, id)
                putExtra(LIST_NAME, name)
                putExtra(LIST_COLOR, color)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val shopListId = intent.getLongExtra(LIST_ID, -1)
        val shopListName = intent.getStringExtra(LIST_NAME)
        val shopListColor = intent.getIntExtra(LIST_COLOR, 0)

        binding = ActivityProductsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[ProductsActivityViewModel::class.java]

        initToolbar(shopListName, shopListColor)
        initRecyclerView()
        initSearch()
        initFabAnimation()
        observeProductsUpdates()
        observePrices()

        runBlocking {
            repeat(15) {
                delay(1)
                val x = Product(
                    shopListId,
                    "produto #$it",
                    -1,
                    Random.nextDouble(499.967),
                    Random.nextInt(5),
                    "info referente ao produto #$it",
                    true
                )
                //    ProductRepository.addOrUpdateProduct(x)
            }
        }

        viewModel.shopListId = shopListId
        viewModel.observeProducts()

    }

    private fun observePrices() {

        viewModel.pricesLiveData.observe(this) {
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
    }

    private fun initSearch() {

        binding.edtSearch.doOnTextChanged { text, start, before, count ->

            val term = text.toString()
            // if (term.length in 1..2) return@doOnTextChanged

            rvAdapter.submitList(emptyList())
            viewModel.searchProduct(term)


            rvAdapter.toggleDragnDropState(term.isEmpty())
            binding.ivClearSearch.visibility = if (term.isEmpty()) GONE else VISIBLE
        }

        binding.ivClearSearch.setOnClickListener {
            binding.edtSearch.setText("")
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(binding.edtSearch.windowToken, 0)
        }

    }

    private fun initToolbar(shopListName: String?, shopListColor: Int) {
        binding.tvTitle.text = shopListName
        // TODO: remover codigo relacionado a cor de fundo se nao for usar
        //binding.appbar.setBackgroundColor(shopListColor)
        binding.ivGoBack.setOnClickListener {
            finish()
        }

    }

    private fun observeProductsUpdates() {
        viewModel.productsLiveData.observe(this) { newData ->
            val sorted = newData.sortedBy { it.position }
            rvAdapter.submitList(sorted)

            if (binding.edtSearch.text.toString().isNotEmpty()) {
                when (sorted.isEmpty()) {
                    true -> Vibrator.error()
                    false -> Vibrator.success()
                }
            }
        }
    }

    private fun initRecyclerView() {

        rvAdapter = ProductAdapter(this@ProductsActivity)


        val dragDropHelper = DragDropHelperCallback(rvAdapter)
        val touchHelper = ItemTouchHelper(dragDropHelper)

        rvAdapter.attachItemTouchHelper(touchHelper)

        touchHelper.attachToRecyclerView(binding.rvProducts)

        binding.rvProducts.layoutManager = LinearLayoutManager(this)
        binding.rvProducts.adapter = rvAdapter
    }

    private fun initFabAnimation() = binding.apply {
        rvProducts.addOnScrollListener(object : OnScrollListener() {


            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0) {  // Rolando para cima - Esconde o FAB
                    if (fabHidden) return

                    fabAddProduct.animate()
                        .translationY(fabAddProduct.height.toFloat() * 2)
                        .alpha(0f)
                        .setStartDelay(100)
                        .setDuration(200L)
                        .start()
                    fabHidden = true

                } else if (dy < 0) { // Rolando para baixo - Mostra o FAB
                    if (!fabHidden) return

                    fabAddProduct.animate()
                        .translationY(0f)
                        .alpha(1f)
                        .setStartDelay(100)
                        .setDuration(200L)
                        .start()
                    fabHidden = false
                }
            }
        })
    }

    override fun rvProductsOnDragAndDrop(toPosition: Int, product: Product) {
        Log.d("USUK", "ProductsActivity.".plus("rvProductsOnDragAndDrop() 1 = toPosition = $toPosition, product = $product"))

        viewModel.updateProductPosition(product, toPosition)

    }

    override fun rvProductsOnEditItemClick(product: Product) {

        BsdEditProductPriceOrQuantity(this@ProductsActivity, product).setOnConfirmListener {
            viewModel.updateProductAsIs(it)
        }.show()
    }

    override fun rvProductsOnBoughtItemClick(product: Product, isBought: Boolean) {
        viewModel.updateProductBoughtState(product, isBought)
    }
}