package dev.gmarques.compras.data.data.model

data class Product(
    val shopListId: Long,
    val name: String,
    val position: Int=-1,
    val price: Double,
    val quantity: Int,
    val info: String,
    val isBought: Boolean,
    val id: Long = System.currentTimeMillis(),
) {
    constructor() : this(0, "not_initialized", -1, 0.0, 0, "not_initialized", false)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Product

        if (shopListId != other.shopListId) return false
        if (name != other.name) return false
        if (position != other.position) return false
        if (price != other.price) return false
        if (quantity != other.quantity) return false
        if (info != other.info) return false
        if (isBought != other.isBought) return false
        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        var result = shopListId.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + position
        result = 31 * result + price.hashCode()
        result = 31 * result + quantity
        result = 31 * result + info.hashCode()
        result = 31 * result + isBought.hashCode()
        result = 31 * result + id.hashCode()
        return result
    }

    override fun toString(): String {
        return "Product(shopListId=$shopListId, name='$name', position=$position, price=$price, quantity=$quantity, info='$info', bought=$isBought, id=$id)"
    }


}
