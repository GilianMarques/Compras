package dev.gmarques.compras.domain.entidades

import androidx.room.PrimaryKey

import dev.gmarques.compras.domain.ConvencaoNome.formatarComoNomeValido
import java.io.Serializable
import java.util.*

abstract class DomainModel : Sincronizavel, Serializable {
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

    abstract fun clonar(): DomainModel


}