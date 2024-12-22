package dev.gmarques.compras.data.data.model

import dev.gmarques.compras.R.string.Insira_um_preco_valido
import dev.gmarques.compras.R.string.Insira_uma_quantidade_valida
import dev.gmarques.compras.R.string.O_nome_deve_ter_no_m_nimo_x_caracteres
import dev.gmarques.compras.R.string.O_nome_deve_ter_no_m_ximo_x_caracteres
import dev.gmarques.compras.R.string.O_nome_nao_pode_ficar_vazio
import dev.gmarques.compras.R.string.Os_detalhes_devem_ter_ate_x_caracteres
import dev.gmarques.compras.utils.App
import dev.gmarques.compras.utils.ExtFun.Companion.removeSpaces
import java.util.Locale
import java.util.Random

data class Product(
    val shopListId: Long,
    val name: String,
    val position: Int = -1,
    val price: Double,
    val quantity: Int,
    val info: String,
    val hasBeenBought: Boolean = false, // isBought estava dando problema com o firebase
    val id: Long = getNewId(),
) {

    @Suppress("unused")  // necessario pra uso com firebase
    constructor() : this(0, "not_initialized", -1, 0.0, 0, "not_initialized", false)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Product

        if (shopListId != other.shopListId) return false
        if (name != other.name) return false
        if (position != other.position) return false
        if (price != other.price) return false
        if (quantity != other.quantity) return false
        if (info != other.info) return false
        if (hasBeenBought != other.hasBeenBought) return false
        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        var result = shopListId.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + position
        result = 31 * result + price.hashCode()
        result = 31 * result + quantity
        result = 31 * result + info.hashCode()
        result = 31 * result + hasBeenBought.hashCode()
        result = 31 * result + id.hashCode()
        return result
    }

    override fun toString(): String {
        return "Product(shopListId=$shopListId, name='$name', position=$position, price=$price, quantity=$quantity, info='$info', hasBeenBought=$hasBeenBought, id=$id)"
    }

    /**
     * Garante que nenhum produto entre no banco de dados sem atender às regras de negócio
     */
    fun selfValidate(): Product {

        if (Validator.validateName(name).isFailure) throw Exception("Nome do produto é invalio: '${name}'")
        if (Validator.validatePrice(price).isFailure) throw Exception("Preço do produto é invalido '${price}'")
        if (Validator.validateQuantity(quantity).isFailure) throw Exception("Quantidade do produto é invalida '${quantity}'")
        if (Validator.validateInfo(info).isFailure) throw Exception("Info do produto é invalida '${info}'")

        return this
    }

    fun withNewId(): Product = this.copy(id = getNewId())

    companion object {
        private fun getNewId(): Long {
            return System.currentTimeMillis() +
                    Random().nextInt(99) +
                    Random().nextInt(999) +
                    Random().nextInt(9999)
        }
    }

    class Validator {
        companion object {

            fun validateName(input: String): Result<String> {
                val maxChars = 30
                val minChars = 3

                return when {
                    input.isEmpty() -> Result.failure(
                        Exception(
                            App.getContext().getString(O_nome_nao_pode_ficar_vazio)
                        )
                    )

                    input.length < minChars -> Result.failure(
                        Exception(
                            String.format(App.getContext().getString(O_nome_deve_ter_no_m_nimo_x_caracteres), minChars)
                        )
                    )

                    input.length > maxChars -> Result.failure(
                        Exception(
                            String.format(App.getContext().getString(O_nome_deve_ter_no_m_ximo_x_caracteres), maxChars)
                        )
                    )

                    else -> {
                        Result.success(input.removeSpaces().replaceFirstChar { it.titlecase(Locale.getDefault()) })
                    }
                }
            }

            fun validateInfo(input: String): Result<String> {
                val maxChars = 50

                return when {

                    input.length > maxChars -> Result.failure(
                        Exception(
                            String.format(App.getContext().getString(Os_detalhes_devem_ter_ate_x_caracteres), maxChars)
                        )
                    )

                    else -> {
                        Result.success(input.removeSpaces().replaceFirstChar { it.titlecase(Locale.getDefault()) })
                    }
                }

            }

            fun validatePrice(input: Double): Result<Double> {
                val maxPrice = 9_999.99
                val minPrice = 0.1

                return when {

                    input !in minPrice..maxPrice -> Result.failure(
                        Exception(
                            (App.getContext().getString(Insira_um_preco_valido))
                        )
                    )

                    else -> Result.success(input)
                }

            }

            fun validateQuantity(input: Int): Result<Int> {
                val minQuantity = 1
                val maxQuantity = 99

                return when {

                    input !in minQuantity..maxQuantity -> Result.failure(
                        Exception(
                            (App.getContext().getString(Insira_uma_quantidade_valida))
                        )
                    )

                    else -> Result.success(input)
                }

            }

        }
    }

}


