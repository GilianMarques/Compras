package dev.gmarques.compras.data.repositorios

import dev.gmarques.compras.data.database.RoomDb
import dev.gmarques.compras.data.preferencias.Preferencias
import dev.gmarques.compras.domain.entidades.Lista


object ListaRepo : BaseRepo() {

    suspend fun getUtimaOuQualquerLista(): Lista? {
        val id: String? = Preferencias().getString(Preferencias.ultimaLista)
        var lista: Lista? = null

        val listas = RoomDb.getInstancia().listaDao().getTodasAsListas()
        for (cLista in listas) if (cLista.id == id) {
            lista = cLista
            break
        }
        return lista ?: if (listas.isNotEmpty()) listas[0]
        else null
    }

    suspend fun getListaPorNome(nome: String) =
            RoomDb.getInstancia().listaDao().getListaPorNome(nome)

    suspend fun addAttLista(lista: Lista) {
        RoomDb.getInstancia().listaDao().addOuAtualizar(lista)
    }

    suspend fun getTodasAsListas() = RoomDb.getInstancia().listaDao().getTodasAsListas()


}
