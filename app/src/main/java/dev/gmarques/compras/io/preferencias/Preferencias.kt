package dev.gmarques.compras.io.preferencias

import android.content.Context
import android.content.SharedPreferences
import dev.gmarques.compras.App

/**
 * https://developer.android.com/training/data-storage/shared-preferences?hl=pt-br#kotlin
 * */
class Preferencias {
    private val prefs: SharedPreferences =
        App.get.applicationContext.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    fun saveString(key: String, value: String) = with(prefs.edit()) {
        putString(key, value)
        apply() //async
    }

    fun getString(key: String, default: String? = null) = prefs.getString(key, default)

    @Suppress("unused")
    fun limparDados(key: String, default: String? = null): Any = with(prefs.edit()) { clear() }

    /**
     * Verifica se esse é o primeiro boot do app
     * se setFalse = true, salva um valor nas preferencias para que na proxima
     * leitura retorne false*/
    fun primeiroBoot(): Boolean {
        val valor = prefs.getBoolean("primeiro_boot", true)

        if (valor) with(prefs.edit()) {
            putBoolean("primeiro_boot", false)
            apply()
        }
        return valor
    }

    companion object {
        const val ultimaLista = "ultimaLista"
    }
}