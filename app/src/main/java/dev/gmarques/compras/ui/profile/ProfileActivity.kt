package dev.gmarques.compras.ui.profile

import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.view.View.GONE
import android.view.WindowInsetsController
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseUser
import dev.gmarques.compras.App
import dev.gmarques.compras.R
import dev.gmarques.compras.data.model.ShopList
import dev.gmarques.compras.data.repository.UserRepository
import dev.gmarques.compras.databinding.ActivityMainBinding
import dev.gmarques.compras.databinding.ActivityProfileBinding
import dev.gmarques.compras.ui.Vibrator
import dev.gmarques.compras.ui.login.LoginActivity
import dev.gmarques.compras.ui.products.ProductsActivity
import java.util.Calendar


class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var viewModel: ProfileActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[ProfileActivityViewModel::class.java]

        setupToolbar()
        loadUserData()
        setupRequestPermission()
    }

    private fun setupRequestPermission() {
        binding.tvRequestPermission.setOnClickListener{
            BsdSendSyncRequest(this).show()
        }

    }

    /**
     * Configura a toolbar da activity.
     */
    private fun setupToolbar() = binding.toolbar.apply {

        tvActivityTitle.text = getString(R.string.Gerencie_sua_conta)
        ivGoBack.setOnClickListener { finish() }
        ivMenu.visibility = GONE

    }


    private fun loadUserData() {
        val user = UserRepository.getUser()!!

        binding.tvUserName.text = user.displayName

        user.photoUrl?.let { photoUrl ->
            Glide.with(binding.root.context)
                .load(photoUrl)
                .circleCrop()
                .into(binding.ivProfilePicture)
        }
    }

}



