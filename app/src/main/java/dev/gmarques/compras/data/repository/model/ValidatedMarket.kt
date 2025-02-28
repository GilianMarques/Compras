package dev.gmarques.compras.data.repository.model

import dev.gmarques.compras.App
import dev.gmarques.compras.data.model.Market

/**
 * Autor: Gilian
 * Data de Criação: 02/01/2025
 * Valida um mercado antes que seja salva no banco de dados, garantindo
 * que apenas objetos que atendam às regras de negócio sejam salvos.
 */
class ValidatedMarket(val market: Market) {
    init {
        market.selfValidate(App.getContext())
    }
}

