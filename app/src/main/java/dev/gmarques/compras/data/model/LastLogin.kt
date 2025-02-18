package dev.gmarques.compras.data.model

data class LastLogin(
    val lastLogin: Long = System.currentTimeMillis(),
)