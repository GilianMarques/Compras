package dev.gmarques.compras.ui.profile

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import dev.gmarques.compras.R
import dev.gmarques.compras.data.model.SyncRequest
import dev.gmarques.compras.data.repository.UserRepository
import dev.gmarques.compras.databinding.ActivityProfileBinding
import dev.gmarques.compras.databinding.ItemSyncRequestBinding
import dev.gmarques.compras.ui.Vibrator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import dev.gmarques.compras.ui.profile.ProfileActivityViewModel.ProfileActivityState
import kotlin.system.exitProcess

class ProfileActivity : AppCompatActivity() {

    private var currentState: ProfileActivityState = ProfileActivityState()
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
        observeStateUpdates()

    }

    private fun observeStateUpdates() {
        viewModel.uiStateLd.observe(this) { newState ->
            updateRequests(newState.requests)
            Log.d("USUK", "ProfileActivity.observeStateUpdates: requests ${currentState.requests}")
            currentState = newState
        }
    }

    private fun updateRequests(requests: List<SyncRequest>?) = binding.apply {

        llSyncRequests.removeAllViews()
        llParentSyncRequest.visibility = if (requests.isNullOrEmpty()) GONE else VISIBLE

        requests?.forEach { req ->
            val item = ItemSyncRequestBinding.inflate(layoutInflater)

            item.tvName.text = req.name
            item.tvEmail.text = req.email

            Glide.with(root.context)
                .load(req.photoUrl)
                .circleCrop()
                .placeholder(R.drawable.vec_invite_user)
                .into(item.ivProfilePicture)

            item.root.setOnClickListener {
                showViewSyncRequestDialog(req)
            }
            llSyncRequests.addView(item.root)

        }

    }

    private fun showViewSyncRequestDialog(req: SyncRequest) {
        BsdManageSyncRequest(req, this, lifecycleScope).show()

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
                        closeApp()

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

    private fun closeApp() = lifecycleScope.launch(Dispatchers.IO) {

        finishAffinity()
        exitProcess(0)
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



