package dev.gmarques.compras.data.repository.model

import dev.gmarques.compras.data.model.Category

/**
 * Autor: Gilian
 * Data de Criação: 02/01/2025
 * Valida uma categoria antes que seja salva no banco de dados, garantindo
 * que apenas objetos que atendam às regras de negócio sejam salvos.
 */
class ValidatedCategory(val category: Category) {
    init {
        category.selfValidate()
    }
}

