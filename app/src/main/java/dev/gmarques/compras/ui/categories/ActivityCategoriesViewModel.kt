package dev.gmarques.compras.ui.categories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.gmarques.compras.App
import dev.gmarques.compras.R
import dev.gmarques.compras.data.model.Category
import dev.gmarques.compras.data.repository.CategoryRepository
import dev.gmarques.compras.data.repository.model.ValidatedCategory
import dev.gmarques.compras.domain.utils.ListenerRegister
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class ActivityCategoriesViewModel : ViewModel() {

    private var listenerRegister: ListenerRegister? = null

    private val _categoriesLd = MutableLiveData<List<Category>>()
    val categoriesLd: LiveData<List<Category>> get() = _categoriesLd

    private val _errorEventLD = MutableLiveData<String>()
    val errorEventLD: LiveData<String> get() = _errorEventLD

    init {
        loadCategories()
    }

    override fun onCleared() {
        listenerRegister?.remove()
        super.onCleared()
    }

    private fun loadCategories() {
        listenerRegister = CategoryRepository.observeCategoryUpdates { categories, error ->
            if (error == null) {

                val sorted = categories!!.sortedWith(compareBy { it.name }).sortedWith(compareBy { it.position })
                _categoriesLd.postValue(sorted)

            } else _errorEventLD.postValue(App.getContext().getString(R.string.Erro_ao_carregar_categorias_x, error.message))

        }
    }

    fun removeCategory(category: Category) = viewModelScope.launch(IO) {
        val result = CategoryRepository.tryAndRemoveCategory(ValidatedCategory(category))
        if (result.isFailure) _errorEventLD.postValue(result.exceptionOrNull()!!.message!!)
    }

    fun updateCategoryPosition(category: Category, newIndex: Int) {
        val newCategory = category.copy(position = newIndex)
        CategoryRepository.addOrUpdateCategory(ValidatedCategory(newCategory))
    }

}
