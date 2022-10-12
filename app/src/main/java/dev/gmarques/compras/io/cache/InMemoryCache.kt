package dev.gmarques.compras.io.cache

import android.util.Log
import android.util.LruCache
import dev.gmarques.compras.objetos.Categoria

/**
 * Serve para manter um cache em memoria das categorias do app
 * nao é interessante manter instancias de categorias dentro dos objetos
 * item, nem ficar lendo o db toda vez que precisar acessar uma. Usar alguma funçao
 * de cache do Room tambem nao parece ideal a menos que tenha um jeito de fazer cache apenas
 * do objeto categoria mas nao sei se é possivel por hora
 * */
// TODO: é possivel fazer cache apenas de categorias no Room ?
class InMemoryCache {
    object Singleton { // singleton

        private val maxMemoriaEmKb = (Runtime.getRuntime().maxMemory() / 1024).toInt()
        private val cacheSize = maxMemoriaEmKb / 30
        private val cache = LruCache<String, Categoria>(cacheSize)

        init {
            Log.d("USUK",
                "Singleton.".plus("maxMemoriaEmKb: $maxMemoriaEmKb, cacheSize: $cacheSize  "))
        }

        fun getCategoria(categoriaId: String): Categoria? = cache.get(categoriaId)

        fun saveCategoria(categoriaId: String, it: Categoria) = cache.put(categoriaId, it)

    }
}