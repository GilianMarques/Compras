package dev.gmarques.compras.data.repository

import android.content.Context
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.toObject
import dev.gmarques.compras.R
import dev.gmarques.compras.data.firestore.Firestore
import dev.gmarques.compras.data.model.SyncRequest
import dev.gmarques.compras.domain.utils.ListenerRegister
import kotlinx.coroutines.tasks.await

/**
 * Encapsula as chamadas ao Firebase Auth
 * */
object UserRepository {

    fun getUser() = FirebaseAuth.getInstance().currentUser

    fun logOff(activity: Context, onResult: (error: Exception?) -> Unit) {

        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(activity.getString(R.string.Google_auth_web_client_id)).requestEmail()
            .build()

        GoogleSignIn.getClient(activity, signInOptions).signOut().addOnSuccessListener {
            FirebaseAuth.getInstance().signOut()
            onResult(null)
        }.addOnFailureListener {
            onResult(it)
        }
    }

    /**
     * Salva no banco de dados do usuario alvo uma solicitação de sincronismo os dados do usuario atual
     * @param email do usuario que recebera o convite
     * @throws IllegalStateException se o usuario alvo nao existir no DB
     * */
    suspend fun sendSyncRequest(email: String): Boolean {

        if (!checkIfUserExists(email)) throw IllegalStateException("O usuario alvo nao existe, é necessario verificar isso quando o usuario (local) insere o email (do alvo) na interface")

        try {
            val myUser = getUser()!!
            Firestore.findSyncRequestsCollection(email).document(myUser.email!!)
                .set(SyncRequest(myUser.displayName!!, myUser.email!!, myUser.photoUrl.toString()))
                .await()
            return true
        } catch (e: Exception) {
            return false
            Log.d("USUK", "UserRepository.sendSyncRequest: ${e.message}")
        }
    }

    /**
     * Verifica se um usuario esta registrado no app ao verificar se no caminho do banco de dados dele
     * existe alguma informaçao salva
     * @return true se o usuario existe no banco de dados, senao, false
     */
    suspend fun checkIfUserExists(targetEmail: String): Boolean {
        return !Firestore.findTargetAccountCollection(targetEmail).limit(1).get().await().isEmpty
    }

    /**
     * Salva dados na raiz do banco do usuario para permitir que ele seja descoberto no caso de algum outro
     * usuario querer enviar um syncrequest
     * */
    fun initUserDatabase() {
        Firestore.rootCollection.document("metadata").set(Metadata())

    }

    fun observeSyncRequests(callback: (MutableList<SyncRequest>) -> Any): ListenerRegister {
        val listenerRegistration = Firestore.syncRequestsCollection
            .addSnapshotListener { querySnapshot: QuerySnapshot?, _: FirebaseFirestoreException? ->

                val requests = mutableListOf<SyncRequest>()

                querySnapshot?.documents?.forEach { snap ->
                    requests.add(snap.toObject<SyncRequest>()!!)
                }
                callback(requests)
            }

        return ListenerRegister(listenerRegistration)
    }

    fun acceptRequest(request: SyncRequest): Boolean {

        return try {
            // salvo os dados do convidado na lista de convidados do usuario local
            Firestore.guestsCollection.document(request.email).set(request)

            // salvo os dados do usuario local na seção de anfitrao do solicitante
            val myUser = getUser()!!
            Firestore.findTargetAccountHostdocument(request.email)
                .set(SyncRequest(myUser.displayName!!, myUser.email!!, myUser.photoUrl.toString()))

            // por fim, removo a solicitação de sincronismo do convidado
            Firestore.syncRequestsCollection.document(request.email).delete()
            true
        } catch (e: Exception) {
            Log.d("USUK", "UserRepository.acceptRequest:Erro aceitando solicitação de sincronismo: $e")
            false
        }
    }

    fun declineRequest(request: SyncRequest): Boolean {
        return try {

            // por fim, removo a solicitação de sincronismo do convidado
            Firestore.syncRequestsCollection.document(request.email).delete()
            true
        } catch (e: Exception) {
            Log.d("USUK", "UserRepository.declineRequest:Erro recusando solicitação de sincronismo: $e")
            false
        }
    }

    data class Metadata(val lastLogin: Long = System.currentTimeMillis())
}