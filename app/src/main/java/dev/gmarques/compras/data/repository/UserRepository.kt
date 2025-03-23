package dev.gmarques.compras.data.repository

import android.content.Context
import android.util.Log
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.toObject
import dev.gmarques.compras.data.firestore.FirebaseCloneDatabase
import dev.gmarques.compras.data.firestore.Firestore
import dev.gmarques.compras.data.model.LastAccess
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
            .document(Firestore.LAST_ACCESS)
            .get()
            .await().exists()
    }

    /**
     * Salva dados na raiz do banco do usuario para permitir que ele seja descoberto no caso de algum outro
     * usuario querer enviar um syncinvite
     * */
    suspend fun updateLastAccessInfo() {
        Firestore.lastAccessDocument().set(LastAccess()).await()
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
    suspend fun sendSyncInvite(email: String, mergeData: Boolean): Boolean {

        if (!checkIfUserExists(email)) throw IllegalStateException("O usuario alvo nao existe, é necessario verificar isso quando o usuario (local) insere o email (do alvo) na interface")

        try {
            val myUser = getUser()!!

            // salvo os dados do convidado na seçao de convidados do local, isso permite o convidado modificar o banco de dados do usuario local
            Firestore.guestsCollection().document(email)
                .set(SyncAccount("", email, "", mergeData, false))
                .await()

            Firestore.syncInvitesCollection(email).document(myUser.email!!).set(
                SyncAccount(
                    myUser.displayName!!,
                    myUser.email!!,
                    myUser.photoUrl.toString(),
                    mergeData,
                    true
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
                        false,
                        true
                    )
                )
            // Apago o convite que ja foi aceito
            Firestore.syncInvitesCollection().document(invite.email).delete().await()

            // Salvo os dados do anfitriao na seçao host do local
            Firestore.hostDocument().set(invite)

            if (invite.mergeData) {
                FirebaseCloneDatabase(localUser.email!!, invite.email, false).beginCloning()
            }

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
            Result.success(true)

        } catch (e: Exception) {
            Result.failure(e)
        }


    }

    /**
     * Se desconeta do anfitriao, apagando as referencias das contas um dou outro e
     * criando uma cópia do banco de dados para o convidado se solicitado
     * @see FirebaseCloneDatabase
     * */
    suspend fun disconnectFromHost(host: SyncAccount, cloneData: Boolean): Result<Boolean> {

        return try {
            val localUser = getUser()!!

            /* Faz uma copia do banco de dados para o convidado */
            if (cloneData) FirebaseCloneDatabase(host.email, localUser.email!!).beginCloning()

            // Apaga os dados do host do db do convidado
            Firestore.hostDocument().delete().await()

            // Remove dados do convidado da seçao guests do host
            Firestore.guestsCollection(host.email).document(localUser.email!!).delete().await()

            Result.success(true)

        } catch (e: Exception) {
            Result.failure(e)
        }

    }

    /**
     * Observa o status de convidado do usuario atual, para que caso ele seja convidado de outro usuario
     * e seja desconectado pelo anfitriao, o app possa prosseguir com o procedimento de desconexão
     * imediatamente e nao apenas no proximo boot
     */
    fun observeGuestStatus(callback: () -> Any): ListenerRegister? {

        val localUserEmail = getUser()!!.email!!

        if (!Firestore.amIaGuest) return null
        val listenerRegistration = Firestore.guestsCollection(Firestore.dataHostEmail).document(localUserEmail)
            .addSnapshotListener { snap, _ ->
                snap?.let { if (!snap.exists()) callback() }
            }


        return ListenerRegister(listenerRegistration)
    }
}