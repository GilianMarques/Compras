package dev.gmarques.compras.domain.entidades

import androidx.room.Entity
import com.google.gson.GsonBuilder

@Entity
class Lista : EntidadeDominio() {

    // TODO: Esse objeto pode manter referencia da ultima categoria selecionada pelo usuario

    override fun clonar(): Lista {
        val g = GsonBuilder().setPrettyPrinting().create()
        return g.fromJson(g.toJson(this), Lista::class.java)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Lista

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