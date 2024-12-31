package dev.gmarques.compras.data.repository

import com.google.firebase.firestore.toObject
import dev.gmarques.compras.App
import dev.gmarques.compras.R
import dev.gmarques.compras.data.firestore.Firestore
import dev.gmarques.compras.data.model.Category
import dev.gmarques.compras.utils.ListenerRegister


object CategoryRepository {

    fun addOrUpdateCategory(category: Category) {
        Firestore.categoryCollection.document(category.id)
            .set(category.selfValidate())
    }

    fun tryAndRemoveCategory(category: Category, callback: (Result<Boolean>) -> Unit) {

        ProductRepository.hasAnyProductWithCategoryId(category.id) { result ->
            if (result.isSuccess) {
                //categoria em uso nao pode ser removida
                if (result.getOrThrow()) {
                    callback(
                        Result.failure(
                            Exception(
                                App.getContext().getString(R.string.A_categoria_esta_em_uso_e_nao_pode_ser_removida)
                            )
                        )
                    )
                } else {
                    Firestore.categoryCollection.document(category.id).delete()
                    callback(Result.success(true))
                }
            } else callback(
                Result.failure(
                    Exception(
                        App.getContext().getString(R.string.Nao_foi_possivel_verificar_se_a_categoria_esta_em_uso)
                    )
                )
            )
        }


    }

    fun getCategoryByName(name: String, callback: (Result<Category?>) -> Unit) {
        Firestore.categoryCollection.whereEqualTo("name", name).limit(1).get()
            .addOnSuccessListener { snapshot ->

                if (snapshot.isEmpty) callback(Result.success(null))
                else {
                    val targetCategory = snapshot.documents[0].toObject<Category>()
                    callback(Result.success(targetCategory))
                }
            }.addOnFailureListener { exception ->
                callback(Result.failure(exception))
            }


    }

    fun getCategory(idCategory: String, callback: (result: Result<Category>) -> Unit) {

        Firestore.categoryCollection.document(idCategory).get().addOnSuccessListener { documentSnapshot ->
            val targetCategory = documentSnapshot.toObject<Category>()!!
            callback(Result.success(targetCategory))
        }.addOnFailureListener { exception: java.lang.Exception ->
            callback(Result.failure(exception))
        }
    }

    /**
     * Define um listener no firebase que notifica de altaraçoes locais e na nuvem
     * Lembre-se de remover o listener quando nao for mais necessario para evitar vazamentos de memoria
     * */
    fun observeCategoryUpdates(onSnapshot: (List<Category>?, Exception?) -> Any): ListenerRegister {
        return ListenerRegister(
            Firestore.categoryCollection.addSnapshotListener { querySnapshot, fbException ->

                if (fbException != null) onSnapshot(null, fbException)
                else querySnapshot?.let {
                    val categories = arrayListOf<Category>()
                    categories.addAll(querySnapshot.map { it.toObject<Category>() })
                    onSnapshot(categories, null)
                }
            })

    }


}
