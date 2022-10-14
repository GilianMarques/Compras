package dev.gmarques.compras.objetos

import androidx.annotation.ColorInt
import androidx.room.ColumnInfo
import androidx.room.Entity
import dev.gmarques.compras.App
import dev.gmarques.compras.R

@Entity
class Categoria : Objetos() {
    override var nome: String = ""

    @ColumnInfo(defaultValue = "vec_cat_0")
    var icone: String = "vec_cat_0"

    @ColorInt
    var cor: Int = 0


    companion object {
        @JvmStatic
        fun intIcone(icone: String) =
            App.get.resources.getIdentifier(icone, "drawable", App.get.packageName)

        fun stringIcone(icone: Int): String = App.get.resources.getResourceEntryName(icone)

        val SEM_CATEGORIA = Categoria().also {
            it.nome = App.get.applicationContext.getString(R.string.sem_categoria)
            it.cor = App.get.applicationContext.getColor(R.color.colorPrimary)
            it.id = "999"
        }
    }

}