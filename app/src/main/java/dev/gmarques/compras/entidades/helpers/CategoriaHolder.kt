package dev.gmarques.compras.entidades.helpers

import com.google.gson.GsonBuilder
import dev.gmarques.compras.entidades.Categoria

/**
 * Usada para disparar atualizaçoes pelo liveData do viewmodel para o RecyclerView
 * permitindo que o DiffUtils veja que houve alteraçao no objeto e atualize a interface
 */
data class CategoriaHolder(
    var categoria: Categoria,
    var itensComprados: Boolean = false,
    var selecionada: Boolean = false,
) {
    fun clonar(): CategoriaHolder {
        val g = GsonBuilder().setPrettyPrinting().create()
        return g.fromJson(g.toJson(this), CategoriaHolder::class.java)
    }


}
