package dev.gmarques.compras.data.entidades

import android.annotation.SuppressLint
import androidx.room.ColumnInfo
import androidx.room.Entity
import com.google.gson.GsonBuilder
import dev.gmarques.compras.App

@Entity
class CategoriaEntidade() : Entidade() {

    @ColumnInfo(defaultValue = "vec_cat_0")
    var icone: String = "vec_cat_0"

    @SuppressLint("DiscouragedApi")
    fun intIcone() = App.get.resources.getIdentifier(icone, "drawable", App.get.packageName)

    fun setIcone(intIcone: Int) {
        icone = App.get.resources.getResourceEntryName(intIcone)
    }



    override fun clonar(): CategoriaEntidade {
        val g = GsonBuilder().setPrettyPrinting().create()
        return g.fromJson(g.toJson(this), CategoriaEntidade::class.java)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CategoriaEntidade

        if (id != other.id) return false
        if (ultimaAtualizacao != other.ultimaAtualizacao) return false
        if (removido != other.removido) return false
        if (nome != other.nome) return false
        if (icone != other.icone) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + ultimaAtualizacao.hashCode()
        result = 31 * result + removido.hashCode()
        result = 31 * result + nome.hashCode()
        result = 31 * result + icone.hashCode()
        return result
    }

    override fun toString(): String {
        return "nome='$nome', icone='$icone', id='$id', ultimaAtualizacao=$ultimaAtualizacao, removido=$removido"
    }


}