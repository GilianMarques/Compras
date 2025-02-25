package dev.gmarques.compras.data.firestore

import com.google.firebase.firestore.toObject
import dev.gmarques.compras.data.model.Category
import dev.gmarques.compras.data.model.DatabaseVersion
import dev.gmarques.compras.data.model.Product
import dev.gmarques.compras.data.model.ShopList
import kotlinx.coroutines.tasks.await

/**
 * Essa classe faz uma copia do banco de dados de um determinado usuario.
 * Foi criada com o proposito de clonar o banco de dados de um usuario apos uma conexao de contas ser encerrada, garantindo
 * que ambos os usuarios que antes compartilhavam o mesmo DB fiquem com uma copia atualizada
 * @param baseEmail o email DE onde serao copiados os dados
 * @param targetEmail o email PARA onde serao copiados os dados
 * */
class FirebaseCloneDatabase(private val baseEmail: String, private val targetEmail: String) {

    suspend fun beginCloning() {
        cleanDatabase()
        cloneShopLists()
        cloneCategories()
        cloneProducts()
        cloneSuggestionProducts()
        removeHostData()
    }


    /**
     * É necessário limpar o banco de dados do usuario local antes da clonagem para evitar dados duplicados, conflitos,
     * ou até mesmo erros por conta de dados antigos no database que nao foram migrados com o passar do tempo
     * */
    private suspend fun cleanDatabase() {

        val collectionsToClean = listOf(
            Firestore.shopListsCollection(targetEmail),
            Firestore.categoriesCollection(targetEmail),
            Firestore.productsCollection(targetEmail),
            Firestore.suggestionProductsCollection(targetEmail)
        )

        collectionsToClean.forEach {
            it.get().await().forEach { targetDoc ->
                targetDoc.reference.delete().await()
            }

        }
    }


    private suspend fun cloneProducts() {

        Firestore.productsCollection(baseEmail).get().await().forEach {
            val obj = it.toObject<Product>()
            Firestore.productsCollection(targetEmail).document(obj.id).set(obj).await()
        }
    }

    private suspend fun cloneSuggestionProducts() {

        Firestore.suggestionProductsCollection(baseEmail).get().await().forEach {
            val obj = it.toObject<Product>()
            Firestore.suggestionProductsCollection(targetEmail).document(obj.id).set(obj).await()
        }
    }

    private suspend fun cloneShopLists() {
        Firestore.shopListsCollection(baseEmail).get().await().forEach {
            val obj = it.toObject<ShopList>()
            Firestore.shopListsCollection(targetEmail).document(obj.id).set(obj).await()
        }
    }

    private suspend fun cloneCategories() {
        Firestore.categoriesCollection(baseEmail).get().await().forEach {
            val obj = it.toObject<Category>()
            Firestore.categoriesCollection(targetEmail).document(obj.id).set(obj).await()
        }

    }

    /**
     * Remove do banco de dados do usuario local o registro do host para que a conexao seja cortada de vez
     * */
    private suspend fun removeHostData() {
        Firestore.hostDocument().delete().await()
    }

}