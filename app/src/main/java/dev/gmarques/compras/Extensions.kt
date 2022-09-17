package dev.gmarques.compras

class Extensions {
    companion object {
        fun String.capitalizar(): String = this[0].uppercase() + this.substring(1).lowercase()
    }

}