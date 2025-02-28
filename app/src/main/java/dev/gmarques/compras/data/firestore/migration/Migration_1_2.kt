package dev.gmarques.compras.data.firestore.migration

import com.google.firebase.firestore.toObject
import dev.gmarques.compras.data.firestore.Firestore
import dev.gmarques.compras.data.model.Category
import dev.gmarques.compras.data.model.DatabaseVersion
import dev.gmarques.compras.data.model.Product
import dev.gmarques.compras.data.model.ShopList
import kotlinx.coroutines.tasks.await

@Suppress("ClassName")
class Migration_1_2 {

    suspend fun beginMigration() {

        migrateShopLists()
        migrateCategories()
        migrateProducts()
        migrateSuggestionProducts()
        updateDatabaseVersion()
    }

    private suspend fun migrateProducts() {

        Firestore.productsCollection().get().await().forEach {
            val obj = it.toObject<Product>()
            Firestore.productsCollection().document(obj.id).set(obj).await()
        }
    }

    private suspend fun migrateSuggestionProducts() {

        Firestore.suggestionProductsCollection().get().await().forEach {
            val obj = it.toObject<Product>()
            Firestore.suggestionProductsCollection().document(obj.id).set(obj).await()
        }
    }

    private suspend fun migrateShopLists() {
        Firestore.shopListsCollection().get().await().forEach {
            val obj = it.toObject<ShopList>()
            Firestore.shopListsCollection().document(obj.id).set(obj).await()
        }
    }

    private suspend fun migrateCategories() {
        Firestore.categoriesCollection().get().await().forEach {
            val obj = it.toObject<Category>()
            Firestore.categoriesCollection().document(obj.id).set(obj).await()
        }

    }

    private suspend fun updateDatabaseVersion() {
        Firestore.databaseVersionDocument().set(DatabaseVersion()).await()
    }

}
