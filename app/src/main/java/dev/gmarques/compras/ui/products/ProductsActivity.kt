package dev.gmarques.compras.ui.products

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import dev.gmarques.compras.data.data.model.Product
import dev.gmarques.compras.databinding.ActivityShopListBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.random.Random

class ProductsActivity : AppCompatActivity(), ProductAdapter.Callback {

    private lateinit var viewModel: ProductsActivityViewModel
    private lateinit var binding: ActivityShopListBinding
    private lateinit var rvAdapter: ProductAdapter


    companion object {
        private const val LIST_ID = "list_id"

        fun newIntent(context: Context, id: Long): Intent {
            return Intent(context, ProductsActivity::class.java).apply {
                putExtra(LIST_ID, id)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityShopListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[ProductsActivityViewModel::class.java]

        val shopListId = intent.getLongExtra(LIST_ID, -1)

        viewModel.observeProducts(shopListId)

        initRecyclerView()
        observeProductsUpdates()

        runBlocking {
            repeat(10) {
                delay(2)
                val x = Product(
                    shopListId,
                    "produto #$it",
                    -1,
                    Random.nextDouble(499.967),
                    Random.nextInt(5),
                    "info referente ao produto #$it",
                    false
                )
                //   ProductRepository.addOrUpdateProduct(x)
            }
        }
    }

    private fun observeProductsUpdates() {
        viewModel.productsLiveData.observe(this) { newData ->
            if (!newData.isNullOrEmpty()) {
                rvAdapter.submitList(newData.sortedBy { it.position })
            }
        }
    }

    private fun initRecyclerView() {

        rvAdapter = ProductAdapter(this@ProductsActivity)


        val dragDropHelper = DragDropHelperCallback(rvAdapter)
        val touchHelper = ItemTouchHelper(dragDropHelper)

        rvAdapter.setItemTouchHelper(touchHelper)

        touchHelper.attachToRecyclerView(binding.rvProducts)

        binding.rvProducts.layoutManager = LinearLayoutManager(this)
        binding.rvProducts.adapter = rvAdapter
    }

    override fun rvProductsOnDragAndDrop(toPosition: Int, product: Product) {
        Log.d("USUK", "ProductsActivity.".plus("rvProductsOnDragAndDrop() product = ${product.name}, toPosition = $toPosition, "))
        viewModel.updateProductPosition(product, toPosition)
    }

    override fun rvProductsOnEditPriceClick(product: Product) {
        BsdEditProductPriceOrQuantity(this, product, true)
            .setOnConfirmListener {
                viewModel.updateProductAsIs(it)
            }.show()
    }

    override fun rvProductsOnEditQuantityClick(product: Product) {
        BsdEditProductPriceOrQuantity(this, product, false)
            .setOnConfirmListener {
                viewModel.updateProductAsIs(it)
            }.show()
    }

    override fun rvProductsOnEditItemClick(product: Product) {
        Log.d("USUK", "ProductsActivity.".plus("rvProductsOnEditItemClick() product = $product"))
    }

    override fun rvProductsOnBoughtItemClick(product: Product, isBought: Boolean) {
        viewModel.updateProductBoughtState(product, isBought)
    }
}