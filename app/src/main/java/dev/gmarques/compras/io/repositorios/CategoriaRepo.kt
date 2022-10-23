package dev.gmarques.compras.io.repositorios

import dev.gmarques.compras.io.cache.InMemoryCache
import dev.gmarques.compras.io.database.RoomDb
import dev.gmarques.compras.objetos.Categoria
import dev.gmarques.compras.objetos.Item
import kotlinx.coroutines.runBlocking

object CategoriaRepo : BaseRepo() {

    /**
     * Retorna a categoria do item que pode estar no DB ou em cache na memoria
     * Não retorna null, se a categoria nao existir, essa funçao retorna a categoria padrao.
     * */
    fun getCategoria(item: Item): Categoria {
        var c = InMemoryCache.Singleton.getCategoria(item.categoriaId)

        if (c == null) runBlocking {
            c = RoomDb.getInstancia().categoriaDao().get(item.categoriaId)

            // categoria padrao caso a original tenha sido removida pelo usuario
            @Suppress("KotlinConstantConditions")
            if (c == null) c = Categoria.SEM_CATEGORIA

            InMemoryCache.Singleton.saveCategoria(item.categoriaId, c!!)
        }

        return c!!
    }

    suspend fun getCategorias() = RoomDb.getInstancia().categoriaDao().getTodas().onEach {
        InMemoryCache.Singleton.saveCategoria(it.id, it)
    }

}