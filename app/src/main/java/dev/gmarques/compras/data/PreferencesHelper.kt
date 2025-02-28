package dev.gmarques.compras.data

import android.content.Context
import android.content.SharedPreferences
import dev.gmarques.compras.App
import dev.gmarques.compras.BuildConfig
import dev.gmarques.compras.domain.SortCriteria


/**
 * Autor: Gilian
 * Data de Criação: 30/12/2024
 * Classe utilitária para facilitar a leitura e escrita de valores em SharedPreferences.
 *
 */
class PreferencesHelper {

    private val sharedPreferences: SharedPreferences =
        App.getContext().getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE)

    private val editor: SharedPreferences.Editor = sharedPreferences.edit()

    /**
     * Salva um valor no SharedPreferences.
     *
     * @param key Chave usada para armazenar o valor.
     * @param value Valor a ser armazenado. Pode ser String, Int, Boolean, Float ou Long.
     */
    fun saveValue(key: String, value: Any) {
        when (value) {
            is String -> editor.putString(key, value)
            is Int -> editor.putInt(key, value)
            is Boolean -> editor.putBoolean(key, value)
            is Float -> editor.putFloat(key, value)
            is Long -> editor.putLong(key, value)
            else -> throw IllegalArgumentException("Tipo não suportado: ${value::class.java}")
        }
        editor.apply()
    }

    /**
     * Obtém um valor do SharedPreferences.
     *
     * @param key Chave usada para recuperar o valor.
     * @param defaultValue Valor padrão retornado caso a chave não exista.
     * @return O valor associado à chave ou o valor padrão se não encontrado.
     */
    fun <T : Any> getValue(key: String, defaultValue: T): T {
        return when (defaultValue) {
            is String -> sharedPreferences.getString(key, defaultValue)
            is Int -> sharedPreferences.getInt(key, defaultValue)
            is Boolean -> sharedPreferences.getBoolean(key, defaultValue)
            is Float -> sharedPreferences.getFloat(key, defaultValue)
            is Long -> sharedPreferences.getLong(key, defaultValue)
            else -> throw IllegalArgumentException("Tipo não suportado: ${defaultValue::class.java}")
        } as T
    }


    /**
     * Remove um valor do SharedPreferences.
     *
     * @param key Chave associada ao valor a ser removido.
     */
    fun removeValue(key: String) {
        editor.remove(key).apply()
    }

    /**
     * Limpa todos os valores armazenados no SharedPreferences.
     */
    fun clearAll() {
        editor.clear().apply()
    }

    class PrefsKeys {
        companion object {
            const val PRODUCTION_DATABASE = "use_production_db_on_debug"
            const val SORT_CRITERIA = "sort_criteria"
            const val SORT_ASCENDING = "sort_ascending"
            const val BOUGHT_PRODUCTS_AT_END = "bought_products_at_end"
            const val LAST_MARKET_USED = "last_market_used"
        }
    }

    class PrefsDefaultValue {
        companion object {
            val SORT_CRITERIA = SortCriteria.CREATION_DATE
            const val SORT_ASCENDING = false
            const val BOUGHT_PRODUCTS_AT_END = true
        }

    }
}
