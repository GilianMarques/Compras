package dev.gmarques.compras

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.util.Log
import com.google.android.material.color.DynamicColors
import dev.gmarques.compras.data.repository.UserRepository
import dev.gmarques.compras.domain.utils.ListenerRegister
import dev.gmarques.compras.ui.sync_stopped.SyncStoppedActivity
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.system.exitProcess

class App : Application() {

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
        instance = this

    }

    /**Define um listener que observa o status de convidado do ususario atual caso ele seja um convidado de outro usuario
     * Caso o listener ja tenha sido definido, nao faz nada */
    private fun observeGuestStatus() {
        if (guestsObserverListener != null) return
        Log.d("USUK", "App.observeGuestStatus: observing if local will be disconnected from host")
        guestsObserverListener = UserRepository.observeGuestStatus {
            Log.d("USUK", "App.observeGuestStatus: guest disconnected")
            startActivity(
                Intent(
                    this,
                    SyncStoppedActivity::class.java
                ).apply { flags += FLAG_ACTIVITY_NEW_TASK })
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
     * @see dev.gmarques.compras.ui.profile.BsdDisconnectAccount
     * @see dev.gmarques.compras.ui.profile.ProfileActivity
     * */
    @OptIn(DelicateCoroutinesApi::class)
    fun toggleGuestListener(turnOn: Boolean) = GlobalScope.launch {

        if (turnOn) observeGuestStatus()
        else guestsObserverListener?.remove().also {
            Log.d(
                "USUK",
                "App.toggleGuestListener: not listening anymore"
            )
        }
    }

    override fun onTerminate() {
        guestsObserverListener?.remove()
        super.onTerminate()
    }

}
