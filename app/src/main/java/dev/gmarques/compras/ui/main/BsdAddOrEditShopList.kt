package dev.gmarques.compras.ui.main

import android.app.Activity
import android.graphics.Color
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import dev.gmarques.compras.R
import dev.gmarques.compras.data.data.model.ShopList
import dev.gmarques.compras.data.data.repository.ShopListRepository
import dev.gmarques.compras.databinding.BsdAddShoplistDialogBinding
import dev.gmarques.compras.ui.Vibrator
import dev.gmarques.compras.utils.Result

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
                edtInput.setText(editShopList.name)

            } else {
                pastelColors =
                    targetActivity.resources.getStringArray(R.array.pastel_colors).asList().map { Color.parseColor(it) }
            }
        }
    }

    private fun validateUserInput(name: String) {

        val result = ShopListRepository.validateName(name)

        if (result is Result.Success) {

            if (editShopList != null) updateListAndClose(name)
            else createListAndClose(name)
            Vibrator.success()

        } else if (result is Result.Error) {
            Snackbar.make(binding.root, result.exception.message!!, Snackbar.LENGTH_LONG).show()
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

        // Acesse o comportamento do BottomSheet e defina o estado para expandido
        // val behavior = BottomSheetBehavior.from(binding.root.parent as View)
        // behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }


}
