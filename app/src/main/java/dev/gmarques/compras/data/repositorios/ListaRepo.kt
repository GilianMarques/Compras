package dev.gmarques.compras.data.repositorios

import dev.gmarques.compras.data.database.RoomDb
import dev.gmarques.compras.data.preferencias.Preferencias
import dev.gmarques.compras.data.entidades.ListaEntidade


object ListaRepo : BaseRepo() {

    suspend fun getUtimaOuQualquerLista(): ListaEntidade? {
        val id: String? = Preferencias().getString(Preferencias.ultimaLista)
        var listaEntidade: ListaEntidade? = null

        val listas = RoomDb.getInstancia().listaDao().getListas()
        for (cLista in listas) if (cLista.id == id) {
            listaEntidade = cLista
            break
        }
        return listaEntidade ?: if (listas.isNotEmpty()) listas[0]
        else null
    }

}
