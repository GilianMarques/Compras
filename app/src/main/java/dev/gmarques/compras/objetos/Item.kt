package dev.gmarques.compras.objetos

import androidx.room.Entity
import dev.gmarques.compras.Extensions.Companion.emMoeda

@Entity
class Item : Objetos() {

    var preco: Float = 0.1f
    var qtd: Int = 1
    var detalhes: String? = null
    var comprado: Boolean = false
    var categoriaId: String = ""
    var listaId: String = ""

    fun emMoeda(): String = preco.emMoeda()



}