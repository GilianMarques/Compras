package dev.gmarques.compras.ui

import androidx.appcompat.app.AppCompatActivity
import dev.gmarques.compras.App
import dev.gmarques.compras.ui.splash.SplashActivity

/**
 * Autor: Gilian
 * Data de Criação: 23/03/2025
 */
abstract class MyActivity : AppCompatActivity() {

    override fun onResume() {

        // splash nao deve ser setada como activity pois se fecha sozinha e por isso, pode ocultar alertas ao usuario
        if (this.localClassName != SplashActivity::class.qualifiedName) {
            App.getContext().currentActivity = this
        }
        super.onResume()
    }

    /**
     * Remove a instancia de activity quando ela sair da tela pra evitar memory leaks
     * caso outra activity ja tenha sido setada como atual quando o onPause desta for chamado
     * a função nao faz nada.
     */
    override fun onPause() {
        val app = App.getContext()
        if (app.currentActivity == this) app.currentActivity = null
        super.onPause()
    }
}