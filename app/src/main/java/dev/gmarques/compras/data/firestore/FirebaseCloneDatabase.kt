package dev.gmarques.compras.data.firestore

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.getField
import dev.gmarques.compras.data.model.Category
import dev.gmarques.compras.data.model.Establishment
import dev.gmarques.compras.data.model.Product
import dev.gmarques.compras.data.model.ShopList
import kotlinx.coroutines.tasks.await

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

        if (cleanTargetBeforeCloning) cleanDatabase()
        cloneDatabase()


    }

    /**
     * (Nem sempre) É necessário limpar o banco de dados do usuario alvo antes da clonagem para evitar dados duplicados, conflitos,
     * ou até mesmo erros por conta de dados antigos no database que nao foram migrados com o passar do tempo
     * */
    private suspend fun cleanDatabase() {

        getTargetCollections().forEach {
            it.get().await().forEach { targetDoc ->
                targetDoc.reference.delete().await()
            }

        }
    }

    private suspend fun cloneDatabase() {

        getSetsOfCollections().forEach { (baseCollection, objClass, targetCollection) ->
            baseCollection.get().await().forEach { baseDoc ->
                val obj = baseDoc.toObject(objClass)
                targetCollection.document(baseDoc.getField<String>("id")!!).set(obj).await()
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