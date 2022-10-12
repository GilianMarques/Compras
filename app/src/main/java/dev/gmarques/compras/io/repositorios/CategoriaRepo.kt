package dev.gmarques.compras.io.repositorios

import dev.gmarques.compras.io.cache.InMemoryCache
import dev.gmarques.compras.io.database.RoomDb
import dev.gmarques.compras.objetos.Categoria
import dev.gmarques.compras.objetos.Item
import kotlinx.coroutines.runBlocking

object CategoriaRepo : BaseRepo() {

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
}