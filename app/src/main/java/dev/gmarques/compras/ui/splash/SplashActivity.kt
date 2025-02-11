package dev.gmarques.compras.ui.splash

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseUser
import dev.gmarques.compras.App
import dev.gmarques.compras.R
import dev.gmarques.compras.data.firestore.Firestore
import dev.gmarques.compras.data.model.ShopList
import dev.gmarques.compras.data.repository.UserRepository
import dev.gmarques.compras.databinding.ActivitySplashBinding
import dev.gmarques.compras.ui.Vibrator
import dev.gmarques.compras.ui.add_edit_shop_list.AddEditShopListActivity
import dev.gmarques.compras.ui.login.LoginActivity
import dev.gmarques.compras.ui.main.MainActivity
import dev.gmarques.compras.ui.main.MainActivityViewModel
import dev.gmarques.compras.ui.main.ShopListAdapter
import dev.gmarques.compras.ui.products.ProductsActivity
import dev.gmarques.compras.ui.profile.ProfileActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.util.Calendar


class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.root.postDelayed({ checkUserAuthenticated() }, 100)

    }

    private fun checkUserAuthenticated() = lifecycleScope.launch {

        val user = UserRepository.getUser()

        if (user != null) {
            setupDataBase()

        } else {
            startActivity(
                Intent(
                    applicationContext,
                    LoginActivity::class.java
                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
            this@SplashActivity.finishAffinity()
        }
    }

    private fun setupDataBase() = runBlocking {
        Firestore.setupDatabase()
        startActivity(Intent(this@SplashActivity, MainActivity::class.java))
        finish()
    }


}



