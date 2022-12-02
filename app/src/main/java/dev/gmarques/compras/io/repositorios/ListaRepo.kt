package dev.gmarques.compras.io.repositorios

import dev.gmarques.compras.io.database.RoomDb
import dev.gmarques.compras.io.preferencias.Preferencias
import dev.gmarques.compras.entidades.Lista


object ListaRepo : BaseRepo() {

    suspend fun getUtimaOuQualquerLista(): Lista? {
        val id: String? = Preferencias().getString(Preferencias.ultimaLista)
        var lista: Lista? = null

        val listas = RoomDb.getInstancia().listaDao().getListas()
        for (cLista in listas) if (cLista.id == id) {
            lista = cLista
            break
        }
        return lista ?: if (listas.isNotEmpty()) listas[0]
        else null
    }

}
