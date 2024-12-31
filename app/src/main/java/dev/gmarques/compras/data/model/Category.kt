package dev.gmarques.compras.data.model

import dev.gmarques.compras.App
import dev.gmarques.compras.R.string.A_cor_nao_pode_ficar_vazia
import dev.gmarques.compras.R.string.O_nome_deve_ter_no_m_nimo_x_caracteres
import dev.gmarques.compras.R.string.O_nome_deve_ter_no_m_ximo_x_caracteres
import dev.gmarques.compras.R.string.O_nome_nao_pode_ficar_vazio
import dev.gmarques.compras.utils.ExtFun.Companion.removeSpaces
import java.io.Serializable
import java.util.Locale
import java.util.UUID


data class Category(
    val name: String,
    val color: Int,
    val id: String = getNewId(),
    val creationDate: Long = System.currentTimeMillis(),
) : Serializable {


    companion object {
        private fun getNewId(): String {
            return UUID.randomUUID().toString()
        }
    }

    /**
     * Garante que nenhuma categoria entre no banco de dados sem atender às regras de negócio
     */
    fun selfValidate(): Category {

        if (Validator.validateName(name).isFailure) throw Exception("Nome da categoria é invalido: '${name}'")
        if (Validator.validateColor(color).isFailure) throw Exception("Cor da categoria é invalida: '${color}'")

        return this
    }

    @Suppress("unused") // necessario pra uso com firebase
    constructor() : this("not_initialized", 0)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Category

        if (name != other.name) return false
        if (color != other.color) return false
        if (id != other.id) return false
        if (creationDate != other.creationDate) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + color
        result = 31 * result + id.hashCode()
        result = 31 * result + creationDate.hashCode()
        return result
    }

    override fun toString(): String {
        return "Category(name='$name', color=$color, id='$id', creationDate=$creationDate)"
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

            fun validateColor(input: Int): Result<Int> {

                return when {
                    input == -1 || input.toString().length <= 2 -> Result.failure(
                        Exception(
                            App.getContext().getString(A_cor_nao_pode_ficar_vazia)
                        )
                    )

                    else -> {
                        Result.success(input)
                    }
                }
            }

        }
    }

}

