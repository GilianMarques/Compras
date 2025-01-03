package dev.gmarques.compras.domain.model

import dev.gmarques.compras.data.model.Product

data class SelectableProduct(val product: Product, var isSelected: Boolean, var quantity: Int)

