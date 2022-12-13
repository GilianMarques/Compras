package dev.gmarques.compras.data.database

import android.graphics.Color
import androidx.room.Room
import androidx.room.RoomDatabase
import dev.gmarques.compras.App
import dev.gmarques.compras.data.entidades.CategoriaEntidade
import dev.gmarques.compras.data.entidades.ListaEntidade
import dev.gmarques.compras.data.entidades.ProdutoEntidade
import java.util.*

// exemplo de auto-migração
/*   autoMigrations = [AutoMigration(from = 1, to = 2), version = 2, exportSchema = true)*/

@androidx.room.Database(entities = [ProdutoEntidade::class, CategoriaEntidade::class, ListaEntidade::class],
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
                val listaEntidade = ListaEntidade()
                listaEntidade.nome = "Lista #$i"
                for (j in 0 until 35) {
                    val produtoEntidade = ProdutoEntidade()
                    with(produtoEntidade) {
                        nome = "produto #$j"
                        preco = j * 0.75f
                        quantidade = j
                        detalhes = "$j itens criados"
                        categoriaId = carregarCategorias()[Random().nextInt(4)].id
                        listaId = listaEntidade.id
                        getInstancia().produtoDao().addOuAtualizar(produtoEntidade)

                    }
                }
                getInstancia().listaDao().addOuAtualizar(listaEntidade)
            }
        }

        private val categorias: ArrayList<CategoriaEntidade> = ArrayList()
        private val cores: ArrayList<Int> = arrayListOf(Color.CYAN,
            Color.GREEN,
            Color.MAGENTA,
            Color.RED,
            Color.YELLOW,
            Color.DKGRAY)

        private suspend fun carregarCategorias(): ArrayList<CategoriaEntidade> {
            if (categorias.size == 0) for (i in 0 until 5) {
                val c = CategoriaEntidade()
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