package dev.gmarques.compras.data.database

import androidx.room.*

@Dao
/**
 * abstraçao para evitar a repetiçao de funçoes que sao padrao em todos os DAOS (até entao)
 * sao essas: addOuAtualizar, atualizar e remover*/
abstract class BaseDao<T> {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun addOuAtualizar(vararg obj: T)

    @Update
    abstract suspend fun atualizar(vararg obj: T): Int // linhas atualizadas da tabela

    @Delete
    abstract suspend fun remover(vararg obj: T): Int // linhas removidas da tabela

}