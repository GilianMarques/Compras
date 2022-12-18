package dev.gmarques.compras.data.database

import androidx.room.Dao
import androidx.room.Query
import dev.gmarques.compras.domain.entidades.Produto

@Dao
abstract class ProdutoDao : BaseDao<Produto>() {

    @Query("SELECT * FROM produto WHERE removido = 0 ORDER BY nome ASC")
    abstract suspend fun getProdutos(): List<Produto>

    @Query("SELECT * FROM produto WHERE listaId = :idLista AND removido = 0 ORDER BY nome ASC")
    abstract suspend fun getProdutos(idLista: String): List<Produto>

    /**
     * retorna todos os itens cujo nome começa com o argumento recebido
     */
    @Query("SELECT * FROM produto WHERE nome LIKE :nome||'%'  AND removido = 0 ORDER BY nome ASC")
    abstract suspend fun getProdutosPorNomeIniciado(nome: String): List<Produto>

    /**
     * https://www.sqlitetutorial.net/sqlite-limit/
     *
     * retorna todos os itens cujo nome começa é igual ao argumento recebido ordenados pela sua ultima
     * atualizaçao do mais recente pro mais antigo
     * LIMIT: limita a quantidade de resultados da busca
     */
    @Query("SELECT * FROM produto WHERE nome LIKE :nome AND removido = 0 ORDER BY ultimaAtualizacao DESC LIMIT :limiteResultados")
    abstract suspend fun getProdutosPorNomeExato(nome: String, limiteResultados: Int): List<Produto>

    @Query("SELECT * FROM produto WHERE listaId = :listaId AND nome = :nome  AND removido = 0 ORDER BY nome ASC")
    abstract suspend fun getProdutosNaListaPorNome(nome: String, listaId: String): List<Produto>

    @Query("SELECT * FROM produto WHERE listaId = :listaId AND categoriaId = :categoriaId AND removido = 0 ORDER BY nome ASC")
    abstract suspend fun getProdutos(listaId: String, categoriaId: String?): List<Produto>

    @Query("SELECT * FROM produto WHERE categoriaId = :categoriaId AND removido = 0 ORDER BY nome ASC  LIMIT :limiteResultados")
    abstract suspend fun getProdutosDaCategoria(
        categoriaId: String,
        limiteResultados: Int,
    ): List<Produto>
}

//  continuar aqui https://developer.android.com/training/data-storage/room?hl=pt-br#kotlin