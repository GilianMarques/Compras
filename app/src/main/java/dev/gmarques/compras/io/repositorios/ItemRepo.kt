package dev.gmarques.compras.io.repositorios

import dev.gmarques.compras.io.database.RoomDb
import dev.gmarques.compras.entidades.Produto


object ItemRepo : BaseRepo() {

    suspend fun addOuAtualizar(produto: Produto) = RoomDb.getInstancia().produtoDao().addOuAtualizar(produto)

    @Suppress("unused")
    suspend fun getTodosItens() = RoomDb.getInstancia().produtoDao().getItens()

    suspend fun getItensPorNome(nome: String): List<Produto> =
        RoomDb.getInstancia().produtoDao().getItensPorNomeIniciado(nome)

    suspend fun getItensNaListaPorNome(nome: String, listaId: String) =
        RoomDb.getInstancia().produtoDao().getItensNaListaPorNome(nome, listaId)

    suspend fun getItensNaLista(listaId: String) = RoomDb.getInstancia().produtoDao().getItens(listaId)

    suspend fun getItensNaListaPorCategoria(
        listaId: String,
        categoriaId: String?,
    ) = ArrayList(RoomDb.getInstancia().produtoDao().getItens(listaId, categoriaId))

    suspend fun getItensPorNomeExato(produto: Produto, limiteResultados: Int) =
        RoomDb.getInstancia().produtoDao().getItensPorNomeExato(produto.nome, limiteResultados)

}
