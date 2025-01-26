package dev.gmarques.compras.ui.main

import android.app.Activity
import android.graphics.Color
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import dev.gmarques.compras.App
import dev.gmarques.compras.R
import dev.gmarques.compras.data.model.ShopList
import dev.gmarques.compras.databinding.BsdAddShoplistDialogBinding
import dev.gmarques.compras.ui.Vibrator

class BsdAddOrEditShopList(
    targetActivity: Activity,
    private val totalLists: Int,
    private val editShopList: ShopList? = null,
) {
    constructor(targetActivity: Activity, editShopList: ShopList) : this(targetActivity, 0, editShopList)

    private var pastelColors: List<Int>? = null
    private var binding = BsdAddShoplistDialogBinding.inflate(targetActivity.layoutInflater)
    private val dialog: BottomSheetDialog = BottomSheetDialog(targetActivity)
    private var onConfirmListener: ((ShopList) -> Unit)? = null

    init {
        dialog.setContentView(binding.root)

        binding.apply {

            fabConfirm.setOnClickListener {
                val name = edtInput.text.toString().trim()
                validateUserInput(name)
            }

            if (editShopList != null) {
                tvTitle.text = targetActivity.getString(R.string.Renomear_lista)
                fabConfirm.text = targetActivity.getString(R.string.Salvar)
                edtInput.setText(editShopList.name)

            } else {
                pastelColors =
                    targetActivity.resources.getStringArray(R.array.pastel_colors).asList().map { Color.parseColor(it) }

                val currentMonthName = java.text.DateFormatSymbols().months[java.util.Calendar.getInstance().get(java.util.Calendar.MONTH)]
                edtInput.setText(targetActivity.getString(R.string.Compras_de_x, currentMonthName))

            }
        }
    }

    private fun validateUserInput(name: String) {

        val result = ShopList.Validator.validateName(name, App.getContext())

        if (result.isSuccess) {

            if (editShopList != null) updateListAndClose(name)
            else createListAndClose(name)
            Vibrator.success()

        } else if (result.isFailure) {
            Snackbar.make(binding.root, result.exceptionOrNull()?.message!!, Snackbar.LENGTH_LONG).show()
            Vibrator.error()
        }
    }

    private fun createListAndClose(name: String) {
        val newList = ShopList(name, getPastelColor(totalLists))
        onConfirmListener?.invoke(newList)
        dialog.dismiss()
    }

    private fun updateListAndClose(newName: String) {
        val edited = editShopList!!.copy(name = newName)
        onConfirmListener?.invoke(edited)
        dialog.dismiss()
    }

    private fun getPastelColor(index: Int): Int {
        // divido o indice pelo tamanho da lista e obtenho o resto, que sempre estara dentro do indice da lista
        val safeIndex = index % pastelColors!!.size
        return pastelColors!![safeIndex]
    }

    fun setOnConfirmListener(listener: (ShopList) -> Unit): BsdAddOrEditShopList {
        onConfirmListener = listener
        return this
    }

    fun show() {

        dialog.show()
        binding.edtInput.requestFocus()

        
        // val behavior = BottomSheetBehavior.from(binding.root.parent as View)
        // behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }


}
