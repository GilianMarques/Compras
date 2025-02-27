package dev.gmarques.compras.data.model

@Suppress("unused")
data class SyncAccount(
    val name: String,
    val email: String,
    val photoUrl: String,
    val mergeData: Boolean = false,
    val accepted: Boolean = false,
) {
    /**Necessário para uso com o firebase*/
    constructor() : this("", "", "")
}

