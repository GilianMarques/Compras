package dev.gmarques.compras.data.model

import android.content.Context
import dev.gmarques.compras.R.string.As_anotacoes_devem_ter_ate_x_caracteres
import dev.gmarques.compras.R.string.Insira_um_preco_valido
import dev.gmarques.compras.R.string.Insira_uma_quantidade_valida
import dev.gmarques.compras.R.string.O_nome_deve_ter_no_m_nimo_x_caracteres
import dev.gmarques.compras.R.string.O_nome_deve_ter_no_m_ximo_x_caracteres
import dev.gmarques.compras.R.string.O_nome_nao_pode_ficar_vazio
import dev.gmarques.compras.R.string.Os_detalhes_devem_ter_ate_x_caracteres
import dev.gmarques.compras.data.repository.UserRepository
import dev.gmarques.compras.domain.utils.ExtFun.Companion.removeSpaces
import java.util.Locale
import java.util.UUID

// isBought estava dando problema com o firebase
data class Product(
    val name: String,
    val price: Double,
    val quantity: Int,
    val info: String = "",
    val annotations: String = "",
    val shopListId: String,
    val categoryId: String,
    val position: Int = -1,
    val establishmentId: String? = null,
    val id: String = getNewId(),
    val boughtDate: Long = System.currentTimeMillis(),
    val creationDate: Long = System.currentTimeMillis(),
    val removed: Boolean = false,
    val createdBy: String = UserRepository.getUser()!!.email!!,
    val updatedBy: String? = null,
    val hasBeenBought: Boolean = false,

    ) {

    constructor() : this(
        name = "",
        price = 0.0,
        quantity = 1,
        info = "",
        shopListId = "",
        categoryId = "",
        position = -1,
        id = "",
        boughtDate = 0L,
        creationDate = 0L,
        removed = false,
        createdBy = "",
        updatedBy = null,
        hasBeenBought = false
    )

    constructor(name:String) : this(
        name = name,
        price = 0.0,
        quantity = 1,
        info = "",
        shopListId = "",
        categoryId = "",
        position = -1,
        id = "",
        boughtDate = 0L,
        creationDate = 0L,
        removed = false,
        createdBy = "",
        updatedBy = null,
        hasBeenBought = false
    )

    /**
     * Garante que nenhum produto entre no banco de dados sem atender às regras de negócio
     */
    fun selfValidate(context: Context): Product {

        val resultValidateName = Validator.validateName(name, context)
        if (resultValidateName.isFailure) throw Exception("Nome do produto é invalido: '${name}' -> ${resultValidateName.exceptionOrNull()!!.message}'")

        val resultValidatePrice = Validator.validatePrice(price, context)
        if (resultValidatePrice.isFailure) throw Exception("Preço do produto é invalido '${price}' -> ${resultValidatePrice.exceptionOrNull()!!.message}'")

        val resultValidateQuantity = Validator.validateQuantity(quantity, context)
        if (resultValidateQuantity.isFailure) throw Exception("Quantidade do produto é invalida '${quantity}' -> ${resultValidateQuantity.exceptionOrNull()!!.message}'")

        val resultValidateInfo = Validator.validateInfo(info, context)
        if (resultValidateInfo.isFailure) throw Exception("Info do produto é invalida '${info}' -> ${resultValidateInfo.exceptionOrNull()!!.message}'")

        val resultValidateShopListId = Validator.validateShopListId(shopListId)
        if (resultValidateShopListId.isFailure) throw Exception("Id da lista associada ao produto é invalida '${shopListId}' -> ${resultValidateShopListId.exceptionOrNull()!!.message}'")


        val resultValidateCategoryId = Validator.validateCategoryId(categoryId)
        if (resultValidateCategoryId.isFailure) throw Exception("Id da categoria associada ao produto é invalida '${categoryId}' -> ${resultValidateCategoryId.exceptionOrNull()!!.message}'")

        return this
    }

    fun withNewId(): Product = this.copy(id = getNewId())

    companion object {
        private fun getNewId(): String {
            return UUID.randomUUID().toString()
        }
    }

    class Validator {
        companion object {

            const val MIN_QUANTITY = 1
            const val MAX_QUANTITY = 99
            private const val MAX_CHARS_INFO = 50
            private const val MAX_CHARS_ANNOTATIOS = 500
            private const val MAX_CHARS_NAME = 30
            private const val MIN_CHARS_NAME = 3
            private const val MAX_PRICE = 9_999.99
            private const val MIN_PRICE = 0.1

            fun validateName(input: String, context: Context): Result<String> {
                return when {
                    input.isEmpty() -> Result.failure(
                        Exception(
                            context.getString(O_nome_nao_pode_ficar_vazio)
                        )
                    )

                    input.length < MIN_CHARS_NAME -> Result.failure(
                        Exception(
                            String.format(
                                context.getString(O_nome_deve_ter_no_m_nimo_x_caracteres),
                                MIN_CHARS_NAME
                            )
                        )
                    )

                    input.length > MAX_CHARS_NAME -> Result.failure(
                        Exception(
                            String.format(
                                context.getString(O_nome_deve_ter_no_m_ximo_x_caracteres),
                                MAX_CHARS_NAME
                            )
                        )
                    )

                    else -> {
                        Result.success(
                            input.removeSpaces()
                                .replaceFirstChar { it.titlecase(Locale.getDefault()) })
                    }
                }
            }

            fun validateInfo(input: String, context: Context): Result<String> {

                val info = input.removeSpaces().replace("\n", "")
                    .replaceFirstChar { it.titlecase(Locale.getDefault()) }

                return when {

                    info.length > MAX_CHARS_INFO -> Result.failure(
                        Exception(
                            String.format(
                                context.getString(Os_detalhes_devem_ter_ate_x_caracteres),
                                MAX_CHARS_INFO
                            )
                        )
                    )

                    else -> {
                        Result.success(info)
                    }
                }

            }


            fun validateAnnotations(input: String, context: Context): Result<String> {

                val annotation = input.removeSpaces()
                    .replaceFirstChar { it.titlecase(Locale.getDefault()) }

                return when {

                    annotation.length > MAX_CHARS_ANNOTATIOS -> Result.failure(
                        Exception(
                            String.format(
                                context.getString(As_anotacoes_devem_ter_ate_x_caracteres),
                                MAX_CHARS_INFO
                            )
                        )
                    )

                    else -> {
                        Result.success(annotation)
                    }
                }

            }

            fun validatePrice(input: Double, context: Context): Result<Double> {

                return when {

                    input !in MIN_PRICE..MAX_PRICE -> Result.failure(
                        Exception(
                            (context.getString(Insira_um_preco_valido))
                        )
                    )

                    else -> Result.success(input)
                }

            }

            fun validateQuantity(input: Int, context: Context): Result<Int> {

                return when {

                    input !in MIN_QUANTITY..MAX_QUANTITY -> Result.failure(
                        Exception(
                            (context.getString(Insira_uma_quantidade_valida))
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

            fun validateCategoryId(categoryId: String): Result<String> {
                return when {
                    categoryId.isEmpty() -> Result.failure(Exception("O id da categoria não pode ser vazio"))
                    categoryId.length < 3 -> Result.failure(Exception("O id da categoria parece ser inválido"))
                    else -> Result.success(categoryId)
                }
            }

        }
    }

}
