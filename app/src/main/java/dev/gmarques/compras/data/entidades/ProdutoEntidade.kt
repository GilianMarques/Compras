package dev.gmarques.compras.data.entidades

import androidx.room.Entity
import com.google.gson.GsonBuilder
import dev.gmarques.compras.Extensions.emMoeda
import java.math.BigDecimal
import java.math.MathContext

@Entity
class ProdutoEntidade(
    var preco: Float = 0.1f,
    var quantidade: Int = 1,
    var detalhes: String? = null,
    var comprado: Boolean = false,
    var categoriaId: String = "999",//categoria padrao
    var listaId: String = "",
) : Entidade() {

    fun valorTotal(): Double =
            BigDecimal(preco.toDouble()).multiply(BigDecimal(quantidade))
                .round(MathContext.DECIMAL32)
                .toDouble()

    override fun clonar(): ProdutoEntidade {
        val g = GsonBuilder().setPrettyPrinting().create()
        return g.fromJson(g.toJson(this), ProdutoEntidade::class.java)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ProdutoEntidade

        if (id != other.id) return false
        if (ultimaAtualizacao != other.ultimaAtualizacao) return false
        if (removido != other.removido) return false
        if (nome != other.nome) return false
        if (preco != other.preco) return false
        if (quantidade != other.quantidade) return false
        if (detalhes != other.detalhes) return false
        if (comprado != other.comprado) return false
        if (categoriaId != other.categoriaId) return false
        if (listaId != other.listaId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + ultimaAtualizacao.hashCode()
        result = 31 * result + removido.hashCode()
        result = 31 * result + nome.hashCode()
        result = 31 * result + preco.hashCode()
        result = 31 * result + quantidade.hashCode()
        result = 31 * result + detalhes.hashCode()
        result = 31 * result + comprado.hashCode()
        result = 31 * result + categoriaId.hashCode()
        result = 31 * result + listaId.hashCode()
        return result
    }

}