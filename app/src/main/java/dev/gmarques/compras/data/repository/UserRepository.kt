package dev.gmarques.compras.data.repository

import android.content.Context
import android.util.Log
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.toObject
import dev.gmarques.compras.App
import dev.gmarques.compras.R
import dev.gmarques.compras.data.PreferencesHelper
import dev.gmarques.compras.data.firestore.Firestore
import dev.gmarques.compras.data.model.LastLogin
import dev.gmarques.compras.data.model.SyncAccount
import dev.gmarques.compras.domain.utils.ListenerRegister
import kotlinx.coroutines.tasks.await

/**
 * Encapsula as chamadas ao Firebase Auth
 * */
object UserRepository {

    fun getUser() = FirebaseAuth.getInstance().currentUser

    suspend fun logOff(activity: Context, onResult: (error: Exception?) -> Unit) {

        try {
            AuthUI.getInstance().signOut(activity).await()
            FirebaseAuth.getInstance().signOut()
            onResult(null)
        } catch (e: Exception) {
            onResult(e)
        }
    }

    /**
     * Verifica se um usuario esta registrado no app ao verificar se no caminho do banco de dados dele
     * existe alguma informaçao salva
     * @return true se o usuario existe no banco de dados, senao, false
     */
    suspend fun checkIfUserExists(targetEmail: String): Boolean {
        return Firestore.rootCollection(targetEmail)
            .document(Firestore.LAST_LOGIN)
            .get()
            .await().exists()
    }

    /**
     * Salva dados na raiz do banco do usuario para permitir que ele seja descoberto no caso de algum outro
     * usuario querer enviar um syncinvite
     * */
    suspend fun updateDatabaseMetadata() {
        Firestore.lastLoginDocument().set(LastLogin()).await()
    }

    fun observeSyncInvites(callback: (MutableList<SyncAccount>) -> Any): ListenerRegister {
        val listenerRegistration =
            Firestore.syncInvitesCollection()
                .addSnapshotListener { querySnapshot: QuerySnapshot?, _: FirebaseFirestoreException? ->

                    val invites = mutableListOf<SyncAccount>()

                    querySnapshot?.documents?.forEach { snap ->
                        invites.add(snap.toObject<SyncAccount>()!!)
                    }
                    Log.d(
                        "USUK",
                        "UserRepository.".plus("observeSyncInvites() querySnapshot = ${querySnapshot?.documents?.size}")
                    )
                    callback(invites)
                }

        return ListenerRegister(listenerRegistration)
    }

    fun observeGuests(callback: (MutableList<SyncAccount>) -> Any): ListenerRegister {
        val localUserEmail = getUser()!!.email!!

        val listenerRegistration = Firestore.guestsCollection().whereEqualTo("accepted", true)
            .addSnapshotListener { querySnapshot: QuerySnapshot?, _: FirebaseFirestoreException? ->

                val guests = mutableListOf<SyncAccount>()

                querySnapshot?.documents?.forEach { snap ->
                    val guest = snap.toObject<SyncAccount>()!!
                    //Quando o usuario aceita ser convidado de outra conta, as contas convidadas do anfitriao aparecem para ele.
                    //Esse filtro impede que a conta do usuario apareça com convidada dela mesma
                    if (guest.email != localUserEmail) guests.add(guest)
                }
                callback(guests)
            }

        return ListenerRegister(listenerRegistration)
    }

    fun observeHost(callback: (MutableList<SyncAccount>) -> Any): ListenerRegister {
        val listenerRegistration =
            Firestore.hostCollection()
                .addSnapshotListener { querySnapshot: QuerySnapshot?, _: FirebaseFirestoreException? ->

                    val invites = mutableListOf<SyncAccount>()

                    querySnapshot?.documents?.forEach { snap ->
                        invites.add(snap.toObject<SyncAccount>()!!)
                    }
                    callback(invites)
                }

        return ListenerRegister(listenerRegistration)
    }

    /**
     * Salva no banco de dados do usuario alvo um convite de sincronismo os dados do usuario atual
     * @param email do usuario que recebera o convite
     * @throws IllegalStateException se o usuario alvo nao existir no DB
     * */
    suspend fun sendSyncInvite(email: String): Boolean {

        if (!checkIfUserExists(email)) throw IllegalStateException("O usuario alvo nao existe, é necessario verificar isso quando o usuario (local) insere o email (do alvo) na interface")

        try {
            Log.d("USUK", "UserRepository.".plus("sendSyncInvite() email = $email 0 "))
            val myUser = getUser()!!

            // salvo os dados do convidado na seçao de convidados do local, isso permite o convidado modificar o banco de dados do usuario local
            Firestore.guestsCollection().document(email).set(SyncAccount("", email, "", false))
                .await()
            Log.d("USUK", "UserRepository.".plus("sendSyncInvite() email = $email 1 "))

            Firestore.syncInvitesCollection(email).document(myUser.email!!).set(
                SyncAccount(
                    myUser.displayName!!, myUser.email!!, myUser.photoUrl.toString(), true
                ),
            ).await()

            return true
        } catch (e: Exception) {
            Log.d("USUK", "UserRepository.sendSyncInvite: ${e.message}")
            return false
        }
    }

    suspend fun acceptInvite(invite: SyncAccount): Boolean {

        return try {

            //Atualizo o db do anfitriao com os dados do usuario local, para que ele saiba que o convite foi aceito
            val localUser = getUser()!!
            Firestore.guestsCollection(invite.email).document(localUser.email!!)
                .set(
                    SyncAccount(
                        localUser.displayName!!,
                        localUser.email!!,
                        localUser.photoUrl.toString(),
                        true
                    )
                )

            // Apago o conviteque ja foi aceito
            Firestore.syncInvitesCollection().document(invite.email).delete().await()

            // Salvo os dados do anfitriao na seçao host do local
            Firestore.hostDocument().set(invite)

            true
        } catch (e: Exception) {
            Log.d(
                "USUK", "UserRepository.acceptRequest:Erro aceitando solicitação de sincronismo: $e"
            )
            false
        }
    }

    suspend fun declineInvite(invite: SyncAccount): Boolean {
        return try {

            Firestore.guestsCollection(invite.email).document(getUser()!!.email!!)
                .delete().await()

            Firestore.syncInvitesCollection().document(invite.email).delete().await()

            true
        } catch (e: Exception) {
            Log.d(
                "USUK",
                "UserRepository.declineRequest:Erro recusando solicitação de sincronismo: $e"
            )
            false
        }
    }

    suspend fun disconnectGuest(guest: SyncAccount): Result<Boolean> {

        return try {
            // Removo os dados do convidado da seçao de convidados
            Firestore.guestsCollection().document(guest.email).delete().await()
            Firestore.rootCollection().document(guest.email).delete().await()
            Result.success(true)

        } catch (e: Exception) {
            Result.failure(e)
        }


    }

    suspend fun disconnectFromHost(host: SyncAccount): Result<Boolean> {

        return try {
// TODO: clonar o db aqui
            val localUser = getUser()!!


            // Limpar dados do host no db do usuario local
            Firestore.hostDocument().delete().await()

            // Remover dados do local da seçao guests do anfitriao
            Firestore.guestsCollection(host.email).document(localUser.email!!).delete().await()

            Result.success(true)

        } catch (e: Exception) {
            Result.failure(e)
        }

    }
}