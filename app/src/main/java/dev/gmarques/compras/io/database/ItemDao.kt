package dev.gmarques.compras.io.database

import androidx.room.Dao
import androidx.room.Query
import dev.gmarques.compras.objetos.Item

@Dao
abstract class ItemDao : BaseDao<Item>() {

    @Query("SELECT * FROM item WHERE removido = 0 ORDER BY nome ASC")
    abstract suspend fun getItens(): List<Item>

    @Query("SELECT * FROM item WHERE listaId = :idLista AND removido = 0 ORDER BY nome ASC")
    abstract suspend fun getItens(idLista: String): List<Item>

    /**
     * retorna todos os itens cujo nome começa com o argumento recebido
     */
    @Query("SELECT * FROM item WHERE nome LIKE :nome||'%'  AND removido = 0 ORDER BY nome ASC")
    abstract suspend fun getItensPorNomeIniciado(nome: String): List<Item>

    /**
     * https://www.sqlitetutorial.net/sqlite-limit/
     *
     * retorna todos os itens cujo nome começa é igual ao argumento recebido ordenados pela sua ultima
     * atualizaçao do mais recente pro mais antigo
     * LIMIT: limita a quantidade de resultados da busca
     */
    @Query("SELECT * FROM item WHERE nome LIKE :nome AND removido = 0 ORDER BY ultimaAtualizacao DESC LIMIT :limiteResultados" )
    abstract suspend fun getItensPorNomeExato(nome: String, limiteResultados: Int): List<Item>
    // TODO: testar essa funçao (acima) deve retornar apenas nomes identicos e ordenados de acordo com a descrição

    @Query("SELECT * FROM item WHERE listaId = :listaId AND nome = :nome  AND removido = 0 ORDER BY nome ASC")
    abstract suspend fun getItensNaListaPorNome(nome: String, listaId: String): List<Item>

    @Query("SELECT * FROM item WHERE listaId = :listaId AND categoriaId = :categoriaId AND removido = 0 ORDER BY nome ASC")
    abstract suspend fun getItens(listaId: String, categoriaId: String?): List<Item>


}

//  continuar aqui https://developer.android.com/training/data-storage/room?hl=pt-br#kotlin