package dev.gmarques.compras.objetos

import androidx.room.Entity
import dev.gmarques.compras.Extensions.Companion.emMoeda
import java.math.BigDecimal
import java.math.MathContext

@Entity
data class Item(
    var preco: Float = 0.1f,
    var qtd: Int = 1,
    var detalhes: String? = null,
    var comprado: Boolean = false,
    var categoriaId: String = "999",//categoria padrao
    var listaId: String = "",
) : Objetos() {

    fun emMoeda(): String = preco.emMoeda()

    fun valorTotal(): Float =
        BigDecimal(preco.toDouble()).multiply(BigDecimal(qtd)).round(MathContext.DECIMAL32)
            .toFloat()

    fun valotTotalEmMoeda(): String =
        BigDecimal(preco.toDouble()).multiply(BigDecimal(qtd)).round(MathContext.DECIMAL32)
            .toFloat().emMoeda()




}