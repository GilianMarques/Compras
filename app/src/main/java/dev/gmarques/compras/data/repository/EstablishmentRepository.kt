package dev.gmarques.compras.data.repository

import android.util.Log
import com.google.firebase.firestore.toObject
import dev.gmarques.compras.App
import dev.gmarques.compras.R
import dev.gmarques.compras.data.firestore.Firestore
import dev.gmarques.compras.data.model.Establishment
import dev.gmarques.compras.data.repository.model.ValidatedEstablishment
import dev.gmarques.compras.domain.utils.ListenerRegister
import kotlinx.coroutines.tasks.await

object EstablishmentRepository {

    fun addOrUpdateEstablishment(validatedEstablishment: ValidatedEstablishment) {
        val establishment = validatedEstablishment.establishment
        Firestore.establishmentsCollection().document(establishment.id).set(establishment)
    }

    suspend fun tryAndRemoveEstablishment(validatedEstablishment: ValidatedEstablishment): Result<Boolean> {
        val establishment = validatedEstablishment.establishment

        val productsUsing = ProductRepository.hasAnyProductWithEstablishmentId(establishment.id)
        val suggestionProductsUsing = SuggestionProductRepository.hasAnyProductWithEstablishmentId(establishment.id)

        val establishmentInUse = productsUsing || suggestionProductsUsing

        if (establishmentInUse) {
            return Result.failure(
                Exception(
                    App.getContext().getString(R.string.A_categoria_esta_em_uso_e_nao_pode_ser_removida)
                )
            )
        } else {
            Firestore.establishmentsCollection().document(establishment.id).delete()
            return Result.success(true)
        }
    }

    /**
     * Retorna uma lista de estabelecimentos com base no nome fornecido. O limite padrao de resultados é
     * um numero que representa o valor maximo permitido pelo firebase, dentro do contexto da aplicação e pode ser limitado a
     * 1 se necessário.
     */
    suspend fun getEstablishmentsByName(name: String, limit: Long = 10000): Result<List<Establishment>> {
        val querySnapshot = Firestore.establishmentsCollection().whereEqualTo("name", name)
            .limit(limit)
            .get().await()

        return if (!querySnapshot.isEmpty) {
            val targetEstablishment = querySnapshot.mapNotNull { it.toObject<Establishment>() }
            Result.success(targetEstablishment)
        } else Result.success(emptyList())
    }
    
    
    suspend fun getAllEstablishments(): Result<List<Establishment>> {
        val querySnapshot = Firestore.establishmentsCollection()
            .get().await()

        return if (!querySnapshot.isEmpty) {
            val targetEstablishment = querySnapshot.mapNotNull { it.toObject<Establishment>() }
            Result.success(targetEstablishment)
        } else Result.success(emptyList())
    }

    suspend fun getEstablishment(establishmentId: String): Establishment? {
        if (establishmentId.isBlank()) throw IllegalArgumentException("A id buscada nao pode ser nula ou estar em branco")
        Log.d("USUK", "EstablishmentRepository.".plus("getEstablishment() establishmentId = $establishmentId"))
        val querySnapshot = Firestore.establishmentsCollection().document(establishmentId).get().await()

        val targetEstablishment = querySnapshot.toObject<Establishment>()
        return targetEstablishment
    }

    /**
     * Define um listener no firebase que notifica de altaraçoes locais e na nuvem
     * Lembre-se de remover o listener quando nao for mais necessario para evitar vazamentos de memoria
     * */
    fun observeEstablishmentUpdates(onSnapshot: (List<Establishment>?, Exception?) -> Any): ListenerRegister {
        return ListenerRegister(Firestore.establishmentsCollection().addSnapshotListener { querySnapshot, fbException ->

            if (fbException != null) onSnapshot(null, fbException)
            else querySnapshot?.let {
                val establishments = arrayListOf<Establishment>()
                establishments.addAll(querySnapshot.map { it.toObject<Establishment>() })
                onSnapshot(establishments, null)
            }
        })

    }


}
