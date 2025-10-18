package dev.gmarques.compras

import android.app.Activity
import android.app.Application
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.res.Configuration
import android.net.Uri
import android.util.Log
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import dev.gmarques.compras.data.repository.UserRepository
import dev.gmarques.compras.domain.utils.ListenerRegister
import dev.gmarques.compras.presenter.MyActivity
import dev.gmarques.compras.presenter.Vibrator
import dev.gmarques.compras.presenter.sync_stopped.SyncStoppedActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.system.exitProcess

class App : Application() {

    private lateinit var scope: CoroutineScope

    var darkModeEnabled: Boolean = false
        private set

    /**
     * Activity  visivel na tela. Ao setar uma instancia aqui, listeners serão chamados.
     */
    var currentActivity: MyActivity? = null
        set(value) {
            field = value
            field?.let { activityObservers.forEach { it.doWork() } }
        }

    /** Contem funçoes que serao executadas quando quando uma instancia de activity estiver disponivel */
    private val activityObservers = mutableListOf<ActivityObserver>()

    /**
     * Observa no firebase alterações no status de convidado do usuario atual, caso ele seja desconectado do host, esse listener
     * informara o app para que sejam tomadas as medidas necesárias.
     */
    private var guestsObserverListener: ListenerRegister? = null

    companion object {
        private lateinit var instance: App

        /**
         * Retorna o contexto global da aplicação.
         */
        fun getContext(): App = instance

        @OptIn(DelicateCoroutinesApi::class)
        fun close(targetActivity: Activity) {
            GlobalScope.launch(IO) {
                targetActivity.finishAffinity()
                delay(1000)
                exitProcess(0)
            }
        }

    }

    override fun onCreate() {
        super.onCreate()
        scope = CoroutineScope(Main)
        instance = this
        scope.launch { setupRemoteConfig() }
        checkDarkModeEnabled()
    }

