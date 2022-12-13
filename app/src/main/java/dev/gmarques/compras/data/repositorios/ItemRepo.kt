package dev.gmarques.compras.data.repositorios

import dev.gmarques.compras.data.database.RoomDb
import dev.gmarques.compras.data.entidades.ProdutoEntidade


object ItemRepo : BaseRepo() {

    suspend fun addOuAtualizar(produtoEntidade: ProdutoEntidade) =
            RoomDb.getInstancia().produtoDao().addOuAtualizar(produtoEntidade)

    @Suppress("unused")
    suspend fun getTodosItens() = RoomDb.getInstancia().produtoDao().getProdutos()

    suspend fun getItensPorNome(nome: String): List<ProdutoEntidade> =
            RoomDb.getInstancia().produtoDao().getProdutosPorNomeIniciado(nome)

    suspend fun getItensNaListaPorNome(nome: String, listaId: String) =
            RoomDb.getInstancia().produtoDao().getProdutosNaListaPorNome(nome, listaId)

    suspend fun getItensNaLista(listaId: String) =
            RoomDb.getInstancia().produtoDao().getProdutos(listaId)

    suspend fun getItensNaListaPorCategoria(
        listaId: String,
        categoriaId: String?,
    ) = ArrayList(RoomDb.getInstancia().produtoDao().getProdutos(listaId, categoriaId))

    suspend fun getItensPorNomeExato(produtoEntidade: ProdutoEntidade, limiteResultados: Int) =
            RoomDb.getInstancia().produtoDao()
                .getProdutosPorNomeExato(produtoEntidade.nome, limiteResultados)

    suspend fun getItensDaCategoria(categoriaId: String,  limiteResultados: Int) =
            RoomDb.getInstancia().produtoDao().getProdutosDaCategoria(categoriaId,limiteResultados)
}
