package dev.gmarques.compras.data.data.model

data class Product(
    val shopListId: Long,
    val name: String,
    val position: Int,
    val price: Double,
    val info: String,
    val obs: String,
    val id: Long = System.currentTimeMillis(),
) {
    constructor() : this(0, "not_initialized", 0, 0.0, "not_initialized", "not_initialized")

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Product

        if (shopListId != other.shopListId) return false
        if (id != other.id) return false
        if (name != other.name) return false
        if (position != other.position) return false
        if (price != other.price) return false
        if (info != other.info) return false
        if (obs != other.obs) return false

        return true
    }

    override fun hashCode(): Int {
        var result = shopListId.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + position
        result = 31 * result + price.hashCode()
        result = 31 * result + info.hashCode()
        result = 31 * result + obs.hashCode()
        return result
    }

    override fun toString(): String {
        return "Product(shopListId='$shopListId', id=$id, name='$name', position=$position, price=$price, info='$info', obs='$obs')"
    }


}
