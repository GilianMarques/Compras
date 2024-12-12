package dev.gmarques.compras.ui.main_activity

import Dropdown
import MenuItem
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.androidpoet.dropdown.ExitAnimation
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.collection.LLRBNode
import dev.gmarques.compras.R
import dev.gmarques.compras.data.data.repository.UserRepository
import dev.gmarques.compras.databinding.ActivityMainBinding
import dev.gmarques.compras.ui.login.LoginActivity
import dropDownMenu
import dropDownMenuColors
import java.util.Calendar

class MainActivity : AppCompatActivity() {


    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainActivityViewModel
    private val rvAdapter = ShopListAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[MainActivityViewModel::class.java]

        checkUserAuthenticated()
        initRecyclerView()
        initFabAddList()
        observeRecyclerViewAtts()

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
        AddShopListBottomSheetDialog(this, rvAdapter.itemCount)
            .setOnConfirmListener { shopList ->
                viewModel.addShopList(shopList)
            }.show()
    }

    private fun observeRecyclerViewAtts() {

        viewModel.listsLiveData.observe(this@MainActivity) { newData ->
            if (!newData.isNullOrEmpty()) {
                rvAdapter.submitList(newData)
            }
        }
    }



    fun getMenu(): MenuItem<String> {
        val menu = dropDownMenu<String> {
            item("remove", "Remover") {
                item("remove_confirm", "Remover") {

                }

                item("remove_cancel", "Cancelar") {

                }
            }
            item("rename", "Renomear") {
            }
        }
        return menu
    }


}