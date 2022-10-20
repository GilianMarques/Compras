package dev.gmarques.compras.io.repositorios

import dev.gmarques.compras.io.database.RoomDb
import dev.gmarques.compras.objetos.Item
import kotlinx.coroutines.launch


object ItemRepo : BaseRepo() {

    fun addOuAtualizar(item: Item) =
        repoScope.launch { RoomDb.getInstancia().itemDao().addOuAtualizar(item) }

    @Suppress("unused")
    suspend fun getTodosItens() = RoomDb.getInstancia().itemDao().getItens()

    suspend fun getItensPorNome(nome: String): List<Item> =
        RoomDb.getInstancia().itemDao().getItensPorNome(nome)

    suspend fun getItensNaListaPorNome(nome: String, listaId: String) =
        RoomDb.getInstancia().itemDao().getItensNaListaPorNome(nome, listaId)

    suspend fun getItensNaLista(listaId: String) = RoomDb.getInstancia().itemDao().getItens(listaId)

    suspend fun getItensNaListaPorCategoria(listaId: String, categoriaId: String?) =
        RoomDb.getInstancia().itemDao().getItens(listaId, categoriaId)

}
