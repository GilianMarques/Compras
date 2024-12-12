package dev.gmarques.compras.data.data.model

data class ListItem(
    val shopListId: String,
    val id: String,
    var name: String,
    var price: Double,
    var info: String,
    var obs: String,
)
