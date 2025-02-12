package dev.gmarques.compras.ui.profile

import android.app.AlertDialog
import android.os.Bundle
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import dev.gmarques.compras.BuildConfig
import dev.gmarques.compras.R
import dev.gmarques.compras.data.PreferencesHelper
import dev.gmarques.compras.data.model.SyncAccount
import dev.gmarques.compras.data.repository.UserRepository
import dev.gmarques.compras.databinding.ActivityProfileBinding
import dev.gmarques.compras.databinding.ItemGuestOrHostBinding
import dev.gmarques.compras.databinding.ItemSyncInviteBinding
import dev.gmarques.compras.ui.Vibrator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.system.exitProcess

class ProfileActivity : AppCompatActivity() {

    private var state: ProfileActivityViewModel.ProfileActivityState? = null
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
        setupDebugOptions()

    }

    private fun setupDebugOptions() = binding.apply {
        if (BuildConfig.DEBUG) {
            cbProductionDatabase.visibility = VISIBLE

            cbProductionDatabase.isChecked =
                PreferencesHelper().getValue(PreferencesHelper.PrefsKeys.PRODUCTION_DATABASE, false)

            cbProductionDatabase.setOnCheckedChangeListener { _, checked ->
                PreferencesHelper().saveValue(
                    PreferencesHelper.PrefsKeys.PRODUCTION_DATABASE,
                    checked
                )
                exitProcess(0)
            }
        }

    }

    private fun observeStateUpdates() {

        viewModel.uiStateLd.observe(this) { newState ->

            loadInvitesViews(newState.requests)
            loadGuestsViews(newState.guests)
            loadHostView(newState.host)

            this.state = newState
        }
    }

    /**Carrega as views de solicitações de sincronismo*/
    private fun loadInvitesViews(
        accountsInfo: List<SyncAccount>?,
    ) {


        binding.llSyncInvites.removeAllViews()
        binding.llParentSyncInvites.visibility = if (accountsInfo.isNullOrEmpty()) GONE else VISIBLE

        accountsInfo?.forEach { req ->
            val item = ItemSyncInviteBinding.inflate(layoutInflater)

            item.tvName.text = req.name
            item.tvEmail.text = req.email

            Glide.with(binding.root.context).load(req.photoUrl).circleCrop()
                .placeholder(R.drawable.vec_invite_user).into(item.ivProfilePicture)

            item.root.setOnClickListener {
                Vibrator.interaction()
                showViewSyncInviteDialog(req)
            }

            binding.llSyncInvites.addView(item.root)
        }

    }

    /**Carrega as views de guests*/
    private fun loadGuestsViews(
        guests: List<SyncAccount>?,
    ) {

        binding.llGuests.removeAllViews()
        binding.llParentGuests.visibility = if (guests.isNullOrEmpty()) GONE else VISIBLE

        guests?.forEach { guest ->
            val item = ItemGuestOrHostBinding.inflate(layoutInflater)

            item.tvName.text = guest.name

            Glide.with(binding.root.context).load(guest.photoUrl).circleCrop()
                .placeholder(R.drawable.vec_invite_user).into(item.ivProfilePicture)

            item.root.setOnClickListener {
                Vibrator.interaction()
                showDisconnectGuestDialog(guest)
            }

            binding.llGuests.addView(item.root)
        }

    }


    /**Carrega a view do anfitriao*/
    private fun loadHostView(
        host: List<SyncAccount>?,
    ) {

        binding.llHost.removeAllViews()
        binding.llParentHost.visibility = if (host.isNullOrEmpty()) GONE else VISIBLE

        host?.forEach { req ->
            val item = ItemGuestOrHostBinding.inflate(layoutInflater)

            item.tvName.text = req.name

            Glide.with(binding.root.context).load(req.photoUrl).circleCrop()
                .placeholder(R.drawable.vec_invite_user).into(item.ivProfilePicture)

            item.root.setOnClickListener {
                Vibrator.interaction()
                showDisconnectFromHostDialog(req)
            }

            binding.llHost.addView(item.root)
        }

    }

    private fun showViewSyncInviteDialog(req: SyncAccount) {
        BsdManageSyncInvite(req, this, lifecycleScope).show()
    }

    private fun showDisconnectGuestDialog(guest: SyncAccount) {
        BsdDisconnectAccount(guest, this, lifecycleScope, false).show()
    }

    private fun showDisconnectFromHostDialog(host: SyncAccount) {
        BsdDisconnectAccount(host, this, lifecycleScope, true).show()
    }

    private fun setupLogOff() {

        binding.tvLogOff.setOnClickListener {
            Vibrator.interaction()
            showDialog(
                getString(R.string.Por_favor_confirme),
                getString(R.string.Voce_sera_desconectado_a_e_todos_os_dados_locais_ser_o_removidos_deseja_mesmo_continuar),
                getString(R.string.Sair)
            ) {

                UserRepository.logOff(this@ProfileActivity) { error ->
                    if (error == null) {
                        Firebase.firestore.clearPersistence()
                        PreferencesHelper().removeValue(PreferencesHelper.PrefsKeys.HOST)
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
        AlertDialog.Builder(this@ProfileActivity).setTitle(title).setMessage(msg)
            .setPositiveButton(confirm) { dialog, _ ->
                dialog.dismiss()
                callback()
            }.show()
    }

    private fun setupRequestPermission() {
        binding.tvRequestPermission.setOnClickListener {

            if (state!!.guests!!.isEmpty()) {
                Vibrator.interaction()
                BsdSendSyncInvite(this, lifecycleScope).show()
            } else {
                Vibrator.error()
                showDialog(
                    getString(R.string.Erro),
                    getString(R.string.Voce_nao_pode_conectar_sua_conta_a_outra_enquanto),
                    getString(R.string.Entendi)
                ) {}
            }
        }

    }

    /**
     * Configura a toolbar da activity.
     */
    private fun setupToolbar() = binding.toolbar.apply {

        tvActivityTitle.text = getString(R.string.Perfil)
        ivGoBack.setOnClickListener { Vibrator.interaction(); finish() }
        ivMenu.visibility = GONE

    }

    private fun loadUserData() {
        val user = UserRepository.getUser()!!

        binding.tvUserName.text = user.displayName

        user.photoUrl?.let { photoUrl ->
            Glide.with(binding.root.context).load(photoUrl).circleCrop()
                .into(binding.ivProfilePicture)
        }
    }


}



