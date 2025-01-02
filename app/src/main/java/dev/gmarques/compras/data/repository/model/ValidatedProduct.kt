package dev.gmarques.compras.data.repository.model

import dev.gmarques.compras.data.model.Product

/**
 * Autor: Gilian
 * Data de Criação: 02/01/2025
 * Valida um um produto antes que seja salvo no banco de dados, garantindo
 * que apenas objetos que atendam às regras de negócio sejam salvos.
 */
class ValidatedProduct(val product: Product) {
    init {
        product.selfValidate()
    }
}

