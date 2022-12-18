package dev.gmarques.compras.domain.entidades

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.GsonBuilder
import java.util.*

@Entity
/**
 * Produto que ficara armazenado para servir de sugestao ao criar novas listas.
 * Esse produto nao precisa de uma UID especial, sua chave primaria sera seu nome.
 * Apenas um do mesmo nome deve existir.
 * */
class ProdutoDispensa : Sincronizavel {

    @PrimaryKey
    var nome: String = ""
    var preco: Float = 0.1f
    var quantidade: Int = 1
    var detalhes: String? = null
    var categoriaId: String = ""

    @Ignore
    override var id: String = ""
    override var removido: Boolean = false
    override var ultimaAtualizacao: Long = 0

    fun produto(): Produto {
        val g = GsonBuilder().create()
        val json = g.toJson(this)
        return g.fromJson(json, Produto::class.java).also { produto ->
            produto.ultimaAtualizacao = System.currentTimeMillis()
            produto.removido = false
            produto.comprado = false
            produto.id = UUID.randomUUID().toString()
        }
    }
}