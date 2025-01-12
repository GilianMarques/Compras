package dev.gmarques.compras.ui.add_edit_category

import android.content.Context
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import dev.gmarques.compras.App
import dev.gmarques.compras.R
import dev.gmarques.compras.data.model.Category
import dev.gmarques.compras.databinding.ActivityAddEditCategoryBinding
import dev.gmarques.compras.domain.utils.ExtFun.Companion.showKeyboard
import dev.gmarques.compras.ui.Vibrator
import kotlinx.coroutines.launch

/**
 * Activity para adicionar ou editar categorias em uma lista.
 * Implementada seguindo o padrão MVVM e princípios de Clean Code e SOLID.
 */
class AddEditCategoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEditCategoryBinding
    private lateinit var viewModel: AddEditCategoryActivityViewModel

    companion object {
        private const val CATEGORY_ID = "category_id"

        fun newIntentAddCategory(context: Context): Intent {
            return Intent(context, AddEditCategoryActivity::class.java).apply {
            }
        }

        fun newIntentEditCategory(context: Context, categoryId: String): Intent {
            return Intent(context, AddEditCategoryActivity::class.java).apply {
                putExtra(CATEGORY_ID, categoryId)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddEditCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[AddEditCategoryActivityViewModel::class.java]
        viewModel.categoryId = intent.getStringExtra(CATEGORY_ID)


        setupToolbar()
        initFabAddCategory()
        setupInputName()
        setupInputColor()
        observeCategory()
        observeViewmodelErrorMessages()
        observeViewmodelFinishEvent()

        binding.edtName.showKeyboard()

    }

    private fun observeViewmodelErrorMessages() = lifecycleScope.launch {
        viewModel.errorEventLD.observe(this@AddEditCategoryActivity) { event ->
            Snackbar.make(binding.root, event, Snackbar.LENGTH_LONG).show()
            Vibrator.error()
        }
    }

    private fun observeViewmodelFinishEvent() = lifecycleScope.launch {
        viewModel.finishEventLD.observe(this@AddEditCategoryActivity) {
            finish()
        }
    }

    private fun observeCategory() = lifecycleScope.launch {
        viewModel.loadCategory()
        viewModel.editingCategoryLD.observe(this@AddEditCategoryActivity) {

            it?.let {
                viewModel.editingCategory = true
                updateViewModelAndUiWithEditableCategory(it)
            }
        }
    }

    private fun updateViewModelAndUiWithEditableCategory(category: Category) = binding.apply {
        viewModel.apply {

            edtName.setText(category.name)
            validatedName = category.name

            edtColor.hint = getString(R.string.Clique_aqui_para_alterar_a_cor)
            validatedColor = category.color
            changeEditDrawableTextColor()

            toolbar.tvActivityTitle.text = String.format(getString(R.string.Editar_x), category.name)
            fabSave.text = getString(R.string.Salvar_categoria)

        }
    }

    /**
     * Configura o botão de salvar categoria (FAB).
     */
    private fun initFabAddCategory() = binding.apply {
        fabSave.setOnClickListener {
            root.clearFocus()

            viewModel.apply {

                if (validatedName.isEmpty()) edtName.requestFocus()
                else if (validatedColor == -1) {
                    edtColor.requestFocus()
                } else {
                    root.clearFocus()
                    lifecycleScope.launch { tryAndSaveCategory() }
                }

            }
        }
    }

    private fun setupInputName() {

        val edtTarget = binding.edtName
        val tvTarget = binding.tvNameError

        edtTarget.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) resetFocus(edtTarget, tvTarget)
            else {
                val term = edtTarget.text.toString()
                val result = Category.Validator.validateName(term,this)

                if (result.isSuccess) {
                    viewModel.validatedName = result.getOrThrow()
                    edtTarget.setText(viewModel.validatedName)
                } else {
                    viewModel.validatedName = ""
                    showError(edtTarget, tvTarget, result.exceptionOrNull()!!.message!!)
                }
            }
        }

    }

    private fun setupInputColor() {

        val edtTarget = binding.edtColor
        val tvTarget = binding.tvColorError

        edtTarget.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                resetFocus(edtTarget, tvTarget)
                showColorDialog()
            } else {
                val result = Category.Validator.validateColor(viewModel.validatedColor, App.getContext())

                if (result.isSuccess) {
                    edtTarget.hint = getString(R.string.Cor_selecionada)
                    changeEditDrawableTextColor()

                } else {
                    edtTarget.hint = ""
                    showError(edtTarget, tvTarget, result.exceptionOrNull()!!.message!!)
                }
            }
        }

    }

    private fun changeEditDrawableTextColor() =
        (binding.edtColor.compoundDrawables[0].mutate() as GradientDrawable).setColor(viewModel.validatedColor)

    private fun showColorDialog() {
        BsdSelectColor.Builder(this)
            .setOnConfirmListener { selectedColor ->
                viewModel.validatedColor = selectedColor
                Vibrator.success()
            }
            .setOnDismissListener {
                binding.edtColor.clearFocus()
            }.build().show()
    }


    private fun resetFocus(edtTarget: AppCompatEditText, tvTarget: TextView) {
        edtTarget.setBackgroundResource(R.drawable.back_addproduct_edittext)
        tvTarget.visibility = GONE
    }

    private fun showError(targetView: View, errorView: TextView, errorMessage: String) {
        targetView.setBackgroundResource(R.drawable.back_addproduct_edittext_error)
        errorView.text = errorMessage
        errorView.visibility = VISIBLE
        Vibrator.error()
    }

    /**
     * Configura a toolbar da activity.
     */
    private fun setupToolbar() = binding.toolbar.apply {
        tvActivityTitle.text = getString(R.string.Adicionar_categoria)
        ivGoBack.setOnClickListener { finish() }
        ivMenu.visibility = GONE
    }

}
