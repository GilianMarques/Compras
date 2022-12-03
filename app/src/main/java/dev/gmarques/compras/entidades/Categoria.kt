package dev.gmarques.compras.entidades

import android.annotation.SuppressLint
import androidx.room.ColumnInfo
import androidx.room.Entity
import com.google.gson.GsonBuilder
import dev.gmarques.compras.App
import dev.gmarques.compras.R

@Entity
class Categoria(
    @ColumnInfo(defaultValue = "vec_cat_0")
    var icone: String = "vec_cat_0",
) : Entidade() {

    companion object {
        @SuppressLint("DiscouragedApi")
        fun intIcone(icone: String) =
            App.get.resources.getIdentifier(icone, "drawable", App.get.packageName)

        fun stringIcone(icone: Int): String = App.get.resources.getResourceEntryName(icone)

        val SEM_CATEGORIA = Categoria().also {
            it.nome = App.get.applicationContext.getString(R.string.Sem_categoria)
            it.id = "999"
        }
    }

    override fun clonar(): Categoria {
        val g = GsonBuilder().setPrettyPrinting().create()
        return g.fromJson(g.toJson(this), Categoria::class.java)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Categoria

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