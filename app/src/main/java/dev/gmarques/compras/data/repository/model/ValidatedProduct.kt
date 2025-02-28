package dev.gmarques.compras.data.repository.model

import dev.gmarques.compras.App
import dev.gmarques.compras.data.model.Product
import dev.gmarques.compras.data.model.ShopList
import dev.gmarques.compras.data.repository.UserRepository

/**
 * Autor: Gilian
 * Data de Criação: 02/01/2025
 * Valida um um produto antes que seja salvo no banco de dados, garantindo
 * que apenas objetos que atendam às regras de negócio sejam salvos.
 */
class ValidatedProduct(product: Product) {

    val product: Product = product.copy(
        updatedBy = UserRepository.getUser()?.email
    )

    init {
        product.selfValidate(App.getContext())
    }
}

