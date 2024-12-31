package dev.gmarques.compras.data.model

import dev.gmarques.compras.App
import dev.gmarques.compras.R.string.Insira_um_preco_valido
import dev.gmarques.compras.R.string.Insira_uma_quantidade_valida
import dev.gmarques.compras.R.string.O_nome_deve_ter_no_m_nimo_x_caracteres
import dev.gmarques.compras.R.string.O_nome_deve_ter_no_m_ximo_x_caracteres
import dev.gmarques.compras.R.string.O_nome_nao_pode_ficar_vazio
import dev.gmarques.compras.R.string.Os_detalhes_devem_ter_ate_x_caracteres
import dev.gmarques.compras.utils.ExtFun.Companion.removeSpaces
import java.util.Locale
import java.util.UUID

data class Product(
    val shopListId: String,
    val categoryId: String,
    val name: String,
    val position: Int = -1,
    val price: Double,
    val quantity: Int,
    val info: String,
    val hasBeenBought: Boolean = false, // isBought estava dando problema com o firebase
    val id: String = getNewId(),
    val creationDate: Long = System.currentTimeMillis(),
) {

    @Suppress("unused")  // necessario pra uso com firebase
    constructor() : this("", "", "not_initialized", -1, 0.0, 0, "not_initialized", false)


    /**
     * Garante que nenhum produto entre no banco de dados sem atender às regras de negócio
     */
    fun selfValidate(): Product {

        val resultvalidateName = Validator.validateName(name)
        if (resultvalidateName.isFailure) throw Exception("Nome do produto é invalio: '${name}' -> ${resultvalidateName.exceptionOrNull()!!.message}'")

        val resultvalidatePrice = Validator.validatePrice(price)
        if (resultvalidatePrice.isFailure) throw Exception("Preço do produto é invalido '${price}' -> ${resultvalidatePrice.exceptionOrNull()!!.message}'")

        val resultvalidateQuantity = Validator.validateQuantity(quantity)
        if (resultvalidateQuantity.isFailure) throw Exception("Quantidade do produto é invalida '${quantity}' -> ${resultvalidateQuantity.exceptionOrNull()!!.message}'")

        val resultvalidateInfo = Validator.validateInfo(info)
        if (resultvalidateInfo.isFailure) throw Exception("Info do produto é invalida '${info}' -> ${resultvalidateInfo.exceptionOrNull()!!.message}'")

        val resultvalidateShopListId = Validator.validateShopListId(shopListId)
        if (resultvalidateShopListId.isFailure) throw Exception("Id da lista associada ao produto é invalida '${shopListId}' -> ${resultvalidateShopListId.exceptionOrNull()!!.message}'")

        val resultvalidateCategoryId = Validator.validateCategoryId(categoryId)
        if (resultvalidateCategoryId.isFailure) throw Exception("Id da categoria associada ao produto é invalida '${categoryId}' -> ${resultvalidateCategoryId.exceptionOrNull()!!.message}'")


        return this
    }

    fun withNewId(): Product = this.copy(id = getNewId())

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Product

        if (shopListId != other.shopListId) return false
        if (categoryId != other.categoryId) return false
        if (name != other.name) return false
        if (position != other.position) return false
        if (price != other.price) return false
        if (quantity != other.quantity) return false
        if (info != other.info) return false
        if (hasBeenBought != other.hasBeenBought) return false
        if (id != other.id) return false
        if (creationDate != other.creationDate) return false

        return true
    }

    override fun hashCode(): Int {
        var result = shopListId.hashCode()
        result = 31 * result + categoryId.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + position
        result = 31 * result + price.hashCode()
        result = 31 * result + quantity
        result = 31 * result + info.hashCode()
        result = 31 * result + hasBeenBought.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + creationDate.hashCode()
        return result
    }

    override fun toString(): String {
        return "Product(shopListId='$shopListId', categoryId='$categoryId', name='$name', position=$position, price=$price, quantity=$quantity, info='$info', hasBeenBought=$hasBeenBought, id='$id', creationDate=$creationDate)"
    }

    companion object {
        private fun getNewId(): String {
            return UUID.randomUUID().toString()
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

            fun validateShopListId(shopListId: String): Result<String> {
                return when {
                    shopListId.isEmpty() -> Result.failure(Exception("O id da lista não pode ser vazio"))
                    shopListId.length < 3 -> Result.failure(Exception("O id da lista parece ser inválido"))
                    else -> Result.success(shopListId)
                }
            }

            fun validateCategoryId(categotyId: String): Result<String> {
                return when {
                    categotyId.isEmpty() -> Result.failure(Exception("O id da categoria não pode ser vazio"))
                    categotyId.length < 3 -> Result.failure(Exception("O id da categoria parece ser inválido"))
                    else -> Result.success(categotyId)
                }
            }

        }
    }

}


