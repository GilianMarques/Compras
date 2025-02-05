package dev.gmarques.compras.data.firestore

import com.google.firebase.Firebase
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.firestore
import dev.gmarques.compras.BuildConfig
import dev.gmarques.compras.data.PreferencesHelper
import dev.gmarques.compras.data.repository.UserRepository

class Firestore {


    companion object {

        private val useProductionDb = PreferencesHelper()
            .getValue(PreferencesHelper.PrefsKeys.PRODUCTION_DATABASE, false)

        private val environment =
            if (BuildConfig.DEBUG && !useProductionDb) "debug" else "production"
        private const val USERS = "users"
        private const val DATABASE = "Data"
        private const val SHOP_LISTS = "ShopLists"
        private const val PRODUCTS = "Products"
        private const val CATEGORIES = "Categories"
        private const val SUGGESTION_PRODUCT = "Suggestion_products"
        private const val COLLABORATION = "Collaboration"
        private const val SYNC_REQUESTS = "Sync_requests"
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

        val syncRequestsCollection by lazy {
            Firebase.firestore.collection(environment).document(USERS).collection(userEmail)
                .document(COLLABORATION)
                .collection(SYNC_REQUESTS)
        }

        val rootCollection by lazy {
            Firebase.firestore.collection(environment).document(USERS).collection(userEmail)

        }


        /**
         * Retorna o caminho onde ficam as solicitaçoes de sincronismo no banco de dados do
         * usuario alvo
         * @param targetEmail o endereço de email do banco de dados do usuario que receberá o pedido de sincronismo
         * */
        fun syncRequestsCollection(targetEmail: String): CollectionReference {
            return Firebase.firestore.collection(environment).document(USERS)
                .collection(targetEmail)
                .document(COLLABORATION)
                .collection(SYNC_REQUESTS)
        }

        /**
         * Retorna o caminho para o banco de dados do usuario alvo
         * @param targetEmail o endereço de email do banco de dados do usuario alvo
         * */
        fun targetAccountCollection(targetEmail: String): CollectionReference {
            return Firebase.firestore.collection(environment).document(USERS)
                .collection(targetEmail)
        }
    }
}

/*
* nome
* email
* foto
*
* */