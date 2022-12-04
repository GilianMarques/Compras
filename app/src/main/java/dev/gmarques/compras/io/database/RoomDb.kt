package dev.gmarques.compras.io.database

import android.graphics.Color
import androidx.room.Room
import androidx.room.RoomDatabase
import dev.gmarques.compras.App
import dev.gmarques.compras.entidades.Categoria
import dev.gmarques.compras.entidades.Lista
import dev.gmarques.compras.entidades.Produto
import java.util.*

// exemplo de auto-migração
/*   autoMigrations = [AutoMigration(from = 1, to = 2), version = 2, exportSchema = true)*/

@androidx.room.Database(entities = [Produto::class, Categoria::class, Lista::class],
    version = 1,
    exportSchema = true)
abstract class RoomDb : RoomDatabase() {

    abstract fun produtoDao(): ProdutoDao
    abstract fun listaDao(): ListaDao
    abstract fun categoriaDao(): CategoriaDao

    companion object {
        @Volatile
        private var INSTANCIA: RoomDb? = null

        @Synchronized // use as classes repositorio pra I/O no banco
        fun getInstancia() = INSTANCIA ?: criarDb().also { INSTANCIA = it }

        private fun criarDb() =
            Room.databaseBuilder(App.get.applicationContext, RoomDb::class.java, "database.sql")
                .build()


        // TODO: remover funcoes abaixo quando o app sair dessa fase inicial
        suspend fun criarListasItensEcategorias() {

            for (i in 0 until 10) {
                val lista = Lista()
                lista.nome = "Lista #$i"
                for (j in 0 until 35) {
                    val produto = Produto()
                    with(produto) {
                        nome = "produto #$j"
                        preco = j * 0.75f
                        quantidade = j
                        detalhes = "$j itens criados"
                        categoriaId = carregarCategorias()[Random().nextInt(4)].id
                        listaId = lista.id
                        getInstancia().produtoDao().addOuAtualizar(produto)

                    }
                }
                getInstancia().listaDao().addOuAtualizar(lista)
            }
        }

        private val categorias: ArrayList<Categoria> = ArrayList()
        private val cores: ArrayList<Int> = arrayListOf(Color.CYAN,
            Color.GREEN,
            Color.MAGENTA,
            Color.RED,
            Color.YELLOW,
            Color.DKGRAY)

        private suspend fun carregarCategorias(): ArrayList<Categoria> {
            if (categorias.size == 0) for (i in 0 until 5) {
                val c = Categoria()
                with(c) {
                    nome = "categ #$i"
                    icone = "vec_cat_${i+10}"
                }
                categorias.add(c)
                getInstancia().categoriaDao().addOuAtualizar(c)
            }
            return categorias
        }

    }


}