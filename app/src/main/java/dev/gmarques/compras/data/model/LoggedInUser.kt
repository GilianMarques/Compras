package dev.gmarques.compras.data.model

/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */
data class LoggedInUser(
    val name: String,
    val email: String,
)