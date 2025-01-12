package dev.gmarques.compras.data.firestore

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import dev.gmarques.compras.BuildConfig
import dev.gmarques.compras.data.PreferencesHelper
import dev.gmarques.compras.data.repository.UserRepository

class Firestore {

    companion object {

        private val userRootPath = if (BuildConfig.DEBUG) {
            val useProductionDb = PreferencesHelper().getValue(PreferencesHelper.PrefsKeys.PRODUCTION_DATABASE, false)
            if (useProductionDb) UserRepository.getUser()!!.email!! else "debug_database"
        } else UserRepository.getUser()!!.email!!

        private const val DATABASE = "Database"
        private const val SHOP_LISTS = "ShopLists"
        private const val PRODUCTS = "Products"
        private const val CATEGORIES = "Categories"
        private const val SUGGESTION_PRODUCT = "Suggestion_products"

        val shopListCollection by lazy {
            Firebase.firestore.collection(userRootPath).document(DATABASE).collection(SHOP_LISTS)
        }

        val categoryCollection by lazy {
            Firebase.firestore.collection(userRootPath).document(DATABASE).collection(CATEGORIES)
        }

        val productCollection by lazy {
            Firebase.firestore.collection(userRootPath).document(DATABASE).collection(PRODUCTS)
        }

        val suggestionProductCollection by lazy {
            Firebase.firestore.collection(userRootPath).document(DATABASE).collection(SUGGESTION_PRODUCT)
        }
    }
}