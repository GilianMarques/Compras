package dev.gmarques.compras.objetos

import androidx.room.Entity
import dev.gmarques.compras.App
import dev.gmarques.compras.R
import java.util.*

@Entity
class Lista : Objetos() {
    companion object {
        val PADRAO = Lista().also {
            it.nome = String.format(App.get.applicationContext.getString(R.string.lista),
                Random().nextInt(999))
        }
    }
}