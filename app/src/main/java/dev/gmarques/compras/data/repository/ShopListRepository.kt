package dev.gmarques.compras.data.repository

import com.google.firebase.firestore.toObject
import dev.gmarques.compras.data.firestore.Firestore
import dev.gmarques.compras.data.model.Category
import dev.gmarques.compras.data.model.ShopList
import dev.gmarques.compras.data.repository.model.ValidatedShopList
import dev.gmarques.compras.domain.utils.ListenerRegister
import kotlinx.coroutines.tasks.await


object ShopListRepository {

    fun addOrUpdateShopList(validatedShopList: ValidatedShopList) {
        val shopList = validatedShopList.shopList
        Firestore.shopListsCollection().document(shopList.id)
            .set(shopList)
    }

    /**
     * Define um listener no firebase que notifica de altaraçoes locais e na nuvem
     * Lembre-se de remover o listener quando nao for mais necessario para evitar vazamentos de memoria
     * */
    fun observeShopListsUpdates(onSnapshot: (List<ShopList>?, Exception?) -> Any): ListenerRegister {
        return ListenerRegister(Firestore.shopListsCollection().addSnapshotListener { querySnapshot, fbException ->

            if (fbException != null) onSnapshot(null, fbException)
            else querySnapshot?.let {
                val shopLists = arrayListOf<ShopList>()
                shopLists.addAll(querySnapshot.map { it.toObject<ShopList>() })
                onSnapshot(shopLists, null)
            }
        })

    }

    fun removeShopList(shopList: ShopList) {
        Firestore.shopListsCollection().document(shopList.id).delete()
    }

    /**
     * Define um listener no firebase que notifica de altaraçoes locais e na nuvem
     * Lembre-se de remover o listener quando nao for mais necessario para evitar vazamentos de memoria
     * */
    fun observeShopList(
        shopListId: String,
        onSnapshot: (ShopList?, Exception?) -> Any,
    ): ListenerRegister {
        return ListenerRegister(
            Firestore.shopListsCollection().whereEqualTo("id", shopListId)
                .addSnapshotListener { querySnapshot, fbException ->

                    if (fbException != null) onSnapshot(null, fbException)
                    else querySnapshot?.let {
                        val shopList = querySnapshot.map { it.toObject<ShopList>() }.firstOrNull()
                        onSnapshot(shopList, null)
                    }
                })

    }


    suspend fun getShopListByName(name: String): Result<Category?> {
        val querySnapshot =
            Firestore.shopListsCollection().whereEqualTo("name", name).limit(1).get().await()

        return if (!querySnapshot.isEmpty) {
            val targetCategory = querySnapshot.documents[0].toObject<Category>()
            Result.success(targetCategory)
        } else Result.success(null)
    }


    suspend fun getShopList(id: String): Result<ShopList> {
        val querySnapshot = Firestore.shopListsCollection().document(id).get().await()

        val targetShopList = querySnapshot.toObject<ShopList>()!!
        return Result.success(targetShopList)
    }
}
