package dev.gmarques.compras.io.repositorios

import dev.gmarques.compras.io.database.RoomDb
import dev.gmarques.compras.objetos.Item
import kotlinx.coroutines.launch


object ItemRepo : BaseRepo() {

    fun addOuAtualizar(item: Item) =
        repoScope.launch { RoomDb.getInstancia().itemDao().addOuAtualizar(item) }

    suspend fun getItens() = RoomDb.getInstancia().itemDao().getTodos()

    suspend fun getItens(nome: String): List<Item> =
        RoomDb.getInstancia().itemDao().getTodosPorNome(nome)

    suspend fun getItens(nome: String, listaId: String) =
        RoomDb.getInstancia().itemDao().getTodosNaListaPorNome(nome, listaId)


}
