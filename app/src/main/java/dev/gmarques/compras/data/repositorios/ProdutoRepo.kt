package dev.gmarques.compras.data.repositorios

import dev.gmarques.compras.data.database.RoomDb
import dev.gmarques.compras.domain.entidades.Produto


object ProdutoRepo : BaseRepo() {

    suspend fun addOuAtualizar(produto: Produto) =
            RoomDb.getInstancia().produtoDao().addOuAtualizar(produto)

    suspend fun getProdutosNaListaPorNome(nome: String, listaId: String) =
            RoomDb.getInstancia().produtoDao().getProdutosNaListaPorNome(nome, listaId)

    suspend fun getProdutosNaLista(listaId: String) =
            RoomDb.getInstancia().produtoDao().getProdutos(listaId)

    suspend fun getProdutosNaListaPorCategoria(
        listaId: String,
        categoriaId: String?,
    ) = ArrayList(RoomDb.getInstancia().produtoDao().getProdutos(listaId, categoriaId))

    suspend fun getProdutosPorNomeExato(produto: Produto, limiteResultados: Int) =
            RoomDb.getInstancia().produtoDao()
                .getProdutosPorNomeExato(produto.nome, limiteResultados)

    suspend fun getProdutosDaCategoria(categoriaId: String, limiteResultados: Int) =
            RoomDb.getInstancia().produtoDao().getProdutosDaCategoria(categoriaId,limiteResultados)
}
