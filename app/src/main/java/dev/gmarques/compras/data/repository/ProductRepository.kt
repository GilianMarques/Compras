package dev.gmarques.compras.data.repository

import android.util.Log
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.toObject
import dev.gmarques.compras.data.firestore.Firestore
import dev.gmarques.compras.data.model.Product
import dev.gmarques.compras.utils.ListenerRegister
import kotlinx.coroutines.tasks.await

// TODO: todas as funçoes de repository  devem ser suspensas com coroutines e usar await. receber um validatedProduct
object ProductRepository {


    /**
     * Define um listener no firebase que notifica de altaraçoes locais e na nuvem
     * Lembre-se de remover o listener quando nao for mais necessario para evitar vazamentos de memoria
     * */
    fun observeProductUpdates(
        shopListId: String,
        onSnapshot: (List<Product>?, Exception?) -> Any,
    ): ListenerRegister {
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


    suspend fun getProducts(shopListId: String): List<String> {
        val querySnapshot: QuerySnapshot = Firestore.productCollection.whereEqualTo("shopListId", shopListId).get().await()
        return querySnapshot.map { it.toObject<Product>().name }
    }

    /**
     * Define um listener no firebase que notifica de altaraçoes locais e na nuvem
     * Lembre-se de remover o listener quando nao for mais necessario para evitar vazamentos de memoria
     * */
    fun observeSuggestionProductUpdates(
        onSnapshot: (List<Product>?, Exception?) -> Any,
    ): ListenerRegister {
        return ListenerRegister(
            Firestore.suggestionProductCollection.addSnapshotListener { querySnapshot, fbException ->

                if (fbException != null) onSnapshot(null, fbException)
                else querySnapshot?.let {
                    val products = arrayListOf<Product>()
                    products.addAll(querySnapshot.map { it.toObject<Product>() })
                    onSnapshot(products, null)
                }
            })

    }

    fun addOrUpdateProduct(product: Product) {
        Firestore.productCollection.document(product.id).set(product.selfValidate())
    }

    /**
     * Verifica se a sugestão existe no banco de dados com base em seu nome, e a atualize.
     * Caso não exista, a sugestão é adicionada ao banco de dados.
     * */
    suspend fun updateOrAddProductAsSuggestion(product: Product) {

        val querySnapshot = Firestore.suggestionProductCollection.whereEqualTo("name", product.name).limit(1).get().await()

        val suggestionProduct = if (querySnapshot.isEmpty) product.withNewId().selfValidate()
        else {
            val oldProduct = querySnapshot.documents[0].toObject<Product>()!!
            product.copy(id = oldProduct.id)
        }

        Firestore.suggestionProductCollection
            .document(suggestionProduct.id)
            .set(suggestionProduct)
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

                Firestore.suggestionProductCollection.document(newProductWithOldId.id)
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
            Log.d(
                "USUK",
                "ProductRepository.updateSuggestionProductIfExists: Erro obtendo sugestoes de produto do firebase $exception"
            )
        }
    }

    fun removeProduct(product: Product) {
        Firestore.productCollection.document(product.id).delete()
    }

    fun removeSuggestionProduct(product: Product) {
        Firestore.suggestionProductCollection.document(product.id).delete()
    }

    fun getProduct(idProduct: String, callback: (result: Result<Product>) -> Unit) {

        Firestore.productCollection.document(idProduct).get().addOnSuccessListener { documentSnapshot ->
            val targetProduct = documentSnapshot.toObject<Product>()!!
            callback(Result.success(targetProduct))
        }.addOnFailureListener { exception: java.lang.Exception ->
            callback(Result.failure(exception))
        }
    }

    /**
     * Verifica se há ao menos um produto ou sugestão de produto associado ao ID de categoria fornecido.
     *
     * A função consulta duas coleções no Firestore (`productCollection` e `suggestionProductCollection`),
     * retornando `true` na primeira correspondência encontrada. Caso contrário, retorna `false`.
     *
     * @param categoryId ID da categoria a ser verificada.
     * @param callback Callback que retorna um [Result] com `true` se algum documento for encontrado,
     *                 `false` se nenhum documento for encontrado, ou uma exceção em caso de erro.
     */
    fun hasAnyProductWithCategoryId(categoryId: String, callback: (result: Result<Boolean>) -> Unit) {

        // Função auxiliar para verificar a coleção de sugestões de produtos
        fun checkSuggestions() {
            Firestore.suggestionProductCollection
                .whereEqualTo("categoryId", categoryId)
                .limit(1)
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    callback(Result.success(!documentSnapshot.isEmpty))
                }
                .addOnFailureListener { exception ->
                    callback(Result.failure(exception))
                }
        }

        // Verifica a coleção de produtos
        Firestore.productCollection
            .whereEqualTo("categoryId", categoryId)
            .limit(1)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (!documentSnapshot.isEmpty) {
                    callback(Result.success(true))
                } else {
                    checkSuggestions() // Caso não encontre, verifica na coleção de sugestões
                }
            }
            .addOnFailureListener { exception ->
                callback(Result.failure(exception))
            }
    }

    fun getProductByName(name: String, listId: String, callback: (Result<Product?>) -> Unit) {
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

    suspend fun getSuggestions(): List<Product> {
        val querySnapshot = Firestore.suggestionProductCollection.get().await()
        return querySnapshot.map { it.toObject<Product>() }
    }

    suspend fun removeAllProductsFromList(shopListId: String) {
        val querySnapshot = Firestore.productCollection.whereEqualTo("shopListId", shopListId).get().await()

        for (document in querySnapshot.documents) {
            document.reference.delete()
            Log.d("USUK", "ProductRepository.removeAllProductsFromList: removendo: ${document.get("name")}")
        }
    }
}
