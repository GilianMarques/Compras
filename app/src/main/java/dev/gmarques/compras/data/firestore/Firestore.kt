package dev.gmarques.compras.data.firestore

import com.google.firebase.Firebase
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import dev.gmarques.compras.BuildConfig
import dev.gmarques.compras.data.PreferencesHelper
import dev.gmarques.compras.data.model.SyncRequest
import dev.gmarques.compras.data.repository.UserRepository
import kotlinx.coroutines.tasks.await

class Firestore {


    companion object {

        private var keyDatabase: String = PreferencesHelper()
            .getValue(PreferencesHelper.PrefsKeys.HOST, "null")

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
        private const val GUESTS = "Guests"
        private const val HOST = "Host"
        private const val SYNCING_WITH = "Syncing_with"

        suspend fun setupDatabase() {
            if (keyDatabase != "null") return

            val data = hostDocument.get().await()
            val host = data.toObject<SyncRequest>()

            keyDatabase = host?.email ?: UserRepository.getUser()!!.email!!
            PreferencesHelper().saveValue(PreferencesHelper.PrefsKeys.HOST, keyDatabase)
        }


        val shopListCollection by lazy {
            Firebase.firestore.collection(environment).document(USERS).collection(keyDatabase)
                .document(DATABASE)
                .collection(SHOP_LISTS)
        }

        val categoryCollection by lazy {
            Firebase.firestore.collection(environment).document(USERS).collection(keyDatabase)
                .document(DATABASE)
                .collection(CATEGORIES)
        }

        val productCollection by lazy {
            Firebase.firestore.collection(environment).document(USERS).collection(keyDatabase)
                .document(DATABASE)
                .collection(PRODUCTS)
        }

        val suggestionProductCollection by lazy {
            Firebase.firestore.collection(environment).document(USERS).collection(keyDatabase)
                .document(DATABASE)
                .collection(SUGGESTION_PRODUCT)
        }

        val syncRequestsCollection by lazy {
            Firebase.firestore.collection(environment).document(USERS).collection(keyDatabase)
                .document(COLLABORATION)
                .collection(SYNC_REQUESTS)
        }

        val rootCollection by lazy {
            Firebase.firestore.collection(environment).document(USERS).collection(keyDatabase)

        }

        val guestsCollection by lazy {
            Firebase.firestore.collection(environment).document(USERS).collection(keyDatabase).document(COLLABORATION)
                .collection(GUESTS)

        }

        private val hostDocument by lazy {
            Firebase.firestore.collection(environment).document(USERS)
                .collection(UserRepository.getUser()!!.email!!)
                .document(COLLABORATION)
                .collection(HOST)
                .document(SYNCING_WITH)
        }


        /**
         * Retorna o caminho onde ficam as solicitaçoes de sincronismo no banco de dados do
         * usuario alvo
         * @param targetEmail o endereço de email do banco de dados do usuario que receberá o pedido de sincronismo
         * */
        fun findSyncRequestsCollection(targetEmail: String): CollectionReference {
            return Firebase.firestore.collection(environment).document(USERS)
                .collection(targetEmail)
                .document(COLLABORATION)
                .collection(SYNC_REQUESTS)
        }

        /**
         * Retorna o caminho para o banco de dados do usuario alvo
         * @param targetEmail o endereço de email do banco de dados do usuario alvo
         * */
        fun findTargetAccountCollection(targetEmail: String): CollectionReference {
            return Firebase.firestore.collection(environment).document(USERS)
                .collection(targetEmail)
        }

        /**
         * Retorna o caminho para o banco de dados do usuario alvo
         * @param targetEmail o endereço de email do banco de dados do usuario alvo
         * */
        fun findTargetAccountHostdocument(targetEmail: String): DocumentReference {
            return Firebase.firestore.collection(environment).document(USERS)
                .collection(targetEmail).document(COLLABORATION)
                .collection(HOST)
                .document(SYNCING_WITH)
        }
    }
}

/*
* nome
* email
* foto
*
* */