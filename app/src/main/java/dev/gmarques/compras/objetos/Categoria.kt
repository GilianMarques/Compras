package dev.gmarques.compras.objetos

import androidx.annotation.ColorInt
import androidx.room.Entity
import dev.gmarques.compras.App
import dev.gmarques.compras.R

@Entity
class Categoria : Objetos() {
    override var nome: String = ""

    @ColorInt
    var cor: Int = 0

    companion object {
        val SEM_CATEGORIA = Categoria().also {
            it.nome = App.get.applicationContext.getString(R.string.sem_categoria)
            it.cor = App.get.applicationContext.getColor(R.color.colorPrimary)
            it.id = "999"
        }
    }

}