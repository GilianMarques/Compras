package dev.gmarques.compras.data.data.repository

import com.google.firebase.firestore.toObject
import dev.gmarques.compras.R
import dev.gmarques.compras.data.data.firestore.Firestore
import dev.gmarques.compras.data.data.model.ShopList
import dev.gmarques.compras.utils.App
import dev.gmarques.compras.utils.Result
import java.util.UUID


object ShopListRepository {

    private val fakeShopLists = mutableListOf<ShopList>()

    init {
        repeat(30) {
            //  addList(ShopList(generateUniqueId(), "Lista de compras # $it"))
        }
    }

    fun addList(shopList: ShopList) {
        Firestore.shopListCollection.document(shopList.createdDate.toString()).set(shopList)
    }

    // TODO: se for chamado de mais de um lugar vai gerar listeners duplicados do fb, otimize quando puder.
    fun observeListUpdates(onSnapshot: (List<ShopList>?, Exception?) -> Any) {
        Firestore.shopListCollection.addSnapshotListener { querySnapshot, fbException ->

            if (fbException != null) onSnapshot(null, fbException)
            else querySnapshot?.let {
                val shopLists = arrayListOf<ShopList>()
                shopLists.addAll(querySnapshot.map { it.toObject<ShopList>() })
                onSnapshot(shopLists, null)
            }
        }

    }

    fun removeList(id: String): Boolean {
        return fakeShopLists.removeIf { it.id == id }
    }

    fun updateList(updatedShopList: ShopList): Boolean {
        val index = fakeShopLists.indexOfFirst { it.id == updatedShopList.id }
        return if (index != -1) {
            fakeShopLists[index] = updatedShopList
            true
        } else {
            false
        }
    }

    fun generateUniqueId(): String {
        return UUID.randomUUID().toString()
    }

    fun validateNameAndGenerateList(name: String): Result<ShopList> {

        return if (name.isEmpty()) {
            Result.Error(
                Exception(App.getContext().getString(R.string.O_nome_da_lista_precisa_ser_preenchido))
            )
        } else if (name.length < 7) {
            Result.Error(
                Exception(App.getContext().getString(R.string.O_nome_inserido_muito_curto))
            )
        } else Result.Success(ShopList(name))
    }
}
