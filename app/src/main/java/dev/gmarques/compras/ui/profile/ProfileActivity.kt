package dev.gmarques.compras.ui.profile

import android.app.AlertDialog
import android.os.Bundle
import android.view.View.GONE
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import dev.gmarques.compras.R
import dev.gmarques.compras.data.repository.UserRepository
import dev.gmarques.compras.databinding.ActivityProfileBinding
import dev.gmarques.compras.ui.Vibrator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


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
        setupLogOff()
    }

    private fun setupLogOff() {

        binding.tvLogOff.setOnClickListener {
            showDialog(
                getString(R.string.Por_favor_confirme),
                getString(R.string.Voce_sera_desconectado_a_e_todos_os_dados_locais_ser_o_removidos_deseja_mesmo_continuar),
                getString(R.string.Sair)
            ) {

                UserRepository.logOff(this@ProfileActivity) { error ->
                    if (error == null) {
                        Firebase.firestore.clearPersistence()
                        Vibrator.success()
                        lifecycleScope.launch(Dispatchers.IO) {
                            delay(1000)
                            finishAffinity()

                        }
                    } else {
                        Vibrator.error()
                        Snackbar.make(
                            binding.root,
                            getString(R.string.Erro_fazendo_logoff_tente_novamente_mais_tarde),
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }

    private fun showDialog(title: String, msg: String, confirm: String, callback: () -> Any) {
        AlertDialog.Builder(this@ProfileActivity)
            .setTitle(title)
            .setMessage(msg)
            .setPositiveButton(confirm) { dialog, _ ->
                dialog.dismiss()
                callback()
            }.setNegativeButton(getString(R.string.Cancelar)) { dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    private fun setupRequestPermission() {
        binding.tvRequestPermission.setOnClickListener {
            BsdSendSyncRequest(this, lifecycleScope).show()
        }

    }

    /**
     * Configura a toolbar da activity.
     */
    private fun setupToolbar() = binding.toolbar.apply {

        tvActivityTitle.text = getString(R.string.Perfil)
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



