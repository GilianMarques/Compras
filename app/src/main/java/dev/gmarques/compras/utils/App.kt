package dev.gmarques.compras.utils

import android.app.Application
import android.content.Context

class App : Application() {

    companion object {
        private lateinit var instance: App

        /**
         * Retorna o contexto global da aplicação.
         */
        fun getContext(): Context = instance
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}
