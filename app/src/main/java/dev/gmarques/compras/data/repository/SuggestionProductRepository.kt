package dev.gmarques.compras.data.repository

import com.google.firebase.firestore.toObject
import dev.gmarques.compras.data.firestore.Firestore
import dev.gmarques.compras.data.model.Product
import dev.gmarques.compras.data.repository.model.ValidatedSuggestionProduct
import dev.gmarques.compras.domain.utils.ListenerRegister
import kotlinx.coroutines.tasks.await

object SuggestionProductRepository {

    /**
     * Retorna um produto de sugestão do banco de dados com base no ID fornecido.
     * @param idProduct ID do produto a ser recuperado.
     * @return Um [Result] contendo o produto ou uma exceção se não encontrado.
     */
    suspend fun getSuggestionProduct(idProduct: String): Product {
        val querySnapshot = Firestore.suggestionProductCollection.document(idProduct).get().await()
        val targetProduct = querySnapshot.toObject<Product>()
        if (targetProduct != null) return targetProduct
        else throw Exception("Produto nao encontrado! É um bug ou foi removido de outro lugar na app, servidor ou de outro dispositivo vinculado à conta")
    }

    /**
     * Retorna uma lista de produtos sugeridos do Firestore.
     * @return Uma lista de produtos sugeridos.
     */
    suspend fun getSuggestions(): List<Product> {
        val querySnapshot = Firestore.suggestionProductCollection.get().await()
        return querySnapshot.map { it.toObject<Product>() }
    }

    /**
     * Verifica se há pelo menos um produto ou sugestão associado ao ID de categoria fornecido.
     * @param categoryId ID da categoria a ser verificada.
     * @return Um [Result] indicando se há produtos associados.
     */
    suspend fun hasAnyProductWithCategoryId(categoryId: String): Boolean {
        val suggestionProductsSnapshot =
            Firestore.suggestionProductCollection.whereEqualTo("categoryId", categoryId).limit(1).get().await()
        return !suggestionProductsSnapshot.isEmpty
    }

    /**
     * Define um listener no Firestore para notificações de alterações em produtos sugeridos.
     * @param onSnapshot Função chamada ao receber atualizações ou erros.
     * @return Um [ListenerRegister] para gerenciar o listener.
     */
    fun observeSuggestionProductUpdates(
        onSnapshot: (List<Product>?, Exception?) -> Any,
    ): ListenerRegister {
        return ListenerRegister(Firestore.suggestionProductCollection.addSnapshotListener { querySnapshot, fbException ->
            if (fbException != null) onSnapshot(null, fbException)
            else querySnapshot?.let {
                val products = arrayListOf<Product>()
                products.addAll(querySnapshot.map { it.toObject<Product>() })
                onSnapshot(products, null)
            }
        })
    }

    /**
     * Remove um produto da coleção de sugestões do Firestore.
     * @param validatedProduct Objeto contendo o produto sugerido a ser removido.
     */
    fun removeSuggestionProduct(vsp: ValidatedSuggestionProduct) {
        Firestore.suggestionProductCollection.document(vsp.suggestionProduct.id).delete()
    }

    /**
     * Atualiza ou adiciona um produto como sugestão no Firestore.
     * @param vsp Objeto contendo o produto validado.
     */
    suspend fun updateOrAddProductAsSuggestion(vsp: ValidatedSuggestionProduct) {

        val querySnapshot = Firestore.suggestionProductCollection.whereEqualTo("name", vsp.suggestionProduct.name).limit(1).get().await()
        val suggestionProduct = if (querySnapshot.isEmpty) vsp.suggestionProduct
        else {
            val oldProduct = querySnapshot.documents[0].toObject<Product>()!!
            vsp.suggestionProduct.copy(id = oldProduct.id)
        }

        Firestore.suggestionProductCollection.document(suggestionProduct.id).set(suggestionProduct)
    }

    /**
     * Atualiza um produto de sugestao no Firestore considerando alterações no nome.
     * @param oldSuggestion Produto sugerido antigo.
     * @param newValidatedSuggestion Objeto validado contendo o produto atualizado.
     */
    suspend fun updateSuggestionProduct(oldSuggestion: Product, newValidatedSuggestion: ValidatedSuggestionProduct) {
        val documentSnapshot =
            Firestore.suggestionProductCollection.whereEqualTo("name", oldSuggestion.name).limit(1).get().await()

        if (!documentSnapshot.isEmpty) {
            val targetSuggestion = documentSnapshot.documents.first().toObject<Product>()!!
            val updatedSuggestionWithOldId = newValidatedSuggestion.suggestionProduct.copy(id = targetSuggestion.id)
            Firestore.suggestionProductCollection.document(updatedSuggestionWithOldId.id).set(updatedSuggestionWithOldId)
        }
    }
}
