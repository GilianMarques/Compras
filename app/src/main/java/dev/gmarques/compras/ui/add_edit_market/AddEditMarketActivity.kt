package dev.gmarques.compras.ui.add_edit_market

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
import dev.gmarques.compras.R
import dev.gmarques.compras.data.model.Market
import dev.gmarques.compras.databinding.ActivityAddEditMarketBinding
import dev.gmarques.compras.domain.utils.ExtFun.Companion.showKeyboard
import dev.gmarques.compras.ui.BsdSelectColor
import dev.gmarques.compras.ui.Vibrator
import kotlinx.coroutines.launch

/**
 * Activity para adicionar ou editar estabelecimentos em uma lista.
 * Implementada seguindo o padrão MVVM e princípios de Clean Code e SOLID.
 */
class AddEditMarketActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEditMarketBinding
    private lateinit var viewModel: AddEditMarketActivityViewModel

    companion object {
        private const val MARKET_ID = "market_id"

        fun newIntentAddMarket(context: Context): Intent {
            return Intent(context, AddEditMarketActivity::class.java).apply {
            }
        }

        fun newIntentEditMarket(context: Context, marketId: String): Intent {
            return Intent(context, AddEditMarketActivity::class.java).apply {
                putExtra(MARKET_ID, marketId)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddEditMarketBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[AddEditMarketActivityViewModel::class.java]
        viewModel.marketId = intent.getStringExtra(MARKET_ID)


        setupToolbar()
        initFabAddMarket()
        setupInputName()
        setupInputColor()
        observeMarket()
        observeViewmodelErrorMessages()
        observeViewmodelFinishEvent()

        binding.edtName.showKeyboard()

    }

    private fun observeViewmodelErrorMessages() = lifecycleScope.launch {
        viewModel.errorEventLD.observe(this@AddEditMarketActivity) { event ->
            Snackbar.make(binding.root, event, Snackbar.LENGTH_LONG).show()
            Vibrator.error()
        }
    }

    private fun observeViewmodelFinishEvent() = lifecycleScope.launch {
        viewModel.finishEventLD.observe(this@AddEditMarketActivity) {
            finish()
        }
    }

    private fun observeMarket() = lifecycleScope.launch {
        viewModel.loadMarket()
        viewModel.editingMarketLD.observe(this@AddEditMarketActivity) {

            it?.let {
                viewModel.editingMarket = true
                updateViewModelAndUiWithEditableMarket(it)
            }
        }
    }

    private fun updateViewModelAndUiWithEditableMarket(market: Market) = binding.apply {
        viewModel.apply {

            edtName.setText(market.name)
            validatedName = market.name

            edtColor.hint = getString(R.string.Clique_aqui_para_alterar_a_cor)
            validatedColor = market.color
            changeEditDrawableTextColor()

            toolbar.tvActivityTitle.text = String.format(getString(R.string.Editar_x), market.name)
            fabSave.text = getString(R.string.Salvar_estabelecimento)

        }
    }

    /**
     * Configura o botão de salvar estabelecimento (FAB).
     */
    private fun initFabAddMarket() = binding.apply {
        fabSave.setOnClickListener {
            root.clearFocus()

            viewModel.apply {

                if (validatedName.isEmpty()) edtName.requestFocus()
                else if (validatedColor == -1) {
                    edtColor.requestFocus()
                } else {
                    root.clearFocus()
                    lifecycleScope.launch { tryAndSaveMarket() }
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
                val result = Market.Validator.validateName(term,this)

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
                val result = Market.Validator.validateColor(viewModel.validatedColor, this)

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
        BsdSelectColor.Builder(this, false)
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
        tvActivityTitle.text = getString(R.string.Adicionar_estabelecimento)
        ivGoBack.setOnClickListener { Vibrator.interaction();finish() }
        ivMenu.visibility = GONE
    }

}
