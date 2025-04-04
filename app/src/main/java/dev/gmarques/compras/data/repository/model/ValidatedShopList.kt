package dev.gmarques.compras.data.repository.model

import dev.gmarques.compras.App
import dev.gmarques.compras.data.model.ShopList
import dev.gmarques.compras.data.repository.UserRepository

/**
 * Autor: Gilian
 * Data de Criação: 02/01/2025
 * Valida uma Lista de compras antes que seja salva no banco de dados, garantindo
 * que apenas objetos que atendam às regras de negócio sejam salvos.
 */
class ValidatedShopList(shopList: ShopList) {

    val shopList: ShopList = shopList.copy(
        updatedBy = UserRepository.getUser()?.email
    )

    init {
        shopList.selfValidate(App.getContext())
    }
}

