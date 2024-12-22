package dev.gmarques.compras.utils

/**
 * A generic class that holds a value with its loading status.
 * @param <T>
 */
sealed class RequestResult<out T : Any> {

    data class Success<out T : Any>(val data: T) : RequestResult<T>()
    data class Error(val exception: Exception) : RequestResult<Nothing>()

    override fun toString(): String {
        return when (this) {
            is Success<*> -> "Success[data=$data]"
            is Error -> "Error[exception=$exception]"
        }
    }
}