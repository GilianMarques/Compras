package dev.gmarques.compras.data.firestore

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import dev.gmarques.compras.BuildConfig
import dev.gmarques.compras.data.PreferencesHelper
import dev.gmarques.compras.data.model.SyncAccount
import dev.gmarques.compras.data.repository.UserRepository
import kotlinx.coroutines.tasks.await

class Firestore {

    companion object {

        var amIaGuest = false
            private set

        private val useProductionDb = PreferencesHelper().getValue(PreferencesHelper.PrefsKeys.PRODUCTION_DATABASE, false)
        private val environment = if (BuildConfig.DEBUG && !useProductionDb) "debug" else "production"
        private val localUserEmail = UserRepository.getUser()!!.email!!

        /**
         * Contem o email usado no caminho para os dados compartilhados (produtos, listas, categorias, etc...)
         * pode ser o email do usuario local ou o do anfitrao caso o local seja convidado de outra conta
         */
        lateinit var dataHostEmail: String
            private set

        private const val USERS = "users"
        private const val USER_DATA = "user_data"
        private const val SHOP_LISTS = "shopLists"
        private const val PRODUCTS = "products"
        private const val CATEGORIES = "categories"
        private const val ESTABLISHMENTS = "establishments"
        private const val SUGGESTION_PRODUCTS = "suggestion_products"
        private const val COLLABORATION = "collaboration"
        private const val SYNC_INVITES = "sync_invites"
        private const val GUESTS = "guests"
        private const val HOST = "host"
        private const val HOST_DATA = "host_data"
        const val LAST_ACCESS = "last_access"

        /**
         * Define o caminho base onde o App deve ler e escrever dados compartilhaveis com base na condição de convidado do usuario
         * local.
         * @return o email usado pra acessar os dados compartilhaveis, pode ser o do usuario local ou de um anfitriao
         * */
        suspend fun setupDatabaseHost(): String {

            // verifico se o usuario local é um convidado de outro usuario ao checar se tem um host definido na conta dele
            val hostAccount = hostDocument().get().await().toObject<SyncAccount>()

            if (hostAccount?.email == null) {
                this.dataHostEmail = localUserEmail
                amIaGuest = false
            } else {
                this.dataHostEmail = hostAccount.email
                amIaGuest = true
            }
            Log.d("USUK", "Firestore.".plus("setupDatabaseHost() dataHostEmail= $dataHostEmail "))
            return dataHostEmail
        }

        /**
         * Verifica se o usuario foi desconectado pelo anfitriao de um sincronismo entre contas.
         * Se o usuario não é um convidado de outra conta, retorna false imediatamente.
         * @return true se o usuario foi desconectado pelo anfitriao, senao false
         */
        suspend fun wasLocalUserDisconnectedFromHost(): Boolean {
            if (!amIaGuest) return false
            // verifico no db do anfitriao se existe algum convidado com o email do usuario local
            return !guestsCollection(dataHostEmail)
                .document(localUserEmail).get().await()
                .exists()
        }

        // Raiz do banco de dados

        /**
         * Retorna o caminho para o banco de dados do usuario alvo
         * @param targetEmail o endereço de email do banco de dados do usuario alvo
         * */
        fun rootCollection(targetEmail: String = localUserEmail): CollectionReference {

            return Firebase.firestore.collection(environment)
                .document(USERS)
                .collection(targetEmail)
        }

        // dados compartilhaveis
        /**
         * Por padrao, retorna a coleçao de dados do usuario local. Se o local for convidado de outra conta, o caminho
         * retornado levara aos dados do anfitriao. Caso o parametro seja preenchido, o camihno levara aos dados
         * da conta relacionada ao email emquestao.
         *
         * @param targetEmail o endereço de email do banco de dados do usuario alvo. Deixe em branco para retornar os
         * dados do usuario local ou de seu anfitriao caso haja algum
         */
        fun shopListsCollection(targetEmail: String = this.dataHostEmail): CollectionReference {
            return rootCollection(targetEmail)
                .document(USER_DATA)
                .collection(SHOP_LISTS)
        }

        /**
         * Por padrao, retorna a coleçao de dados do usuario local. Se o local for convidado de outra conta, o caminho
         * retornado levara aos dados do anfitriao. Caso o parametro seja preenchido, o camihno levara aos dados
         * da conta relacionada ao email emquestao.
         *
         * @param targetEmail o endereço de email do banco de dados do usuario alvo. Deixe em branco para retornar os
         * dados do usuario local ou de seu anfitriao caso haja algum
         */
        fun categoriesCollection(targetEmail: String = this.dataHostEmail): CollectionReference {
            return rootCollection(targetEmail)
                .document(USER_DATA)
                .collection(CATEGORIES)
        }

        /**
         * Por padrao, retorna a coleçao de dados do usuario local. Se o local for convidado de outra conta, o caminho
         * retornado levara aos dados do anfitriao. Caso o parametro seja preenchido, o camihno levara aos dados
         * da conta relacionada ao email emquestao.
         *
         * @param targetEmail o endereço de email do banco de dados do usuario alvo. Deixe em branco para retornar os
         * dados do usuario local ou de seu anfitriao caso haja algum
         */
        fun establishmentsCollection(targetEmail: String = this.dataHostEmail): CollectionReference {
            return rootCollection(targetEmail)
                .document(USER_DATA)
                .collection(ESTABLISHMENTS)
        }

        /**
         * Por padrao, retorna a coleçao de dados do usuario local. Se o local for convidado de outra conta, o caminho
         * retornado levara aos dados do anfitriao. Caso o parametro seja preenchido, o camihno levara aos dados
         * da conta relacionada ao email emquestao.
         *
         * @param targetEmail o endereço de email do banco de dados do usuario alvo. Deixe em branco para retornar os
         * dados do usuario local ou de seu anfitriao caso haja algum
         */
        fun productsCollection(targetEmail: String = this.dataHostEmail): CollectionReference {
            return rootCollection(targetEmail)
                .document(USER_DATA)
                .collection(PRODUCTS)
        }

        /**
         * Por padrao, retorna a coleçao de dados do usuario local. Se o local for convidado de outra conta, o caminho
         * retornado levara aos dados do anfitriao. Caso o parametro seja preenchido, o camihno levara aos dados
         * da conta relacionada ao email emquestao.
         *
         * @param targetEmail o endereço de email do banco de dados do usuario alvo. Deixe em branco para retornar os
         * dados do usuario local ou de seu anfitriao caso haja algum
         */
        fun suggestionProductsCollection(targetEmail: String = this.dataHostEmail): CollectionReference {
            return rootCollection(targetEmail).document(USER_DATA)
                .collection(SUGGESTION_PRODUCTS)
        }

        // dados de colaboração

        /**
         * Retorna o caminho onde ficam os convites para sincronismo no banco de dados do
         * usuario local, caso o parametro nao seja fornecido.
         * @param targetEmail o endereço de email do banco de dados do usuario que receberá o convite.
         * */
        fun syncInvitesCollection(targetEmail: String = this.localUserEmail): CollectionReference {
            return Firebase.firestore.collection(environment).document(USERS)
                .collection(targetEmail).document(COLLABORATION)
                .collection(SYNC_INVITES)
        }

        /**
         * Retorna a coleçao onde ficam os convidados do usuario local, caso o parametro nao seja fornecido.
         * @param targetEmail o endereço de email do usuario alvo
         */
        fun guestsCollection(targetEmail: String = this.localUserEmail): CollectionReference {
            return rootCollection(targetEmail)
                .document(COLLABORATION)
                .collection(GUESTS)

        }

        /**
         * Referencia da coleçao onde fica o documento contendo o email do anfitriao do qual o usuario local é convidado.
         * Essa referencia a coleçao é necessaria para que o codigo na tela de Perfil do usuario possa usa-la para
         * observar alterações no documento de anfitriao.
         */
        fun hostCollection(): CollectionReference {
            return rootCollection()
                .document(COLLABORATION)
                .collection(HOST)
        }

        /**
         * Referencia ao documento no db do usuario local, onde ficam os dados do anfitriao do qual o usuario local é convidado.
         */
        fun hostDocument(): DocumentReference {
            return hostCollection()
                .document(HOST_DATA)
        }

        // dados privados do usuario

        fun lastAccessDocument(): DocumentReference {
            return rootCollection().document(LAST_ACCESS)
        }

    }
}
