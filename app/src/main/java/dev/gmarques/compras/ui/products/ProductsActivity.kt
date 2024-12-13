package dev.gmarques.compras.ui.products

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import dev.gmarques.compras.databinding.ActivityShopListBinding

class ProductsActivity : AppCompatActivity() {

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

        /*  repeat(20) {
              val x = Product(shopListId, name = "produto #$it",0,Random.nextDouble(49.967),"no info", "no obs")
              ProductRepository.addOrAttProduct(x)
          }*/
    }

    private fun observeProductsUpdates() {
        viewModel.productsLiveData.observe(this) { newData ->
            if (!newData.isNullOrEmpty()) rvAdapter.submitList(newData)
        }
    }


    private fun initRecyclerView() {

        rvAdapter = ProductAdapter(
            { fromPosition: Int, toPosition: Int ->
                Log.d(
                    "USUK",
                    "ProductsActivity.".plus("initRecyclerView() fromPosition = $fromPosition, toPosition = $toPosition")
                )
            },
        )


        val dragDropHelper = DragDropHelperCallback(rvAdapter)
        val touchHelper = ItemTouchHelper(dragDropHelper)

        rvAdapter.setItemTouchHelper(touchHelper)

        touchHelper.attachToRecyclerView(binding.rvProducts)

        binding.rvProducts.layoutManager = LinearLayoutManager(this)
        binding.rvProducts.adapter = rvAdapter
    }
}