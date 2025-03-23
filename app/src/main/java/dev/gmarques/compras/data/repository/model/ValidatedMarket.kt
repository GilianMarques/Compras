package dev.gmarques.compras.data.repository.model

import dev.gmarques.compras.App
import dev.gmarques.compras.data.model.Establishment
import dev.gmarques.compras.data.model.Product
import dev.gmarques.compras.data.repository.UserRepository

/**
 * Autor: Gilian
 * Data de Criação: 02/01/2025
 * Valida um estabelecimento antes que seja salva no banco de dados, garantindo
 * que apenas objetos que atendam às regras de negócio sejam salvos.
 */
class ValidatedEstablishment(establishment: Establishment) {

    val establishment: Establishment = establishment.copy(
        updatedBy = UserRepository.getUser()?.email
    )

    init {
        establishment.selfValidate(App.getContext())
    }
}

