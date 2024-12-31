package dev.gmarques.compras.ui.add_edit_category

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dev.gmarques.compras.App
import dev.gmarques.compras.R
import dev.gmarques.compras.data.model.Category
import dev.gmarques.compras.data.repository.CategoryRepository
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.withContext


class AddEditCategoryActivityViewModel : ViewModel() {

    fun tryAndSaveCategory() {

        // se adicionanda categoria ou se durante a ediçao o usuario trocar o nome da categoria, preciso verificar se o novo nome ja nao existe
        val needCheckName = !editingCategory || editingCategoryLD.value!!.name != validatedName

        if (needCheckName) {
            CategoryRepository.getCategoryByName(validatedName) { result ->
                if (result.isSuccess) {
                    if (result.getOrNull() == null) saveCategory()// a categoria nao existe, posso salvar
                    else {
                        val msg = String.format(App.getContext().getString(R.string.X_ja_existe), validatedName)
                        _errorEventFlow.tryEmit(msg)
                    }
                } else {
                    val msg = String.format(
                        App.getContext().getString(R.string.Nao_foi_possivel_verificar_se_x_ja_existe),
                        validatedName
                    )
                    _errorEventFlow.tryEmit(msg)
                }
            }

        } else saveCategory()


    }

    private fun saveCategory() {

        val newCategory = if (editingCategory) _editingCategoryLD.value!!.copy(
            name = validatedName,
            color = validatedColor
        )
        else Category(
            validatedName, validatedColor
        )

        CategoryRepository.addOrUpdateCategory(newCategory)

        _finishEventFlow.tryEmit(true)

    }

    suspend fun loadCategory() = withContext(IO) {
        categoryId?.let {
            CategoryRepository.getCategory(categoryId!!) { result ->

                if (result.isSuccess) _editingCategoryLD.postValue(result.getOrThrow())
                else Log.d(
                    "USUK",
                    "AddCategoryActivityViewModel.loadCategory: erro obtendo a categoria do firebase${result.exceptionOrNull()}"
                )
            }
        }
    }

    var editingCategory: Boolean = false
    var categoryId: String? = null
    var validatedName: String = ""
    var validatedColor: Int = -1


    private val _editingCategoryLD = MutableLiveData<Category>()
    val editingCategoryLD: LiveData<Category> get() = _editingCategoryLD

    private val _errorEventFlow = MutableSharedFlow<String>(replay = 1)
    val errorEventFlow = _errorEventFlow.asSharedFlow()

    private val _finishEventFlow = MutableSharedFlow<Any?>(replay = 1)
    val finishEventFlow = _finishEventFlow.asSharedFlow()

}