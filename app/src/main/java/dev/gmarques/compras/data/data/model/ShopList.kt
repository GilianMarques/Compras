package dev.gmarques.compras.data.data.model

import java.util.UUID

class ShopList() {

    constructor(name: String) : this() {
        this.name = name
    }

    var id: String = UUID.randomUUID().toString()
    var createdDate: Long = System.currentTimeMillis()  // TODO: mudar pra utc
    var color: Int = 0
    var name: String = ""
    //  Atualize o equals e hashcode a cada nova variavel inserida aqui

    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (other !is ShopList) return false
        if (this.id != other.id) return false
        if (this.name != other.name) return false
        if (this.createdDate != other.createdDate) return false
        if (this.color != other.color) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + createdDate.hashCode()
        result = 31 * result + color
        result = 31 * result + name.hashCode()
        return result
    }
}

