package dev.gmarques.compras.data.repository.model

import dev.gmarques.compras.data.model.Product

/**
 * Autor: Gilian
 * Data de Criação: 03/01/2025
 * Valida um produto de sugestão antes que seja salvo no banco de dados, garantindo
 * que apenas objetos que atendam às regras de negócio sejam salvos.
 */
class ValidatedSuggestionProduct(product: Product) {

     val suggestionProduct: Product

    init {
        product.selfValidate()
        suggestionProduct = product
            .withNewId()
            .copy(shopListId = "", hasBeenBought = false)
    }
}

