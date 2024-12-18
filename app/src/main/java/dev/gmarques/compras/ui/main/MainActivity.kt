package dev.gmarques.compras.ui.main

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.Spanned
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseUser
import dev.gmarques.compras.R
import dev.gmarques.compras.data.data.model.ShopList
import dev.gmarques.compras.data.data.repository.UserRepository
import dev.gmarques.compras.databinding.ActivityMainBinding
import dev.gmarques.compras.ui.Vibrator
import dev.gmarques.compras.ui.login.LoginActivity
import dev.gmarques.compras.ui.products.ProductsActivity
import dev.gmarques.compras.utils.ExtFun.Companion.formatHtml
import java.util.Calendar


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainActivityViewModel
    private val rvAdapter = ShopListAdapter(::rvItemMenuclick, ::rvItemClick)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[MainActivityViewModel::class.java]

        checkUserAuthenticated()
        initRecyclerView()
        initFabAddList()
        observeListsUpdates()

    }

    private fun checkUserAuthenticated() {
        val user = UserRepository.getUser()

        if (user != null) attUiWithUserData(user)
        else {
            startActivity(
                Intent(
                    applicationContext, LoginActivity::class.java
                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )

            this@MainActivity.finishAffinity()
        }
    }

    private fun attUiWithUserData(user: FirebaseUser) {
        binding.tvUserName.text = user.displayName//?.split(" ")?.get(0) ?: ""

        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        binding.tvGreetings.text = when (currentHour) {
            in 0..11 -> getString(R.string.Bom_dia)
            in 12..17 -> getString(R.string.Boa_tarde)
            else -> getString(R.string.Boa_noite)
        }

        user.photoUrl?.let { photoUrl ->
            Glide.with(binding.root.context)
                .load(photoUrl)
                .circleCrop()
                .into(binding.ivProfilePicture)
        } ?: run {
            // Esconde a imagem se não houver foto
            binding.ivProfilePicture.visibility = View.GONE
            binding.tvUserName.visibility = View.GONE
            binding.tvGreetings.visibility = View.GONE
        }
    }

    private fun initRecyclerView() {

        binding.rv.layoutManager = LinearLayoutManager(this)
        binding.rv.adapter = rvAdapter
    }

    private fun initFabAddList() = binding.fabAddList.setOnClickListener {
        showDialogAddShopList()
    }

    private fun showDialogAddShopList() {
        BsdAddOrEditShopList(this, rvAdapter.itemCount)
            .setOnConfirmListener { shopList ->
                viewModel.addOrUpdateShopList(shopList)
            }.show()
    }

    private fun observeListsUpdates() {

        viewModel.listsLiveData.observe(this@MainActivity) { newData ->
            if (!newData.isNullOrEmpty()) rvAdapter.submitList(newData)
        }
    }

    private fun rvItemMenuclick(shopList: ShopList) {

        Vibrator.interaction()

        BsdShopListMenu(this, shopList,
            { renameList ->
                showRenameDialog(renameList)
            }, { removeList ->
                confirmRemove(removeList)
            })
            .show()
    }

    private fun showRenameDialog(renameList: ShopList) {
        BsdAddOrEditShopList(this, renameList)
            .setOnConfirmListener { shopList ->
                viewModel.addOrUpdateShopList(shopList)
            }.show()
    }

    private fun confirmRemove(shopList: ShopList) {
        val msg: Spanned = String.format(getString(R.string.Deseja_mesmo_remover_x), shopList.name).formatHtml()

        val dialogBuilder =
            AlertDialog.Builder(this).setTitle(getString(R.string.Por_favor_confirme))
                .setMessage(msg)
                .setPositiveButton(getString(R.string.Remover)) { dialog, _ ->
                    viewModel.removeShopList(shopList)
                    dialog.dismiss()
                }.setNegativeButton(getString(R.string.Cancelar)) { dialog, _ ->
                    dialog.dismiss()
                }

        val dialog = dialogBuilder.create()
        dialog.show()
    }

    private fun rvItemClick(shopList: ShopList) {
        Vibrator.interaction()
        val intent = ProductsActivity.newIntent(this, shopList.name, shopList.id)
        startActivity(intent)
    }
}



