package dev.gmarques.compras.data.repository

import com.google.firebase.firestore.toObject
import dev.gmarques.compras.data.firestore.Firestore
import dev.gmarques.compras.data.model.ShopList
import dev.gmarques.compras.domain.utils.ListenerRegister


object ShopListRepository {

    fun addOrAttShopList(shopList: ShopList) {
        Firestore.shopListCollection.document(shopList.id)
            .set(shopList.selfValidate())
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

    fun removeShopList(shopList: ShopList) {
        Firestore.shopListCollection.document(shopList.id).delete()
    }

    /**
     * Define um listener no firebase que notifica de altaraçoes locais e na nuvem
     * Lembre-se de remover o listener quando nao for mais necessario para evitar vazamentos de memoria
     * */
    fun observeList(shopListId: String, onSnapshot: (ShopList?, Exception?) -> Any): ListenerRegister {
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
