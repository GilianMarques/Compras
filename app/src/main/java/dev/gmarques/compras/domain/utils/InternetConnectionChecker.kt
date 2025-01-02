package dev.gmarques.compras.domain.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/**
 * Verifica a conectividade com a internet de forma assíncrona.
 */
class InternetConnectionChecker(private val timeoutMillis: Long = 10_000L) {

    /**
     * Verifica a conectividade com a internet.
     * @param onResult Callback que retorna o resultado da verificação.
     */
    fun checkConnection(onResult: (Boolean) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {

            val isConnected = withTimeoutOrDefault(timeoutMillis, defaultValue = false) {
                try {
                    val urlConnection =  URL("https://clients3.google.com/generate_204").openConnection() as HttpURLConnection
                    urlConnection.responseCode == 204 && urlConnection.contentLength == 0
                } catch (e: IOException) {
                    false
                }
            }

            withContext(Dispatchers.Main) {
                onResult(isConnected)
            }
        }
    }

    /**
     * Executa um bloco de código com um limite de tempo, retornando um valor padrão em caso de timeout.
     */
    private suspend fun <T> withTimeoutOrDefault(
        timeoutMillis: Long,
        defaultValue: T,
        block: suspend () -> T,
    ): T {
        return try {
            withTimeout(timeoutMillis) {
                block()
            }
        } catch (e: Exception) {
            defaultValue
        }
    }
}
