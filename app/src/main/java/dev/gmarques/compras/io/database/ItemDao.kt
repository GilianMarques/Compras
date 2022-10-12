package dev.gmarques.compras.io.database

import androidx.room.Dao
import androidx.room.Query
import dev.gmarques.compras.objetos.Item

@Dao
abstract class ItemDao : BaseDao<Item>() {

    @Query("SELECT * FROM item")
    abstract suspend fun getTodos(): List<Item>

    @Query("SELECT * FROM item WHERE listaId = :idLista")
    abstract suspend fun getTodos(idLista: String): List<Item>

    /**
     * retorna todos os itens cujo nome começa com o argumento recebido
     */
    @Query("SELECT * FROM item WHERE nome LIKE :nome||'%'")
    abstract suspend fun getTodosPorNome(nome: String): List<Item>

    @Query("SELECT * FROM item WHERE listaId = :listaId AND nome = :nome")
    abstract suspend fun getTodosNaListaPorNome(nome: String, listaId: String): List<Item>


}

// TODO: continuar aqui https://developer.android.com/training/data-storage/room?hl=pt-br#kotlin 