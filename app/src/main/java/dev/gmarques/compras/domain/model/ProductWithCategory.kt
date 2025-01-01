package dev.gmarques.compras.domain.model

import dev.gmarques.compras.data.model.Category
import dev.gmarques.compras.data.model.Product

data class ProductWithCategory(val product: Product, val category: Category)
