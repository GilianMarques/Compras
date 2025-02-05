package dev.gmarques.compras.data.firestore

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.toObject
import dev.gmarques.compras.data.model.Category
import dev.gmarques.compras.data.model.Product
import dev.gmarques.compras.data.model.ShopList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MigrationDatabase {

    init {
        GlobalScope.launch(Dispatchers.IO) {
            migrateSuggestions()
            migrateCategories()
            migrateProducts()
            migrateShopLists()
        }
    }

    private suspend fun migrateSuggestions() {

        val suggestions = Firestore.suggestionProductCollection.get().await()
        suggestions.documents.forEach { doc: DocumentSnapshot ->
            val suggestion = doc.toObject<Product>()
            Firestore.suggestionProductCollection.document(suggestion!!.id).set(suggestion)
            Log.d("USUK", "MigrationDatabase.migrateSuggestions: $suggestion")
        }
    }

    private suspend fun migrateCategories() {

        val suggestions = Firestore.categoryCollection.get().await()
        suggestions.documents.forEach { doc: DocumentSnapshot ->
            val suggestion = doc.toObject<Category>()
            Firestore.categoryCollection.document(suggestion!!.id).set(suggestion)
            Log.d("USUK", "MigrationDatabase.migrateSuggestions: $suggestion")
        }
    }

    private suspend fun migrateProducts() {

        val suggestions = Firestore.productCollection.get().await()
        suggestions.documents.forEach { doc: DocumentSnapshot ->
            val suggestion = doc.toObject<Product>()
            Firestore.productCollection.document(suggestion!!.id).set(suggestion)
            Log.d("USUK", "MigrationDatabase.migrateSuggestions: $suggestion")
        }
    }

    private suspend fun migrateShopLists() {

        val suggestions = Firestore.shopListCollection.get().await()
        suggestions.documents.forEach { doc: DocumentSnapshot ->
            val suggestion = doc.toObject<ShopList>()
            Firestore.shopListCollection.document(suggestion!!.id).set(suggestion)
            Log.d("USUK", "MigrationDatabase.migrateSuggestions: $suggestion")
        }
    }
}