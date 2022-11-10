package dev.gmarques.compras.io.repositorios

import dev.gmarques.compras.io.database.RoomDb
import dev.gmarques.compras.objetos.Item


object ItemRepo : BaseRepo() {

    suspend fun addOuAtualizar(item: Item) = RoomDb.getInstancia().itemDao().addOuAtualizar(item)

    @Suppress("unused")
    suspend fun getTodosItens() = RoomDb.getInstancia().itemDao().getItens()

    suspend fun getItensPorNome(nome: String): List<Item> =
        RoomDb.getInstancia().itemDao().getItensPorNomeIniciado(nome)

    suspend fun getItensNaListaPorNome(nome: String, listaId: String) =
        RoomDb.getInstancia().itemDao().getItensNaListaPorNome(nome, listaId)

    suspend fun getItensNaLista(listaId: String) = RoomDb.getInstancia().itemDao().getItens(listaId)

    suspend fun getItensNaListaPorCategoria(listaId: String, categoriaId: String?) =
        RoomDb.getInstancia().itemDao().getItens(listaId, categoriaId)

    suspend fun getItensPorNomeExato(item: Item, limiteResultados: Int) =
        RoomDb.getInstancia().itemDao().getItensPorNomeExato(item.nome,limiteResultados)

}
