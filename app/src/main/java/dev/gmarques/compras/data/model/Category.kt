package dev.gmarques.compras.data.model

import dev.gmarques.compras.App
import dev.gmarques.compras.R.string.A_cor_nao_pode_ficar_vazia
import dev.gmarques.compras.R.string.O_nome_deve_ter_no_m_nimo_x_caracteres
import dev.gmarques.compras.R.string.O_nome_deve_ter_no_m_ximo_x_caracteres
import dev.gmarques.compras.R.string.O_nome_nao_pode_ficar_vazio
import dev.gmarques.compras.domain.utils.ExtFun.Companion.removeSpaces
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
    fun selfValidate() {

        if (Validator.validateName(name).isFailure) throw Exception("Nome da categoria é invalido: '${name}'")
        if (Validator.validateColor(color).isFailure) throw Exception("Cor da categoria é invalida: '${color}'")
    }

    @Suppress("unused") // necessario pra uso com firebase
    constructor() : this("not_initialized", 0)

    class Validator {
        companion object {

            private const val MAX_CHARS = 30
            private const val MIN_CHARS = 3
            private const val EMPTY_COLOR = -1
            private const val COLOR_LENGTH_THRESHOLD = 2

            fun validateName(input: String): Result<String> {
                return when {
                    input.isEmpty() -> Result.failure(
                        Exception(
                            App.getContext().getString(O_nome_nao_pode_ficar_vazio)
                        )
                    )

                    input.length < MIN_CHARS -> Result.failure(
                        Exception(
                            String.format(App.getContext().getString(O_nome_deve_ter_no_m_nimo_x_caracteres), MIN_CHARS)
                        )
                    )

                    input.length > MAX_CHARS -> Result.failure(
                        Exception(
                            String.format(App.getContext().getString(O_nome_deve_ter_no_m_ximo_x_caracteres), MAX_CHARS)
                        )
                    )

                    else -> {
                        Result.success(input.removeSpaces().replaceFirstChar { it.titlecase(Locale.getDefault()) })
                    }
                }
            }

            fun validateColor(input: Int): Result<Int> {
                return when {
                    input == EMPTY_COLOR || input.toString().length <= COLOR_LENGTH_THRESHOLD -> Result.failure(
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