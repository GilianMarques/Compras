package dev.gmarques.compras.data.database

import androidx.room.Dao
import androidx.room.Query
import dev.gmarques.compras.domain.entidades.Produto
import dev.gmarques.compras.domain.entidades.ProdutoDispensa

@Dao
abstract class ProdutoDispensaDao : BaseDao<ProdutoDispensa>() {

    @Query("SELECT * FROM produtodispensa WHERE removido = 0 ORDER BY nome ASC")
    abstract suspend fun getProdutos(): List<ProdutoDispensa>

    /**
     * retorna todos os itens cujo nome começa com o argumento recebido
     */
    @Query("SELECT * FROM produtodispensa WHERE nome LIKE :nome||'%'  AND removido = 0 ORDER BY nome ASC")
    abstract suspend fun getProdutosPorNomeIniciado(nome: String): List<ProdutoDispensa>

    /**
     * https://www.sqlitetutorial.net/sqlite-limit/
     *
     * retorna todos os itens cujo nome começa é igual ao argumento recebido ordenados pela sua ultima
     * atualizaçao do mais recente pro mais antigo
     * LIMIT: limita a quantidade de resultados da busca
     */
    @Query("SELECT * FROM produtodispensa WHERE nome LIKE :nome AND removido = 0 ORDER BY ultimaAtualizacao DESC LIMIT :limiteResultados")
    abstract suspend fun getProdutosPorNomeExato(nome: String, limiteResultados: Int): List<ProdutoDispensa>

    @Query("SELECT * FROM produtodispensa WHERE categoriaId = :categoriaId AND removido = 0 ORDER BY nome ASC  LIMIT :limiteResultados")
    abstract suspend fun getProdutosDaCategoria(
        categoriaId: String,
        limiteResultados: Int,
    ): List<ProdutoDispensa>
}

//  continuar aqui https://developer.android.com/training/data-storage/room?hl=pt-br#kotlin