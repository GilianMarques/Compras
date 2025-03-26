package dev.gmarques.compras.ui.profile

import android.os.Bundle
import android.view.View.GONE
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.firebase.ui.auth.AuthUI
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import dev.gmarques.compras.App
import dev.gmarques.compras.BuildConfig
import dev.gmarques.compras.R
import dev.gmarques.compras.data.PreferencesHelper
import dev.gmarques.compras.data.model.SyncAccount
import dev.gmarques.compras.data.repository.UserRepository
import dev.gmarques.compras.databinding.ActivityProfileBinding
import dev.gmarques.compras.databinding.ItemGuestOrHostBinding
import dev.gmarques.compras.databinding.ItemSyncInviteBinding
import dev.gmarques.compras.ui.MyActivity
import dev.gmarques.compras.ui.Vibrator
import kotlinx.coroutines.launch
import kotlin.system.exitProcess


class ProfileActivity: MyActivity() {

    private var state: ProfileActivityViewModel.UiState? = null
    private lateinit var binding: ActivityProfileBinding
    private lateinit var viewModel: ProfileActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[ProfileActivityViewModel::class.java]

        setupToolbar()
        loadUserData()
        setupSendInvite()
        setupLogOff()
        observeStateUpdates()
        setupDebugOptions()
        setupDeleteAccount()

    }

    private fun setupDeleteAccount() {
        // TODO: fazer acontecer

        /*AuthUI.getInstance()
            .delete(this)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Deletion succeeded
                } else {
                    // Deletion failed
                }
            }*/
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
                cbProductionDatabase.postDelayed({ exitProcess(0) }, 1000)
            }
        }

    }

    private fun observeStateUpdates() {

        viewModel.uiStateLd.observe(this) { newState ->

            loadInvitesViews(newState.requests)
            updateUiWithSyncAccountsViews(newState.guests, newState.host)
            this.state = newState
        }
    }

    private fun updateUiWithSyncAccountsViews(guests: List<SyncAccount>, host: List<SyncAccount>) {

        // reseto o estado dos containers que comportam as views de convidado e anfitriao.
        binding.llParentSyncAccounts.visibility = if (host.isEmpty() && guests.isEmpty()) GONE else VISIBLE
        binding.llSyncAccounts.removeAllViews()

        // oculta a seção de enviar convites caso o usuario seja um convidado, pois se for, ele nao pode convidar.
        binding.llInvite.visibility = if (host.isEmpty()) VISIBLE else GONE
        binding.tvSendInvite.isEnabled = host.isEmpty()

        host.forEach { req -> loadSyncAccountView(req, true) }
        guests.forEach { req -> loadSyncAccountView(req) }
    }

    /**
     * Carrega as views de solicitações de sincronismo
     */
    private fun loadInvitesViews(accountsInfo: List<SyncAccount>?) {


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

    /**
     * Carrega as views de convidados e anfitriao
     */
    private fun loadSyncAccountView(syncAccount: SyncAccount, host: Boolean = false) {

        val item = ItemGuestOrHostBinding.inflate(layoutInflater)

        item.tvName.text = syncAccount.name

        Glide.with(binding.root.context).load(syncAccount.photoUrl).circleCrop()
            .placeholder(R.drawable.vec_invite_user).into(item.ivProfilePicture)

        item.root.setOnClickListener {
            Vibrator.interaction()
            if (host) showDisconnectFromHostDialog(syncAccount)
            else showDisconnectGuestDialog(syncAccount)
        }

        item.tvGuest.visibility = if (host) INVISIBLE else VISIBLE
        item.tvHost.visibility = if (host) VISIBLE else INVISIBLE

        binding.llSyncAccounts.addView(item.root)

    }

    /**
     * Mostra um bottomsheet dialog que permite gerenciar um convite de sincronismo
     * */
    private fun showViewSyncInviteDialog(req: SyncAccount) {
        BsdManageSyncInvite(
            req,
            state?.guests.isNullOrEmpty() && state?.host.isNullOrEmpty(),
            this,
            lifecycleScope
        ).show()

    }

    /**
     * Mostra um bottomsheet dialog que permite desconectar um convidado
     * */
    private fun showDisconnectGuestDialog(guest: SyncAccount) {
        BsdDisconnectAccount(guest, this, lifecycleScope, false).show()
    }

    /**
     * Mostra um bottomsheet dialog que permite que o usuario se desconecte do anfitriao
     * */
    private fun showDisconnectFromHostDialog(host: SyncAccount) {
        BsdDisconnectAccount(host, this, lifecycleScope, true).show()
    }

    private fun setupLogOff() {

        binding.tvLogOff.setOnClickListener {
            Vibrator.interaction()


            MaterialAlertDialogBuilder(this@ProfileActivity)
                .setTitle(getString(R.string.Por_favor_confirme))
                .setMessage(getString(R.string.Voce_sera_desconectado_a_e_todos_os_dados_locais_ser_o_removidos_deseja_mesmo_continuar))
                .setCancelable(true)
                .setPositiveButton(getString(R.string.Sair)) { dialog, _ ->
                    lifecycleScope.launch {
                        UserRepository.logOff(this@ProfileActivity) { error ->
                            if (error == null) {
                                Firebase.firestore.clearPersistence()
                                PreferencesHelper().clearAll()
                                Vibrator.success()
                                App.close(this@ProfileActivity)

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
                }.show()
        }
    }

    private fun setupSendInvite() {
        binding.tvSendInvite.setOnClickListener {

            val accountsInSync = (state?.guests.orEmpty() + state?.host.orEmpty())

            if (state!!.host.isEmpty()) {
                Vibrator.interaction()
                BsdSendSyncInvite(this, lifecycleScope, accountsInSync).show()
            } else {
                Vibrator.error()
                MaterialAlertDialogBuilder(this@ProfileActivity)
                    .setTitle(getString(R.string.Erro))
                    .setMessage(getString(R.string.Voce_nao_pode_convidar_um_usu_rio_para_a_sua_conta_enquanto_for_convidado_de_outra_pessoa))
                    .setPositiveButton(getString(R.string.Entendi)) { dialog, _ ->
                        dialog.dismiss()
                    }
                    .setCancelable(false)
                    .show()
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



