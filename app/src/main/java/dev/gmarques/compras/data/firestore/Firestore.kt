package dev.gmarques.compras.data.firestore

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
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


        private var host = "null"

        var imIGuest = false
            private set

        private val useProductionDb =
            PreferencesHelper().getValue(PreferencesHelper.PrefsKeys.PRODUCTION_DATABASE, false)

        private val environment =
            if (BuildConfig.DEBUG && !useProductionDb) "debug" else "production"

        private const val USERS = "users"
        private const val DATABASE = "v2" // TODO: voltar ao padrao e usar versao nos objetos
        private const val SHOP_LISTS = "shopLists"
        private const val PRODUCTS = "products"
        private const val CATEGORIES = "categories"
        private const val MARKETS = "markets"
        private const val SUGGESTION_PRODUCT = "suggestion_products"
        private const val COLLABORATION = "collaboration"
        private const val SYNC_INVITES = "sync_invites"
        private const val GUESTS = "guests"
        private const val HOST = "host"
        private const val HOST_DATA = "data"
        const val LAST_LOGIN = "last_login"
        private const val DATABASE_VERSION = "database_version"

        /**
         * Define o caminho base onde o App deve ler e escrever dados.
         *
         * @return o email do host para que os dados sejam clonados antes de interromper o sincronismo, caso o usuario local
         * seja um convidado do host que foi desconectado por ele (host) ou nulo, caso contrário.
         * */
        suspend fun loadDatabasePaths(): String? {

            if (host != "null") return null
                .also {
                    Log.d(
                        "USUK",
                        "Firestore.loadDatabasePaths: Nao se deve alterar o caminho do servidor, uma vez que definido. valor atual: '$host'"
                    )
                }

            val localUserEmail = UserRepository.getUser()!!.email!!

            // verifico se o usuario local é um convidado de outro usuario ao checar se tem um host definido na conta dele
            val hostAccount = hostDocument().get().await().toObject<SyncAccount>()

            if (hostAccount?.email == null) {
                this.host = localUserEmail
                Log.d("USUK", "Firestore.loadDatabasePaths: host: $host")
                return null

            } else {

                //Caso tenha, verifico se o host nao interrompeu o sincronismo com o convidado
                imIGuest = guestsCollection(hostAccount.email)
                    .document(localUserEmail).get().await()
                    .exists()


                this.host = if (imIGuest) hostAccount.email else localUserEmail

                return if (imIGuest) null else hostAccount.email

            }

        }

        fun shopListsCollection(targetEmail: String = host): CollectionReference {
            return rootCollection(targetEmail)
                .document(DATABASE)
                .collection(SHOP_LISTS)
        }

        fun categoriesCollection(targetEmail: String = host): CollectionReference {
            return rootCollection(targetEmail)
                .document(DATABASE)
                .collection(CATEGORIES)
        }

        fun marketsCollection(targetEmail: String = host): CollectionReference {
            return rootCollection(targetEmail)
                .document(DATABASE)
                .collection(MARKETS)
        }

        fun productsCollection(targetEmail: String = host): CollectionReference {
            return rootCollection(targetEmail)
                .document(DATABASE)
                .collection(PRODUCTS)
        }

        fun suggestionProductsCollection(targetEmail: String = host): CollectionReference {
            return rootCollection(targetEmail).document(DATABASE)
                .collection(SUGGESTION_PRODUCT)
        }

        /**
         * Retorna o caminho onde ficam os convites para sincronismo no banco de dados do
         * usuario alvo
         * @param targetEmail o endereço de email do banco de dados do usuario que receberá o convite
         * */
        fun syncInvitesCollection(targetEmail: String = host): CollectionReference {
            return Firebase.firestore.collection(environment).document(USERS)
                .collection(targetEmail).document(COLLABORATION)
                .collection(SYNC_INVITES)
        }

        /**
         * Retorna o caminho para o banco de dados do usuario alvo
         * @param targetEmail o endereço de email do banco de dados do usuario alvo
         * */
        fun rootCollection(targetEmail: String = host): CollectionReference {
            return Firebase.firestore.collection(environment)
                .document(USERS)
                .collection(targetEmail)

        }

        /**
         * Retorna a coleçao do email alvo onde ficam os convidados
         */
        fun guestsCollection(targetEmail: String = host): CollectionReference {
            return rootCollection(targetEmail)
                .document(COLLABORATION)
                .collection(GUESTS)

        }

        /**
         * Referencia a coleçao onde fica o documento contendo o email do anfitriao do qual o ususario local é convidado.
         * Essa referencia a coleçao é necessaria para que o codigo na tela de Perfil do usuario possa usa-la para
         * observar alterações no documento de anfitriao.
         */
        fun hostCollection(): CollectionReference {
            return rootCollection(UserRepository.getUser()!!.email!!)
                .document(COLLABORATION)
                .collection(HOST)
        }

        /**
         * Referencia ao documento onde fica o email do anfitriao do qual o ususario local é convidado
         */
        fun hostDocument(): DocumentReference {
            return hostCollection()
                .document(HOST_DATA)
        }

        fun lastLoginDocument(targetEmail: String = host): DocumentReference {
            return rootCollection(targetEmail).document(LAST_LOGIN)
        }

        fun databaseVersionDocument(targetEmail: String = host): DocumentReference {
            return rootCollection(targetEmail).document(DATABASE_VERSION)
        }

        suspend fun getCloudDatabaseVersion(): Int {
            return databaseVersionDocument().get().await()
                ?.toObject<DatabaseVersion>()?.databaseVersion ?: 1

        }

    }
}
