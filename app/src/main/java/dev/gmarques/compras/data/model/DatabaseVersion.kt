package dev.gmarques.compras.data.model

import dev.gmarques.compras.data.firestore.Firestore

data class DatabaseVersion(
    val databaseVersion: Int = Firestore.VERSION,
)