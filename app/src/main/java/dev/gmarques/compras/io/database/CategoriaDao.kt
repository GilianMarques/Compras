package dev.gmarques.compras.io.database

import androidx.room.Dao
import androidx.room.Query
import dev.gmarques.compras.objetos.Categoria

@Dao
abstract class CategoriaDao : BaseDao<Categoria>() {

    @Query("SELECT * FROM categoria  WHERE removido = 0")
    abstract suspend  fun getTodas(): List<Categoria>

    @Query("SELECT * FROM categoria WHERE id = :id  AND removido = 0")
    abstract suspend  fun get(id: String): Categoria

}

