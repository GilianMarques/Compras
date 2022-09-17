package dev.gmarques.compras.objetos

import android.os.Parcelable
import dev.gmarques.compras.Extensions.Companion.capitalizar
import java.io.Serializable
import java.util.*

/*Essa classe serve para aplicar regras rigidas sobre os nomes dos modelos de objtos*/
open class Objetos : Sinc, Serializable {

    override var id: String = UUID.randomUUID().toString()
    override var ultimaAtualizacao: Long = System.currentTimeMillis()

    override var nome: String = ""
        set(value) {
            field = value.replace(Regex("[^0-9a-zA-Z ]"), "")
                .replace(Regex("[ ]+"), " ")
                .trim()
                .capitalizar()
        }

}