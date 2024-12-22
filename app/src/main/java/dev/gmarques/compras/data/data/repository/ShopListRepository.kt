package dev.gmarques.compras.data.data.repository

import com.google.firebase.firestore.toObject
import dev.gmarques.compras.R
import dev.gmarques.compras.data.data.ListenerRegister
import dev.gmarques.compras.data.data.firestore.Firestore
import dev.gmarques.compras.data.data.model.ShopList
import dev.gmarques.compras.utils.App
import dev.gmarques.compras.utils.RequestResult


object ShopListRepository {

    fun addOrAttShopList(shopList: ShopList) {
        Firestore.shopListCollection.document(shopList.id.toString())
            .set(shopList)
    }

    /**
     * Define um listener no firebase que notifica de altaraçoes locais e na nuvem
     * Lembre-se de remover o listener quando nao for mais necessario para evitar vazamentos de memoria
     * */
    fun observeListUpdates(onSnapshot: (List<ShopList>?, Exception?) -> Any): ListenerRegister {
        return ListenerRegister(Firestore.shopListCollection.addSnapshotListener { querySnapshot, fbException ->

            if (fbException != null) onSnapshot(null, fbException)
            else querySnapshot?.let {
                val shopLists = arrayListOf<ShopList>()
                shopLists.addAll(querySnapshot.map { it.toObject<ShopList>() })
                onSnapshot(shopLists, null)
            }
        })

    }

    fun validateName(name: String): RequestResult<Boolean> {

        return if (name.isEmpty()) {
            RequestResult.Error(
                Exception(App.getContext().getString(R.string.O_nome_da_lista_precisa_ser_preenchido))
            )
        } else if (name.length < 7) {
            RequestResult.Error(
                Exception(App.getContext().getString(R.string.O_nome_inserido_muito_curto))
            )
        } else RequestResult.Success(true)
    }

    fun removeShopList(shopList: ShopList) {
        Firestore.shopListCollection.document(shopList.id.toString()).delete()
    }

    /**
     * Define um listener no firebase que notifica de altaraçoes locais e na nuvem
     * Lembre-se de remover o listener quando nao for mais necessario para evitar vazamentos de memoria
     * */
    fun observeList(shopListId: Long, onSnapshot: (ShopList?, Exception?) -> Any): ListenerRegister {
        return ListenerRegister(
            Firestore.shopListCollection.whereEqualTo("id", shopListId)
                .addSnapshotListener { querySnapshot, fbException ->

                    if (fbException != null) onSnapshot(null, fbException)
                    else querySnapshot?.let {
                        val shopList = querySnapshot.map { it.toObject<ShopList>() }.firstOrNull()
                        onSnapshot(shopList, null)
                    }
                })

    }

}
