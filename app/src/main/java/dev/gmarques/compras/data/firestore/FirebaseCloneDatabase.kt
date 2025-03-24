package dev.gmarques.compras.data.firestore

import android.util.Log
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.getField
import dev.gmarques.compras.data.model.Category
import dev.gmarques.compras.data.model.Establishment
import dev.gmarques.compras.data.model.Product
import dev.gmarques.compras.data.model.ShopList
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlin.system.measureTimeMillis

/**
 * Essa classe faz uma copia do banco de dados de um determinado usuario.
 * Foi criada com o proposito de clonar o banco de dados de um usuario apos uma conexao de contas ser encerrada, garantindo
 * que ambos os usuarios que antes compartilhavam o mesmo DB fiquem com uma copia atualizada
 * @param baseEmail o email DE onde serao copiados os dados
 * @param targetEmail o email PARA onde serao copiados os dados
 * @param cleanTargetBeforeCloning define se o banco de dados alvo tera os dados removidos antes da inclusao de novos dados
 * */
class FirebaseCloneDatabase(
    private val baseEmail: String,
    private val targetEmail: String,
    private val cleanTargetBeforeCloning: Boolean = true,
) {

    suspend fun beginCloning() {

        Log.d("USUK", "FirebaseCloneDatabase.beginCloning: inicio de clonagem")
        if (cleanTargetBeforeCloning) cleanDatabase()

        val totalTime = measureTimeMillis { cloneDatabase() }
        Log.d("USUK", "FirebaseCloneDatabase.beginCloning: tempo de clonagem: ${totalTime / 1000} segs")


    }

    /**
     * (Nem sempre) É necessário limpar o banco de dados do usuario alvo antes da clonagem para evitar dados duplicados, conflitos,
     * ou até mesmo erros por conta de dados antigos no database que nao foram migrados com o passar do tempo
     * */
    private suspend fun cleanDatabase() {
        getTargetCollections().forEach {
            it.get().await().forEach { targetDoc ->
                targetDoc.reference.delete()
            }
        }
    }

    private suspend fun cloneDatabase() = withContext(IO) {
        coroutineScope { // Organizo os jobs dentro do escopo da corrotina

            getSetsOfCollections().forEach { (baseCollection, objClass, targetCollection) ->

                // Uso launch para clonar todas as coleções em paralelo
                launch {
                    baseCollection.get().await().forEach { baseDoc ->
                        val obj = baseDoc.toObject(objClass)
                        launch { targetCollection.document(baseDoc.getField<String>("id")!!).set(obj).await() }
                    }
                }
            }
        }
    }


    /**
     * Uma lista contendo as coleções base,alvo e tipo de objeto
     */
    private fun getSetsOfCollections() = listOf(
        Triple(
            Firestore.shopListsCollection(baseEmail),
            ShopList::class.java,
            Firestore.shopListsCollection(targetEmail)

        ),

        Triple(
            Firestore.categoriesCollection(baseEmail),
            Category::class.java,
            Firestore.categoriesCollection(targetEmail)

        ),

        Triple(
            Firestore.productsCollection(baseEmail),
            Product::class.java,
            Firestore.productsCollection(targetEmail)

        ),

        Triple(
            Firestore.suggestionProductsCollection(baseEmail),
            Product::class.java,
            Firestore.suggestionProductsCollection(targetEmail)

        ),

        Triple(
            Firestore.establishmentsCollection(baseEmail),
            Establishment::class.java,
            Firestore.establishmentsCollection(targetEmail)

        )
    )

    /**
     * Retorna as coleçoes alvo para que sejam limpas caso necessario
     * */
    @Suppress("UNUSED_DESTRUCTURED_PARAMETER_ENTRY")
    private fun getTargetCollections(): MutableList<CollectionReference> {

        //  retornar a coleção errada (base ao inves de target) fara com que os dados do usuario errado sejam removidos e isso é um problemao...
        val collections = mutableListOf<CollectionReference>()
        getSetsOfCollections().forEach { (baseCollection, typeObject, targetCollection) ->
            collections.add(targetCollection)
        }

        return collections
    }

}