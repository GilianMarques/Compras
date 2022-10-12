package dev.gmarques.compras.objetos

import androidx.room.PrimaryKey
import dev.gmarques.compras.Extensions.Companion.capitalizar
import java.io.Serializable
import java.util.*

/*Essa classe serve para aplicar regras rigidas sobre os nomes dos modelos de objetos*/
open class Objetos : Sinc, Serializable {

    @PrimaryKey
    override var id: String = UUID.randomUUID().toString()
    override var ultimaAtualizacao: Long = System.currentTimeMillis()

    override var nome: String = ""
        set(value) {
            field = if (value.isEmpty()) ""
            else value.replace(Regex("[^0-9a-zA-Z ]"), "").replace(Regex("[ ]+"), " ").trim()
                .capitalizar()
        }

}