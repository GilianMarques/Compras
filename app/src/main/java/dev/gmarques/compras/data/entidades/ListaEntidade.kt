package dev.gmarques.compras.data.entidades

import androidx.room.Entity
import com.google.gson.GsonBuilder
import dev.gmarques.compras.App
import dev.gmarques.compras.R
import java.util.*

@Entity
class ListaEntidade : Entidade() {
    companion object {
        val PADRAO = ListaEntidade().also {
            it.nome = String.format(App.get.applicationContext.getString(R.string.Lista),
                Random().nextInt(999))
        }
    }

    override fun clonar(): ListaEntidade {
        val g = GsonBuilder().setPrettyPrinting().create()
        return g.fromJson(g.toJson(this), ListaEntidade::class.java)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ListaEntidade

        if (id != other.id) return false
        if (ultimaAtualizacao != other.ultimaAtualizacao) return false
        if (removido != other.removido) return false
        if (nome != other.nome) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + ultimaAtualizacao.hashCode()
        result = 31 * result + removido.hashCode()
        result = 31 * result + nome.hashCode()
        return result
    }

}