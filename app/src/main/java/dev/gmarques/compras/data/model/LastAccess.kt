package dev.gmarques.compras.data.model

data class LastAccess(
    val lastAccess: Long = System.currentTimeMillis(),
)