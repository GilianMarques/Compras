package dev.gmarques.compras.data.data.repository

import com.google.firebase.firestore.toObject
import dev.gmarques.compras.R
import dev.gmarques.compras.data.data.ListenerRegister
import dev.gmarques.compras.data.data.firestore.Firestore
import dev.gmarques.compras.data.data.model.Product
import dev.gmarques.compras.utils.App
import dev.gmarques.compras.utils.Result


object ProductRepository {

    fun addOrAttProduct(product: Product) {
        Firestore.productCollection.document(product.id.toString())
            .set(product)
    }

    /**
     * Define um listener no firebase que notifica de altaraçoes locais e na nuvem
     * Lembre-se de remover o listener quando nao for mais necessario para evitar vazamentos de memoria
     * */
    fun observeProductUpdates(shopListId: Long, onSnapshot: (List<Product>?, Exception?) -> Any): ListenerRegister {
        return ListenerRegister(
            Firestore.productCollection.whereEqualTo("shopListId", shopListId)
                .addSnapshotListener { querySnapshot, fbException ->

                    if (fbException != null) onSnapshot(null, fbException)
                    else querySnapshot?.let {
                        val products = arrayListOf<Product>()
                        products.addAll(querySnapshot.map { it.toObject<Product>() })
                        onSnapshot(products, null)
                    }
                })

    }

    fun validateProductName(name: String): Result<Boolean> {

        return if (name.isEmpty()) {
            Result.Error(
                Exception(App.getContext().getString(R.string.O_nome_da_lista_precisa_ser_preenchido))
            )
        } else if (name.length < 3) {
            Result.Error(
                Exception(App.getContext().getString(R.string.O_nome_inserido_muito_curto))
            )
        } else Result.Success(true)
    }

    fun removeProduct(product: Product) {
        Firestore.productCollection.document(product.id.toString()).delete()
    }


}
