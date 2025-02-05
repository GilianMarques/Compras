package dev.gmarques.compras.data.model

data class SyncRequest(
    val name: String,
    val email: String,
    val photoUrl: String,
) {
    constructor():this("","","")
}

