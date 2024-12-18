package dev.gmarques.compras.ui.products

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import dev.gmarques.compras.data.data.model.Product
import dev.gmarques.compras.databinding.ActivityProductsBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.random.Random

class ProductsActivity : AppCompatActivity(), ProductAdapter.Callback {

    private lateinit var viewModel: ProductsActivityViewModel
    private lateinit var binding: ActivityProductsBinding
    private lateinit var rvAdapter: ProductAdapter


    companion object {
        private const val LIST_ID = "list_id"
        private const val LIST_NAME = "list_name"

        fun newIntent(context: Context, name: String, id: Long): Intent {
            return Intent(context, ProductsActivity::class.java).apply {
                putExtra(LIST_ID, id)
                putExtra(LIST_NAME, name)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityProductsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[ProductsActivityViewModel::class.java]

        val shopListId = intent.getLongExtra(LIST_ID, -1)
        val shopListName = intent.getStringExtra(LIST_NAME)

        initToolbar(shopListName)
        viewModel.observeProducts(shopListId)

        initRecyclerView()
        observeProductsUpdates()

        runBlocking {
            repeat(100) {
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
    }

    private fun initToolbar(shopListName: String?) {
        binding.tvTitle.text = shopListName
        binding.ivGoBack.setOnClickListener {
            finish()
        }

    }

    private fun observeProductsUpdates() {
        viewModel.productsLiveData.observe(this) { newData ->
            if (!newData.isNullOrEmpty()) {
                val sorted = newData.sortedBy { it.position }
                rvAdapter.submitList(sorted)
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

    override fun rvProductsOnDragAndDrop(toPosition: Int, product: Product) {
        viewModel.updateProductPosition(product, toPosition)
    }

    override fun rvProductsOnEditItemClick(product: Product) {
        BsdEditProductPriceOrQuantity(this, product).setOnConfirmListener {
            viewModel.updateProductAsIs(it)
        }.show()
    }

    override fun rvProductsOnBoughtItemClick(product: Product, isBought: Boolean) {
        viewModel.updateProductBoughtState(product, isBought)
    }
}