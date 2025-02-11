package dev.gmarques.compras.data.model

data class SyncAccount(
    val name: String,
    val email: String,
    val photoUrl: String,
    val accepted: Boolean = false,
) {
    constructor() : this("", "", "", false)

}

