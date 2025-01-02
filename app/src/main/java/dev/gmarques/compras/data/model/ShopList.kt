package dev.gmarques.compras.data.model

import dev.gmarques.compras.App
import dev.gmarques.compras.R.string.O_nome_deve_ter_no_m_nimo_x_caracteres
import dev.gmarques.compras.R.string.O_nome_deve_ter_no_m_ximo_x_caracteres
import dev.gmarques.compras.R.string.O_nome_nao_pode_ficar_vazio
import dev.gmarques.compras.domain.utils.ExtFun.Companion.removeSpaces
import java.io.Serializable
import java.util.Locale
import java.util.UUID


data class ShopList(
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
     * Garante que nenhuma lista entre no banco de dados sem atender às regras de negócio
     */
    fun selfValidate(): ShopList {

        if (Validator.validateName(name).isFailure) throw Exception("Nome da lista é invalio: '${name}'")

        return this
    }

    @Suppress("unused") // necessario pra uso com firebase
    constructor() : this("not_initialized", 0)

    constructor(id: String) : this("", 0, id)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ShopList

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
        return "ShopList(name='$name', color=$color, id='$id', creationDate=$creationDate)"
    }

    class Validator {
        companion object {
            // Constantes privadas
            private const val MAX_CHARS = 30
            private const val MIN_CHARS = 3

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
        }
    }

}

