package dev.gmarques.compras.presenter.entidades

import com.google.gson.GsonBuilder
import dev.gmarques.compras.domain.entidades.Categoria

/**
 * Usada para disparar atualizaçoes pelo liveData do viewmodel para o RecyclerView
 * permitindo que o DiffUtils veja que houve alteraçao no objeto e atualize a interface
 */
data class CategoriaUi(
    var categoria: Categoria,
    var itensComprados: Boolean = false,
    var selecionada: Boolean = false,
) {
    fun clonar(): CategoriaUi {
        val g = GsonBuilder().setPrettyPrinting().create()
        return g.fromJson(g.toJson(this), CategoriaUi::class.java)
    }


}
