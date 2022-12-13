package dev.gmarques.compras.data.database

import androidx.room.Dao
import androidx.room.Query
import dev.gmarques.compras.data.entidades.ListaEntidade

@Dao
abstract class ListaDao : BaseDao<ListaEntidade>() {

    @Query("SELECT * FROM listaentidade WHERE removido = 0")
    abstract suspend fun getListas(): List<ListaEntidade>

    @Query("SELECT * FROM listaentidade WHERE id = :id AND removido = 0")
    abstract suspend fun getListas(id: String): ListaEntidade

}

