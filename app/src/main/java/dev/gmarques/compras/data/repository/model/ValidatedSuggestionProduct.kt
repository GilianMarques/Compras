package dev.gmarques.compras.data.repository.model

import dev.gmarques.compras.App
import dev.gmarques.compras.data.model.Product
import dev.gmarques.compras.data.repository.UserRepository

/**
 * Autor: Gilian
 * Data de Criação: 03/01/2025
 * Valida um produto de sugestão antes que seja salvo no banco de dados, garantindo
 * que apenas objetos que atendam às regras de negócio sejam salvos.
 */
class ValidatedSuggestionProduct(product: Product) {

    val suggestionProduct: Product

    init {
        // TODO: separar o produto de sugestao em uma classe diferente
        //defino uma id de lista arbitraria pra evitar uma exceção ao avaliar essa propriedade, uma vez que
        // produtos de sugestão nao devem ter id de lista ja que nao pertencem a nenhuma.
        product.copy(shopListId = "suggestion_product").selfValidate(App.getContext())

        suggestionProduct = product
            .withNewId()
            .copy(
                shopListId = "",
                hasBeenBought = false,
                updatedBy = UserRepository.getUser()?.email
            )
    }
}

