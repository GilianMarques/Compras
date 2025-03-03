package dev.gmarques.compras.ui.add_edit_category

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dev.gmarques.compras.App
import dev.gmarques.compras.R
import dev.gmarques.compras.data.model.Category
import dev.gmarques.compras.data.repository.CategoryRepository
import dev.gmarques.compras.data.repository.model.ValidatedCategory
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext


class AddEditCategoryActivityViewModel : ViewModel() {


    var editingCategory: Boolean = false
    var categoryId: String? = null
    var validatedName: String = ""
    var validatedColor: Int = -1


    private val _editingCategoryLD = MutableLiveData<Category>()
    val editingCategoryLD: LiveData<Category> get() = _editingCategoryLD

    private val _finishEventLD = MutableLiveData<Boolean>()
    val finishEventLD: LiveData<Boolean> get() = _finishEventLD

    private val _errorEventLD = MutableLiveData<String>()
    val errorEventLD: LiveData<String> get() = _errorEventLD

    suspend fun tryAndSaveCategory() = withContext(IO) {

        // se adicionada categoria ou se durante a edição o usuário trocar o nome da categoria, preciso verificar se o novo nome ja nao existe
        val needCheckName = !editingCategory || editingCategoryLD.value!!.name != validatedName

        if (needCheckName) {
            val result = CategoryRepository.getCategoryByName(validatedName)

            val categoryDoesNotExist = result.getOrNull() == null

            if (categoryDoesNotExist) saveCategory()
            else {
                val msg = String.format(App.getContext().getString(R.string.X_ja_existe), validatedName)
                _errorEventLD.postValue(msg)
            }

        } else saveCategory()


    }

    private fun saveCategory() {

        val newCategory = if (editingCategory) _editingCategoryLD.value!!.copy(
            name = validatedName, color = validatedColor
        )
        else Category(
            validatedName, validatedColor
        )

        CategoryRepository.addOrUpdateCategory(ValidatedCategory(newCategory))

        _finishEventLD.postValue(true)

    }

    suspend fun loadCategory() = withContext(IO) {
        categoryId?.let {
            val category = CategoryRepository.getCategory(categoryId!!)
            _editingCategoryLD.postValue(category)
        }
    }


}