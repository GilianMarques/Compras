package dev.gmarques.compras.domain.model

import dev.gmarques.compras.data.model.Category

data class CategoryWithProductsStats(val category: Category, val totalProducts: Int, val boughtProducts: Int, val selected: Boolean)
