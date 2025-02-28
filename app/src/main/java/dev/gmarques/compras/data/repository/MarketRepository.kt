package dev.gmarques.compras.data.repository

import android.util.Log
import com.google.firebase.firestore.toObject
import dev.gmarques.compras.App
import dev.gmarques.compras.R
import dev.gmarques.compras.data.firestore.Firestore
import dev.gmarques.compras.data.model.Market
import dev.gmarques.compras.data.repository.model.ValidatedMarket
import dev.gmarques.compras.domain.utils.ListenerRegister
import kotlinx.coroutines.tasks.await

object MarketRepository {

    fun addOrUpdateMarket(validatedMarket: ValidatedMarket) {
        val market = validatedMarket.market
        Firestore.marketsCollection().document(market.id).set(market)
    }

    suspend fun tryAndRemoveMarket(validatedMarket: ValidatedMarket): Result<Boolean> {
        val market = validatedMarket.market

        val productsUsing = ProductRepository.hasAnyProductWithMarketId(market.id)
        val suggestionProductsUsing =
            SuggestionProductRepository.hasAnyProductWithMarketId(market.id)

        val marketInUse = productsUsing || suggestionProductsUsing

        if (marketInUse) {
            return Result.failure(
                Exception(
                    App.getContext()
                        .getString(R.string.A_categoria_esta_em_uso_e_nao_pode_ser_removida)
                )
            )
        } else {
            Firestore.marketsCollection().document(market.id).delete()
            return Result.success(true)
        }
    }

    suspend fun getMarketByName(name: String): Result<Market?> {
        val querySnapshot =
            Firestore.marketsCollection().whereEqualTo("name", name).limit(1).get().await()

        return if (!querySnapshot.isEmpty) {
            val targetMarket = querySnapshot.documents[0].toObject<Market>()
            Result.success(targetMarket)
        } else Result.success(null)
    }

    suspend fun getMarket(marketId: String): Market? {
        if (marketId.isBlank()) throw IllegalArgumentException("A id buscada nao pode ser nula ou estar em branco")
        Log.d("USUK", "MarketRepository.".plus("getMarket() marketId = $marketId"))
        val querySnapshot = Firestore.marketsCollection().document(marketId).get().await()

        val targetMarket = querySnapshot.toObject<Market>()
        return targetMarket
    }

    /**
     * Define um listener no firebase que notifica de altaraçoes locais e na nuvem
     * Lembre-se de remover o listener quando nao for mais necessario para evitar vazamentos de memoria
     * */
    fun observeMarketUpdates(onSnapshot: (List<Market>?, Exception?) -> Any): ListenerRegister {
        return ListenerRegister(
            Firestore.marketsCollection().addSnapshotListener { querySnapshot, fbException ->

                if (fbException != null) onSnapshot(null, fbException)
                else querySnapshot?.let {
                    val markets = arrayListOf<Market>()
                    markets.addAll(querySnapshot.map { it.toObject<Market>() })
                    onSnapshot(markets, null)
                }
            })

    }


}
