package dev.gmarques.compras.data.firestore

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import dev.gmarques.compras.BuildConfig
import dev.gmarques.compras.data.PreferencesHelper
import dev.gmarques.compras.data.repository.UserRepository

class FirestoreV2 {


    companion object {

        private val useProductionDb = PreferencesHelper()
            .getValue(PreferencesHelper.PrefsKeys.PRODUCTION_DATABASE, false)

        private val environment = if (BuildConfig.DEBUG && !useProductionDb) "debug" else "production"
        private const val USERS = "users"
        private const val DATABASE = "Data"
        private const val SHOP_LISTS = "ShopLists"
        private const val PRODUCTS = "Products"
        private const val CATEGORIES = "Categories"
        private const val SUGGESTION_PRODUCT = "Suggestion_products"
        private val userEmail = UserRepository.getUser()!!.email!!

        val shopListCollection by lazy {
            Firebase.firestore.collection(environment).document(USERS).collection(userEmail)
                .document(DATABASE)
                .collection(SHOP_LISTS)
        }

        val categoryCollection by lazy {
            Firebase.firestore.collection(environment).document(USERS).collection(userEmail)
                .document(DATABASE)
                .collection(CATEGORIES)
        }

        val productCollection by lazy {
            Firebase.firestore.collection(environment).document(USERS).collection(userEmail)
                .document(DATABASE)
                .collection(PRODUCTS)
        }

        val suggestionProductCollection by lazy {
            Firebase.firestore.collection(environment).document(USERS).collection(userEmail)
                .document(DATABASE)
                .collection(SUGGESTION_PRODUCT)
        }
    }
}