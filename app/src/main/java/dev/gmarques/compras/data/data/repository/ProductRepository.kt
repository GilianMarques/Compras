package dev.gmarques.compras.data.data.repository

import android.util.Log
import com.google.firebase.firestore.toObject
import dev.gmarques.compras.data.data.ListenerRegister
import dev.gmarques.compras.data.data.firestore.Firestore
import dev.gmarques.compras.data.data.model.Product

// TODO: todas as funçoes de repositio devem ser suspensas com coroutines
object ProductRepository {


    /**
     * Define um listener no firebase que notifica de altaraçoes locais e na nuvem
     * Lembre-se de remover o listener quando nao for mais necessario para evitar vazamentos de memoria
     * */
    fun observeProductUpdates(shopListId: Long, onSnapshot: (List<Product>?, Exception?) -> Any): ListenerRegister {
        return ListenerRegister(
            Firestore.productCollection.whereEqualTo("shopListId", shopListId).addSnapshotListener { querySnapshot, fbException ->

                if (fbException != null) onSnapshot(null, fbException)
                else querySnapshot?.let {
                    val products = arrayListOf<Product>()
                    products.addAll(querySnapshot.map { it.toObject<Product>() })
                    onSnapshot(products, null)
                }
            })

    }

    fun addOrUpdateProduct(product: Product) {
        Firestore.productCollection.document(product.id.toString()).set(product.selfValidate())
    }

    /**
     * O produto de lista  a ser modificado e incluido como sugestao no DB
     * Essa função nao faz atualizaçoes apenas inserçoes.
     */
    fun addProductAsSuggestion(product: Product) {
        Firestore.suggestionProductCollection
            .document(product.id.toString())
            .set(product.withNewId().selfValidate())
    }

    /**
     * Atualiza um produto na lista de sugestoes caso ele exista, faz isso considerando o nome do produto.
     *
     * Essa função recebe um objeto atualizado e um objeto antigo que tem seu nome usado para encontrar o produto na lista de sugestões.
     *
     * Nem sempre um produto que está sendo atualizado estará presente na lista de sugestões e por vezes o usuário pode ter alterado
     * o nome do produto, por isso o produto antigo é necessário, usando seu nome a função busca na lista de sugestões
     * por uma de mesmo nome e ao encontrá-la obtém a ID dela e a define no novo produto antes de atualiza-lo no db
     */
    fun updateSuggestionProduct(oldProduct: Product, newProduct: Product) {
        val documentRef = Firestore.suggestionProductCollection.whereEqualTo("name", oldProduct.name)

        documentRef.get().addOnSuccessListener { documentSnapshot ->
            if (!documentSnapshot.isEmpty) {

                val oldProductOnDb = documentSnapshot.documents[0].toObject<Product>()!!
                val newProductWithOldId = newProduct.copy(id = oldProductOnDb.id)

                Firestore.suggestionProductCollection.document(newProductWithOldId.id.toString())
                    .set(newProductWithOldId.selfValidate())

            } else {
                // O documento não existe
                Log.d(
                    "USUK",
                    "ProductRepository.updateSuggestionProductIfExists: sem sugestao de produto pra atualizar no firebase"
                )
            }
        }.addOnFailureListener { exception ->
            // Falha ao acessar o banco
            Log.d("USUK", "ProductRepository.updateSuggestionProductIfExists: Erro obtendo sugestoes de produto do firebase")
        }
    }

    fun removeProduct(product: Product) {
        Firestore.productCollection.document(product.id.toString()).delete()
    }

    fun removeSuggestionProduct(product: Product) {
        Firestore.suggestionProductCollection.document(product.id.toString()).delete()
    }

    fun getProduct(idProduct: Long, callback: (result: Result<Product>) -> Unit) {

        Firestore.productCollection.document(idProduct.toString()).get().addOnSuccessListener { documentSnapshot ->
            val targetProduct = documentSnapshot.toObject<Product>()!!
            callback(Result.success(targetProduct))
        }.addOnFailureListener { exception: java.lang.Exception ->
            callback(Result.failure(exception))
        }
    }

    fun getProductByName(name: String, listId: Long, callback: (Result<Product?>) -> Unit) {
        Firestore.productCollection.whereEqualTo("name", name).whereEqualTo("shopListId", listId).limit(1).get()
            .addOnSuccessListener { snapshot ->

                if (snapshot.isEmpty) callback(Result.success(null))
                else {
                    val targetProduct = snapshot.documents[0].toObject<Product>()
                    callback(Result.success(targetProduct))
                }
            }.addOnFailureListener { exception ->
                callback(Result.failure(exception))
            }


    }
}
