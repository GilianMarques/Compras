package dev.gmarques.compras.data.data.model

import java.io.Serializable


data class ShopList(
    val name: String,
    val color: Int,
    val id: Long = System.currentTimeMillis(),
) : Serializable {

    @Suppress("unused") // necessario pra uso com firebase
    constructor() : this("not_initialized", 0)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ShopList

        if (name != other.name) return false
        if (color != other.color) return false
        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + color
        result = 31 * result + id.hashCode()
        return result
    }

    override fun toString(): String {
        return "ShopList(name='$name', color=$color, id=$id)"
    }


}

