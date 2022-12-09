package dev.gmarques.compras.io.repositorios

import dev.gmarques.compras.io.database.RoomDb
import dev.gmarques.compras.entidades.Categoria as Categoria1

object CategoriaRepo : BaseRepo() {

    /**
     * Retorna a categoria do produto
     * Não retorna null, se a categoria nao existir, essa funçao retorna a categoria padrao.
     */
    suspend fun getCategoriaPorId(id: String): Categoria1 =
            RoomDb.getInstancia().categoriaDao().get(id) ?: Categoria1.SEM_CATEGORIA

    suspend fun getCategoriaPorNome(nome: String): Categoria1? {

        getCategorias().forEach {
            if (it.nome == nome) return it
        }
        return null
    }

    suspend fun getCategorias() = RoomDb.getInstancia().categoriaDao().getTodas()

    /**
     * Adidiona ou atualiza o objeto recebido no banco de dados
     */
    suspend fun addCategoria(novaCategoria: Categoria1) {
        RoomDb.getInstancia().categoriaDao().addOuAtualizar(novaCategoria)
    }
}