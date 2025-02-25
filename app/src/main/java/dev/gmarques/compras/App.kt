package dev.gmarques.compras

import android.app.Activity
import android.app.Application
import android.content.Context
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.system.exitProcess

class App : Application() {

    companion object {
        private lateinit var instance: App

        /**
         * Retorna o contexto global da aplicação.
         */
        fun getContext(): Context = instance

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
}
