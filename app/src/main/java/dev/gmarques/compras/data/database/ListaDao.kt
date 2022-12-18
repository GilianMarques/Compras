package dev.gmarques.compras.data.database

import androidx.room.Dao
import androidx.room.Query
import dev.gmarques.compras.domain.entidades.Lista

@Dao
abstract class ListaDao : BaseDao<Lista>() {

    @Query("SELECT * FROM lista WHERE removido = 0")
    abstract suspend fun getListas(): List<Lista>

    @Query("SELECT * FROM lista WHERE id = :id AND removido = 0")
    abstract suspend fun getListas(id: String): Lista

}

