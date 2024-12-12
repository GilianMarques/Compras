package dev.gmarques.compras.data.data.model


data class ShopList(
    val name: String,
    val color: Int,
    val id: Long = System.currentTimeMillis(),
) {

    constructor() : this("not_initializes", 0)


    //  Atualize o equals e hashcode a cada nova variavel inserida aqui


    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (other !is ShopList) return false
        if (this.id != other.id) return false
        if (this.name != other.name) return false
        if (this.color != other.color) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + color
        return result
    }

    override fun toString(): String {
        return "ShopList(id=$id, name='$name', color=$color)"
    }

}

