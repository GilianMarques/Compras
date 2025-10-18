package dev.gmarques.compras.presenter.add_edit_shop_list

import android.content.Context
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.TextView
import dev.gmarques.compras.presenter.MyActivity
import androidx.appcompat.widget.AppCompatEditText
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import dev.gmarques.compras.R
import dev.gmarques.compras.data.model.ShopList
import dev.gmarques.compras.databinding.ActivityAddEditShopListBinding
import dev.gmarques.compras.domain.utils.ExtFun.Companion.showKeyboard
import dev.gmarques.compras.presenter.BsdSelectColor
import dev.gmarques.compras.presenter.Vibrator
import kotlinx.coroutines.launch

/**
 * Activity para adicionar ou editar listas de compra.
 * Implementada seguindo o padrão MVVM e princípios de Clean Code e SOLID.
 */
class AddEditShopListActivity: MyActivity() {

    private lateinit var binding: ActivityAddEditShopListBinding
    private lateinit var viewModel: AddEditShopListActivityViewModel

    companion object {
        private const val SHOPLIST_ID = "shopList_id"

        fun newIntentAddShopList(context: Context): Intent {
            return Intent(context, AddEditShopListActivity::class.java).apply {
            }
        }

        fun newIntentEditShopList(context: Context, shopListId: String): Intent {
            return Intent(context, AddEditShopListActivity::class.java).apply {
                putExtra(SHOPLIST_ID, shopListId)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddEditShopListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[AddEditShopListActivityViewModel::class.java]
        viewModel.shopListId = intent.getStringExtra(SHOPLIST_ID)


        setupToolbar()
        setupFabAddShopList()
        setupInputName()
        setupInputColor()
        observeShopList()
        observeViewmodelErrorMessages()
        observeViewmodelFinishEvent()


        binding.edtName.showKeyboard()

    }

    private fun observeViewmodelErrorMessages() = lifecycleScope.launch {
        viewModel.errorEventLD.observe(this@AddEditShopListActivity) { event ->
            Snackbar.make(binding.root, event, Snackbar.LENGTH_LONG).show()
            Vibrator.error()
        }
    }

    private fun observeViewmodelFinishEvent() = lifecycleScope.launch {
        viewModel.finishEventLD.observe(this@AddEditShopListActivity) {
            finish()
        }
    }

    private fun observeShopList() = lifecycleScope.launch {
        viewModel.loadShopList()
        viewModel.editingShopListLD.observe(this@AddEditShopListActivity) {

            it?.let {
                viewModel.editingShopList = true
                updateViewModelAndUiWithEditableShopList(it)
            }
        }
    }

    private fun updateViewModelAndUiWithEditableShopList(shopList: ShopList) = binding.apply {
        viewModel.apply {

            edtName.setText(shopList.name)
            validatedName = shopList.name

            edtColor.hint = getString(R.string.Clique_aqui_para_alterar_a_cor)
            validatedColor = shopList.color
            changeEditDrawableTextColor()

            toolbar.tvActivityTitle.text =
                String.format(getString(R.string.Editar_x), shopList.name)
            fabSave.text = getString(R.string.Salvar_lista)

        }
    }

    /**
     * Configura o botão de salvar categoria (FAB).
     */
    private fun setupFabAddShopList() = binding.apply {
        fabSave.setOnClickListener {
            root.clearFocus()

            viewModel.apply {

                if (validatedName.isEmpty()) edtName.requestFocus()
                else if (validatedColor == -1) {
                    edtColor.requestFocus()
                } else {
                    root.clearFocus()
                    lifecycleScope.launch { tryAndSaveShopList() }
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
                val result = ShopList.Validator.validateName(term, this)

                if (result.isSuccess) {
                    viewModel.validatedName = result.getOrThrow()
                    edtTarget.setText(viewModel.validatedName)
                } else {
                    viewModel.validatedName = ""
                    showError(edtTarget, tvTarget, result.exceptionOrNull()!!.message!!)
                }
            }
        }

        if (!viewModel.editingShopList) {
            val currentMonthName = java.text.DateFormatSymbols().months[java.util.Calendar.getInstance().get(java.util.Calendar.MONTH)]
            edtTarget.setText(getString(R.string.Compras_de_x, currentMonthName))
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
                val result = ShopList.Validator.validateColor(viewModel.validatedColor, this)

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

        BsdSelectColor.Builder(this, true)
            .setOnConfirmListener { selectedColor ->
                viewModel.validatedColor = selectedColor
                Vibrator.success()
            }
            .setOnDismissListener {
                binding.edtColor.clearFocus()
            }
            .build()
            .show()
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
        tvActivityTitle.text = getString(R.string.Adicionar_lista)
        ivGoBack.setOnClickListener {Vibrator.interaction(); finish() }
        ivMenu.visibility = GONE
    }

}
