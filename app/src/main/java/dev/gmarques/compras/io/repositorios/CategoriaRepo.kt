package dev.gmarques.compras.io.repositorios

import dev.gmarques.compras.io.database.RoomDb
import dev.gmarques.compras.entidades.Categoria

object CategoriaRepo : BaseRepo() {

    /**
     * Retorna a categoria do produto
     * Não retorna null, se a categoria nao existir, essa funçao retorna a categoria padrao.
     */
    suspend fun getCategoriaPorId(id: String): Categoria =
            RoomDb.getInstancia().categoriaDao().get(id)
                ?: throw Exception("Uma categoria jamais pode ser nula, id=$id")

    suspend fun getCategoriaPorNome(nome: String): Categoria? {

        getCategorias().forEach {
            if (it.nome == nome) return it
        }
        return null
    }

    suspend fun getCategorias() = RoomDb.getInstancia().categoriaDao().getTodas()

    /**
     * Adidiona ou atualiza o objeto recebido no banco de dados
     */
    suspend fun addAttCategoria(novaCategoria: Categoria) {
        RoomDb.getInstancia().categoriaDao().addOuAtualizar(novaCategoria)
    }
}