    private suspend fun setupRemoteConfig() {

        val remoteConfig = Firebase.remoteConfig
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)

        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = if (BuildConfig.DEBUG) 10 else 3600
        }
        remoteConfig.setConfigSettingsAsync(configSettings)

        remoteConfig.fetchAndActivate().await()

        val blockBelow = remoteConfig.getLong("blockBelow")
        val latestVersion = remoteConfig.getLong("latestVersion")

        blockApp(blockBelow)
        showUpdateAvailableDialog(latestVersion)

    }

    /**
     *  Define o app como bloqueado com base em sua versao e configurações do remote config
     */
    private fun blockApp(blockBelow: Long) {
        val appBlocked = (BuildConfig.VERSION_CODE < blockBelow)
        if (appBlocked) {

            val showDialogAction = object : ActivityObserver {
                override fun doWork() {
                    showAppBlockedDialog()
                }
            }

            currentActivity?.let { showDialogAction.doWork() }

            /* no caso de bloqueio, sempre que uma activity for aberta
            o dialogo de trava deve ser exibido*/
            addActivityObserver(showDialogAction)
        }

    }

    /**
     * Se o app foi bloqueado, exibe um dialogo com orientações que impede o uso do app
     */
    private fun showAppBlockedDialog() {
        Vibrator.error()
        MaterialAlertDialogBuilder(currentActivity!!).setTitle(getString(R.string.Atencao))
            .setMessage(
                getString(
                    R.string.Esta_versao_do_app_foi_bloqueada_verifique_atualiza_es_na_play_store,
                    getString(R.string.app_name)
                )
            )
            .setPositiveButton(getString(R.string.Ir_a_loja)) { dialog, _ ->
                openPlayStore()
            }
            .setNegativeButton(getString(R.string.Sair)) { dialog, _ ->
                exitProcess(0)
            }
            .setCancelable(false)
            .show()
    }

    private fun checkDarkModeEnabled() {

        val action = object : ActivityObserver {
            override fun doWork() {
                val nightModeFlags = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
                darkModeEnabled = (nightModeFlags == Configuration.UI_MODE_NIGHT_YES)
            }
        }
        // verifico se possivel
        currentActivity?.let { action.doWork() }

        // agendo tarefa para que se repita sempre que uma nova activity estiver disponivel
        addActivityObserver(action)

    }

    private fun showUpdateAvailableDialog(latestVersion: Long) {
        if (latestVersion > BuildConfig.VERSION_CODE) {

            val action = object : ActivityObserver {
                override fun doWork() {
                    Vibrator.error()
                    MaterialAlertDialogBuilder(currentActivity!!).setTitle(getString(R.string.Nova_versao_disponivel)).setMessage(
                        getString(R.string.Uma_nova_vers_o_do_app_est_dispon_vel_na_loja)
                    ).setPositiveButton(getString(R.string.Ir_a_loja)) { dialog, _ ->
                        openPlayStore()
                    }.setNegativeButton(getString(R.string.Fechar)) { dialog, _ ->
                        dialog.dismiss()
                    }.setCancelable(true).show()

                    removeActivityObserver(this)
                }
            }

            // verifico se possivel
            if (currentActivity != null) action.doWork()
            else addActivityObserver(action)
        }
    }

    private fun openPlayStore() {
        val appPackageName = packageName
        try {
            val intent = Intent(
                Intent.ACTION_VIEW, Uri.parse("market://details?id=$appPackageName")
            ).addFlags(FLAG_ACTIVITY_NEW_TASK)
            intent.setPackage("com.android.vending") // Garante que a Play Store será aberta
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            // Caso o usuário não tenha a Play Store instalada, abre no navegador
            val intent = Intent(
                Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")
            ).addFlags(FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    /**
     * Define um listener que sera chamado sempre que uma instancia de activity estiver disponivel
     */
    private fun addActivityObserver(observer: ActivityObserver) {
        if (!activityObservers.contains(observer)) {
            activityObservers.add(observer)
        }
    }

    /**
     * Remove o listener, caso ele tenha sido definido
     */
    fun removeActivityObserver(observer: ActivityObserver) {
        activityObservers.remove(observer)
    }

    /**Define um listener que observa o status de convidado do ususario atual caso ele seja um convidado de outro usuario
     * Caso o listener ja tenha sido definido, nao faz nada */
    private fun observeGuestStatus() {
        if (guestsObserverListener != null) return
        Log.d("USUK", "App.observeGuestStatus: observing if local will be disconnected from host")
        guestsObserverListener = UserRepository.observeGuestStatus {
            Log.d("USUK", "App.observeGuestStatus: guest disconnected")
            startActivity(Intent(
                this, SyncStoppedActivity::class.java
            ).apply { flags + FLAG_ACTIVITY_NEW_TASK })
        }
    }

    /**
     * Habilita ou desabilita  o listener que observa se o usuario local foi desconectado
     * de um sincronismo entre contas pelo host. Ao desabilitar o listener a activity que impede o uso
     * do app, forçando a reiniciar para separar as contas nao será exibida.
     *
     * Essa função foi criada para que a activity de bloqueio nao interfira no fluxo de desconexao
     * do host, que acontece quando o usuario se desconecta do host por vontade propria atraves da tela
     * de perfil do usuario.
     *
     * @see dev.gmarques.compras.presenter.profile.BsdDisconnectAccount
     * @see dev.gmarques.compras.presenter.profile.ProfileActivity
     * */
    @OptIn(DelicateCoroutinesApi::class)
    fun toggleGuestListener(turnOn: Boolean) = GlobalScope.launch {

        if (turnOn) observeGuestStatus()
        else guestsObserverListener?.remove().also {
            Log.d(
                "USUK", "App.toggleGuestListener: not listening anymore"
            )
        }
    }

    override fun onTerminate() {
        guestsObserverListener?.remove()
        scope.cancel("App fechou")
        super.onTerminate()
    }


    interface ActivityObserver {
        fun doWork()
    }
}
