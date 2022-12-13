package dev.gmarques.compras.data.repositorios

import dev.gmarques.compras.data.database.RoomDb
import dev.gmarques.compras.data.entidades.CategoriaEntidade

object CategoriaRepo : BaseRepo() {

    /**
     * Retorna a categoria do produto
     * Não retorna null, se a categoria nao existir, essa funçao retorna a categoria padrao.
     */
    suspend fun getCategoriaPorId(id: String): CategoriaEntidade =
            RoomDb.getInstancia().categoriaDao().get(id)
                ?: throw Exception("Uma categoria jamais pode ser nula, id=$id")

    suspend fun getCategoriaPorNome(nome: String): CategoriaEntidade? {

        getCategorias().forEach {
            if (it.nome == nome) return it
        }
        return null
    }

    suspend fun getCategorias() = RoomDb.getInstancia().categoriaDao().getTodas()

    /**
     * Adidiona ou atualiza o objeto recebido no banco de dados
     */
    suspend fun addAttCategoria(novaCategoria: CategoriaEntidade) {
        RoomDb.getInstancia().categoriaDao().addOuAtualizar(novaCategoria)
    }
}