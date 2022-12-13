package dev.gmarques.compras.data.entidades

import androidx.room.PrimaryKey

import dev.gmarques.compras.domain.ConvencaoNome.formatarComoNomeValido
import java.io.Serializable
import java.util.*

/*Essa classe serve para aplicar regras rigidas sobre os nomes dos modelos de objetos*/
abstract class Entidade : Sinc, Serializable {
    // TODO: essa classe pertence ao domain?
    @PrimaryKey
    override var id: String = UUID.randomUUID().toString()
    override var ultimaAtualizacao: Long = System.currentTimeMillis()

    override var removido: Boolean = false

    var nome: String = ""
        set(value) {
            field = value.formatarComoNomeValido().ifEmpty {
                throw java.lang.Exception("nome invalido, apos a aplicação das regras de nome a string ficou vazia: $value")
            }
        }

    abstract fun clonar(): Entidade


}