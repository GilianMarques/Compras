package dev.gmarques.compras.data.repository

import com.google.firebase.firestore.toObject
import dev.gmarques.compras.App
import dev.gmarques.compras.R
import dev.gmarques.compras.data.firestore.Firestore
import dev.gmarques.compras.data.model.Category
import dev.gmarques.compras.data.repository.model.ValidatedCategory
import dev.gmarques.compras.domain.utils.ListenerRegister
import kotlinx.coroutines.tasks.await

object CategoryRepository {

    fun addOrUpdateCategory(validatedCategory: ValidatedCategory) {
        val category = validatedCategory.category
        Firestore.categoriesCollection().document(category.id).set(category)
    }

    suspend fun tryAndRemoveCategory(validatedCategory: ValidatedCategory): Result<Boolean> {
        val category = validatedCategory.category

        val productsUsing = ProductRepository.hasAnyProductWithCategoryId(category.id)
        val suggestionProductsUsing =
            SuggestionProductRepository.hasAnyProductWithCategoryId(category.id)

        val categoryInUse = productsUsing || suggestionProductsUsing

        if (categoryInUse) {
            return Result.failure(
                Exception(
                    App.getContext()
                        .getString(R.string.A_categoria_esta_em_uso_e_nao_pode_ser_removida)
                )
            )
        } else {
            Firestore.categoriesCollection().document(category.id).delete()
            return Result.success(true)
        }
    }

    suspend fun getCategoryByName(name: String): Result<Category?> {
        val querySnapshot =
            Firestore.categoriesCollection().whereEqualTo("name", name).limit(1).get().await()

        return if (!querySnapshot.isEmpty) {
            val targetCategory = querySnapshot.documents[0].toObject<Category>()
            Result.success(targetCategory)
        } else Result.success(null)
    }

    suspend fun getCategory(categoryId: String): Category {
        if (categoryId.isNullOrBlank()) throw IllegalArgumentException("A id buscada nao pode ser nula ou estar em branco")

        val querySnapshot = Firestore.categoriesCollection().document(categoryId).get().await()

        val targetCategory = querySnapshot.toObject<Category>()!!
        return targetCategory
    }

    /**
     * Define um listener no firebase que notifica de altaraçoes locais e na nuvem
     * Lembre-se de remover o listener quando nao for mais necessario para evitar vazamentos de memoria
     * */
    fun observeCategoryUpdates(onSnapshot: (List<Category>?, Exception?) -> Any): ListenerRegister {
        return ListenerRegister(
            Firestore.categoriesCollection().addSnapshotListener { querySnapshot, fbException ->

                if (fbException != null) onSnapshot(null, fbException)
                else querySnapshot?.let {
                    val categories = arrayListOf<Category>()
                    categories.addAll(querySnapshot.map { it.toObject<Category>() })
                    onSnapshot(categories, null)
                }
            })

    }


}
