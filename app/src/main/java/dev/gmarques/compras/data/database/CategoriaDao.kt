package dev.gmarques.compras.data.database

import androidx.room.Dao
import androidx.room.Query
import dev.gmarques.compras.data.entidades.CategoriaEntidade

@Dao
abstract class CategoriaDao : BaseDao<CategoriaEntidade>() {

    @Query("SELECT * FROM categoriaentidade  WHERE removido = 0")
    abstract suspend fun getTodas(): List<CategoriaEntidade>

    @Query("SELECT * FROM categoriaentidade WHERE id = :id  AND removido = 0 LIMIT 1")
    abstract suspend fun get(id: String): CategoriaEntidade?

}

