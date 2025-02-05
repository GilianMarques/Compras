package dev.gmarques.compras.data.repository

import android.util.Log
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.toObject
import dev.gmarques.compras.data.firestore.Firestore
import dev.gmarques.compras.data.model.Product
import dev.gmarques.compras.data.repository.model.ValidatedProduct
import dev.gmarques.compras.domain.utils.ListenerRegister
import kotlinx.coroutines.tasks.await

object ProductRepository {

    /**
     * Adiciona ou atualiza um produto na coleção de produtos.
     * @param validatedProduct Objeto contendo o produto a ser adicionado ou atualizado.
     */
    fun addOrUpdateProduct(validatedProduct: ValidatedProduct) {
        val product = validatedProduct.product
        Firestore.productCollection.document(product.id).set(product)
    }

    /**
     * Retorna um produto do banco de dados com base no ID fornecido.
     * @param idProduct ID do produto a ser recuperado.
     * @return Um [Result] contendo o produto ou uma exceção se não encontrado.
     */
    suspend fun getProduct(idProduct: String): Product {
        val querySnapshot = Firestore.productCollection.document(idProduct).get().await()
        val targetProduct = querySnapshot.toObject<Product>()
        if (targetProduct != null) return targetProduct
        else throw Exception("Produto nao encontrado!")
    }

    /**
     * Retorna um produto com base no nome e ID da lista fornecidos.
     * @param name Nome do produto.
     * @param listId ID da lista de compras.
     * @return Um [Result] contendo o produto ou null se não encontrado.
     */
    suspend fun getProductByName(name: String, listId: String): Result<Product?> {
        val querySnapShot = Firestore.productCollection
            .whereEqualTo("name", name).whereEqualTo("shopListId", listId)
            .limit(1).get().await()

        return if (querySnapShot.isEmpty) Result.success(null)
        else Result.success(querySnapShot.documents.first().toObject<Product>())
    }

    /**
     * Retorna os nomes dos produtos associados a uma lista de compras.
     * @param shopListId ID da lista de compras.
     * @return Uma lista de nomes de produtos.
     */
    suspend fun getProducts(shopListId: String): List<String> {
        val querySnapshot: QuerySnapshot = Firestore.productCollection.whereEqualTo("shopListId", shopListId).get().await()
        return querySnapshot.map { it.toObject<Product>().name }
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
        val productsSnapshot = Firestore.productCollection.whereEqualTo("categoryId", categoryId).limit(1).get().await()
        return !productsSnapshot.isEmpty
    }

    /**
     * Define um listener no Firestore para notificações de alterações em produtos locais e na nuvem.
     * @param shopListId ID da lista de compras.
     * @param onSnapshot Função chamada ao receber atualizações ou erros.
     * @return Um [ListenerRegister] para gerenciar o listener.
     */
    fun observeProductUpdates(
        shopListId: String,
        onSnapshot: (List<Product>?, Exception?) -> Any,
    ): ListenerRegister {
        return ListenerRegister(Firestore.productCollection.whereEqualTo("shopListId", shopListId)
            .addSnapshotListener { querySnapshot, fbException ->
                if (fbException != null) onSnapshot(null, fbException)
                else querySnapshot?.let {
                    val products = arrayListOf<Product>()
                    products.addAll(querySnapshot.map { it.toObject<Product>() })
                    onSnapshot(products, null)
                }
            })
    }


    /**
     * Remove todos os produtos associados a uma lista de compras do Firestore.
     * @param shopListId ID da lista de compras.
     */
    suspend fun removeAllProductsFromList(shopListId: String) {
        val querySnapshot = Firestore.productCollection.whereEqualTo("shopListId", shopListId).get().await()
        for (document in querySnapshot.documents) {
            document.reference.delete()
            Log.d("USUK", "ProductRepository.removeAllProductsFromList: removendo: ${document.get("name")}")
        }
    }

    /**
     * Remove um produto da coleção de produtos do Firestore.
     * @param validatedProduct Objeto contendo o produto a ser removido.
     */
    fun removeProduct(validatedProduct: ValidatedProduct) {
        val product = validatedProduct.product
        Firestore.productCollection.document(product.id).delete()
    }

}
