package dev.gmarques.compras.data.database

import androidx.room.Dao
import androidx.room.Query
import dev.gmarques.compras.data.entidades.ProdutoEntidade

@Dao
abstract class ProdutoDao : BaseDao<ProdutoEntidade>() {

    @Query("SELECT * FROM produtoentidade WHERE removido = 0 ORDER BY nome ASC")
    abstract suspend fun getProdutos(): List<ProdutoEntidade>

    @Query("SELECT * FROM produtoentidade WHERE listaId = :idLista AND removido = 0 ORDER BY nome ASC")
    abstract suspend fun getProdutos(idLista: String): List<ProdutoEntidade>

    /**
     * retorna todos os itens cujo nome começa com o argumento recebido
     */
    @Query("SELECT * FROM produtoentidade WHERE nome LIKE :nome||'%'  AND removido = 0 ORDER BY nome ASC")
    abstract suspend fun getProdutosPorNomeIniciado(nome: String): List<ProdutoEntidade>

    /**
     * https://www.sqlitetutorial.net/sqlite-limit/
     *
     * retorna todos os itens cujo nome começa é igual ao argumento recebido ordenados pela sua ultima
     * atualizaçao do mais recente pro mais antigo
     * LIMIT: limita a quantidade de resultados da busca
     */
    @Query("SELECT * FROM produtoentidade WHERE nome LIKE :nome AND removido = 0 ORDER BY ultimaAtualizacao DESC LIMIT :limiteResultados")
    abstract suspend fun getProdutosPorNomeExato(nome: String, limiteResultados: Int): List<ProdutoEntidade>

    @Query("SELECT * FROM produtoentidade WHERE listaId = :listaId AND nome = :nome  AND removido = 0 ORDER BY nome ASC")
    abstract suspend fun getProdutosNaListaPorNome(nome: String, listaId: String): List<ProdutoEntidade>

    @Query("SELECT * FROM produtoentidade WHERE listaId = :listaId AND categoriaId = :categoriaId AND removido = 0 ORDER BY nome ASC")
    abstract suspend fun getProdutos(listaId: String, categoriaId: String?): List<ProdutoEntidade>

    @Query("SELECT * FROM produtoentidade WHERE categoriaId = :categoriaId AND removido = 0 ORDER BY nome ASC  LIMIT :limiteResultados")
    abstract suspend fun getProdutosDaCategoria(
        categoriaId: String,
        limiteResultados: Int,
    ): List<ProdutoEntidade>
}

//  continuar aqui https://developer.android.com/training/data-storage/room?hl=pt-br#kotlin