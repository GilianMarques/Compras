package dev.gmarques.compras.database

import android.graphics.Color
import android.util.Log
import dev.gmarques.compras.objetos.Categoria
import dev.gmarques.compras.objetos.Item
import dev.gmarques.compras.objetos.Lista

class Database private constructor() {


    companion object {
        private var db: Database? = null
        fun inst(): Database {
            if (db == null) db = Database()
            return db!!
        }
    }

    private val listas = ArrayList<Lista>()

    fun getListas(): ArrayList<Lista> {
        Log.d("USUK: Database", "getListas() called")
       // if (listas.size > 0) return listas

        val categorias = ArrayList<Categoria>()

        for (i in 0 until 5) {
            val c = Categoria()
            c.nome = "Categoria#$i"
            c.cor = Color.CYAN
            categorias.add(c)
        }

        for (i in 0 until 10) {
            val lista = Lista()
            lista.nome = "Lista#$i"
            for (j in 0 until 50) {
                val item = Item()
                with(item) {
                    nome = "item#$j"
                    preco = j * 0.75f
                    qtd = j
                    detalhes = "$j itens crados"
                }
                lista.addItem(item)
            }

            listas.add(lista)
        }
        return listas
    }
}