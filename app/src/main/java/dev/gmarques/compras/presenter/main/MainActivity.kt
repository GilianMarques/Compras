package dev.gmarques.compras.presenter.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseUser
import dev.gmarques.compras.R
import dev.gmarques.compras.data.model.Category
import dev.gmarques.compras.data.model.Product
import dev.gmarques.compras.data.model.ShopList
import dev.gmarques.compras.data.repository.CategoryRepository
import dev.gmarques.compras.data.repository.ProductRepository
import dev.gmarques.compras.data.repository.ShopListRepository
import dev.gmarques.compras.data.repository.SuggestionProductRepository
import dev.gmarques.compras.data.repository.UserRepository
import dev.gmarques.compras.data.repository.model.ValidatedCategory
import dev.gmarques.compras.data.repository.model.ValidatedProduct
import dev.gmarques.compras.data.repository.model.ValidatedShopList
import dev.gmarques.compras.data.repository.model.ValidatedSuggestionProduct
import dev.gmarques.compras.databinding.ActivityMainBinding
import dev.gmarques.compras.presenter.MyActivity
import dev.gmarques.compras.presenter.Vibrator
import dev.gmarques.compras.presenter.add_edit_shop_list.AddEditShopListActivity
import dev.gmarques.compras.presenter.products.ProductsActivity
import dev.gmarques.compras.presenter.profile.ProfileActivity
import java.util.Calendar


class MainActivity : MyActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainActivityViewModel
    private val rvAdapter = ShopListAdapter(::rvItemClick, ::rvLongItemClick)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[MainActivityViewModel::class.java]

        attUiWithUserData(UserRepository.getUser()!!)
        setupRecyclerView()
        setupFabAddList()
        observeListsUpdates()

        //   lifecycleScope.launch { populateForTest() }
        // lifecycleScope.launch { Migration_1_2().beginMigration() }
     //   lifecycleScope.launch { FirebaseCloneDatabase("gilian762@gmail.com", "vizzualserigrafia@gmail.com").beginCloning() }

    }

    private suspend fun populateForTest() = repeat(5) {

        val name = UserRepository.getUser()!!.email!!.split("@")[0]

        val list = ShopList("lista de $name", 125)
        val category = Category(name = "categoria de $name", color = 12345)
        val product = Product(
            name = "produto de $name",
            price = 1.5,
            quantity = 1,
            info = "",
            shopListId = list.id,
            categoryId = category.id,
            position = 0
        )
        repeat(100) {
            ProductRepository.addOrUpdateProduct(ValidatedProduct(product.withNewId()))
            SuggestionProductRepository.updateOrAddProductAsSuggestion(
                ValidatedSuggestionProduct(
                    product.withNewId()
                )
            )
        }
        ShopListRepository.addOrUpdateShopList(ValidatedShopList(list))
        CategoryRepository.addOrUpdateCategory(ValidatedCategory(category))
    }

    private fun attUiWithUserData(user: FirebaseUser) = binding.apply {
        binding.tvUserName.text = user.displayName

        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        tvGreetings.text = when (currentHour) {
            in 0..11 -> getString(R.string.Bom_dia)
            in 12..17 -> getString(R.string.Boa_tarde)
            else -> getString(R.string.Boa_noite)
        }

        user.photoUrl?.let { photoUrl ->
            Glide.with(root.context).load(photoUrl).circleCrop().into(ivProfilePicture)
        } ?: run {
            // Esconde a imagem se não houver foto
            ivProfilePicture.visibility = View.GONE
            tvUserName.visibility = View.GONE
            tvGreetings.visibility = View.GONE
        }

        listOf(ivMenu, tvUserName, ivProfilePicture).forEach { view ->
            view.setOnClickListener {
                startActivity(Intent(this@MainActivity, ProfileActivity::class.java))
                Vibrator.interaction()
            }
        }

    }

    private fun setupRecyclerView() {
        binding.rv.layoutManager = LinearLayoutManager(this)
        binding.rv.adapter = rvAdapter
    }

    private fun setupFabAddList() = binding.fabAddList.setOnClickListener {

        startActivity(AddEditShopListActivity.newIntentAddShopList(this))
    }

    private fun observeListsUpdates() {

        viewModel.listsLiveData.observe(this@MainActivity) { newData ->
            if (!newData.isNullOrEmpty()) {
                rvAdapter.submitList(newData)
            }
        }
    }

    private fun rvItemClick(shopList: ShopList) {
        startActivityProducts(shopList)
    }

    private fun rvLongItemClick(shopList: ShopList) {
        startActivity(
            AddEditShopListActivity.newIntentEditShopList(
                this, shopList.id
            )
        )
    }

    private fun startActivityProducts(shopList: ShopList) {
        Vibrator.interaction()
        val intent = ProductsActivity.newIntent(this, shopList.id)
        startActivity(intent)
    }


}
