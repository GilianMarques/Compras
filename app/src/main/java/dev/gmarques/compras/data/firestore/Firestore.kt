package dev.gmarques.compras.data.firestore

import com.google.firebase.Firebase
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import dev.gmarques.compras.BuildConfig
import dev.gmarques.compras.data.PreferencesHelper
import dev.gmarques.compras.data.model.DatabaseVersion
import dev.gmarques.compras.data.model.SyncAccount
import dev.gmarques.compras.data.repository.UserRepository
import kotlinx.coroutines.tasks.await

class Firestore {

    companion object {

        const val VERSION = 2

        private var host: String = PreferencesHelper()
            .getValue(PreferencesHelper.PrefsKeys.HOST, "null")

        private val useProductionDb = PreferencesHelper()
            .getValue(PreferencesHelper.PrefsKeys.PRODUCTION_DATABASE, false)

        private val environment =
            if (BuildConfig.DEBUG && !useProductionDb) "debug" else "production"

        private const val USERS = "users"
        private const val DATABASE = "v$VERSION"
        private const val SHOP_LISTS = "shopLists"
        private const val PRODUCTS = "products"
        private const val CATEGORIES = "categories"
        private const val SUGGESTION_PRODUCT = "suggestion_products"
        private const val COLLABORATION = "collaboration"
        private const val SYNC_INVITES = "sync_invites"
        private const val GUESTS = "guests"
        private const val HOST = "host"
        private const val HOST_DATA = "data"
        const val LAST_LOGIN = "last_login"
        private const val DATABASE_VERSION = "database_version"

        suspend fun loadDatabasePaths() {
            if (host != "null") return

            val data = hostCollection.document(HOST_DATA).get().await()
            val host = data.toObject<SyncAccount>()

            this.host = host?.email ?: UserRepository.getUser()!!.email!!
            PreferencesHelper().saveValue(PreferencesHelper.PrefsKeys.HOST, this.host)
        }

        val shopListsCollection by lazy {
            Firebase.firestore.collection(environment).document(USERS).collection(host)
                .document(DATABASE)
                .collection(SHOP_LISTS)
        }

        val categoriesCollection by lazy {
            Firebase.firestore.collection(environment).document(USERS).collection(host)
                .document(DATABASE)
                .collection(CATEGORIES)
        }

        val productsCollection by lazy {
            Firebase.firestore.collection(environment).document(USERS).collection(host)
                .document(DATABASE)
                .collection(PRODUCTS)
        }

        val suggestionProductsCollection by lazy {
            Firebase.firestore.collection(environment).document(USERS).collection(host)
                .document(DATABASE)
                .collection(SUGGESTION_PRODUCT)
        }

        val syncInvitesCollection by lazy {
            Firebase.firestore.collection(environment).document(USERS).collection(host)
                .document(COLLABORATION)
                .collection(SYNC_INVITES)
        }

        private val rootCollection by lazy {
            Firebase.firestore.collection(environment).document(USERS).collection(host)

        }

        val guestsCollection by lazy {
            Firebase.firestore.collection(environment).document(USERS).collection(host)
                .document(COLLABORATION)
                .collection(GUESTS)

        }

        val hostCollection by lazy {
            Firebase.firestore.collection(environment).document(USERS)
                .collection(UserRepository.getUser()!!.email!!)
                .document(COLLABORATION)
                .collection(HOST)
        }

        val hostDocument by lazy {
            Firebase.firestore.collection(environment).document(USERS)
                .collection(UserRepository.getUser()!!.email!!)
                .document(COLLABORATION)
                .collection(HOST)
                .document(HOST_DATA)
        }

        val lastLoginDocument by lazy {
            rootCollection.document(LAST_LOGIN)
        }

        val databaseVersionDocument by lazy {
            rootCollection.document(DATABASE_VERSION)
        }

        /**
         * Retorna o caminho onde ficam os convites para sincronismo no banco de dados do
         * usuario alvo
         * @param targetEmail o endereço de email do banco de dados do usuario que receberá o convite
         * */
        fun findGuestSyncInvitesCollection(targetEmail: String): CollectionReference {
            return Firebase.firestore.collection(environment).document(USERS)
                .collection(targetEmail)
                .document(COLLABORATION)
                .collection(SYNC_INVITES)
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
         * Retorna a coleçao do email alvo onde ficam os convidados
         */
        fun findTargetAccountGuestsCollection(targetEmail: String): CollectionReference {
            return Firebase.firestore.collection(environment).document(USERS)
                .collection(targetEmail).document(COLLABORATION)
                .collection(GUESTS)
        }

        suspend fun getCloudDatabaseVersion(): Int {
            return databaseVersionDocument.get().await()
                ?.toObject<DatabaseVersion>()?.databaseVersion ?: 1

        }
    }
}
