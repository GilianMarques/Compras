package dev.gmarques.compras

import java.text.NumberFormat

class Extensions {
    companion object {
        fun String.capitalizar(): String = this[0].uppercase() + this.substring(1).lowercase()
        fun Float.emMoeda(): String = NumberFormat.getCurrencyInstance().format(this)


    }

}