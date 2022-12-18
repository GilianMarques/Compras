package dev.gmarques.compras.data.repositorios

import dev.gmarques.compras.data.database.RoomDb
import dev.gmarques.compras.domain.entidades.Produto
import dev.gmarques.compras.domain.entidades.ProdutoDispensa


object ProdutoDispensaRepo : BaseRepo() {

    suspend fun addOuAtualizar(produto: ProdutoDispensa) =
            RoomDb.getInstancia().produtoDispensaDao().addOuAtualizar(produto)

    @Suppress("unused")
    suspend fun getTodosProdutos() = RoomDb.getInstancia().produtoDispensaDao().getProdutos()

    suspend fun getProdutosPorNome(nome: String): List<ProdutoDispensa> =
            RoomDb.getInstancia().produtoDispensaDao().getProdutosPorNomeIniciado(nome)

    suspend fun getProdutosPorNomeExato(produto: ProdutoDispensa, limiteResultados: Int) =
            RoomDb.getInstancia().produtoDispensaDao()
                .getProdutosPorNomeExato(produto.nome, limiteResultados)

    suspend fun getProdutosDaCategoria(categoriaId: String,  limiteResultados: Int) =
            RoomDb.getInstancia().produtoDispensaDao().getProdutosDaCategoria(categoriaId,limiteResultados)
}
