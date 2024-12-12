package dev.gmarques.compras.ui.main_activity

import android.app.Activity
import android.graphics.Color
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import dev.gmarques.compras.R
import dev.gmarques.compras.data.data.model.ShopList
import dev.gmarques.compras.data.data.repository.ShopListRepository
import dev.gmarques.compras.databinding.BottomSheetDialogBinding
import dev.gmarques.compras.ui.Vibrator
import dev.gmarques.compras.utils.Result

class AddShopListBottomSheetDialog(targetActivity: Activity, private val totalLists: Int) {

    private var pastelColors: List<Int>
    private var binding = BottomSheetDialogBinding.inflate(targetActivity.layoutInflater)
    private val dialog: BottomSheetDialog = BottomSheetDialog(targetActivity)
    private var onConfirmListener: ((ShopList) -> Unit)? = null

    init {
        dialog.setContentView(binding.root)

        binding.apply {
            fabConfirm.setOnClickListener {
                val name = edtInput.text.toString().trim()
                validateNameAndClose(name)
            }
        }

        pastelColors = targetActivity.resources.getStringArray(R.array.pastel_colors)
            .asList().map { Color.parseColor(it) }
    }

    private fun getPastelColor(index: Int): Int {
        // divido o indice pelo tamanho da lista e obtenho o resto, que sempre estara dentro do indice da lista
        val safeIndex = index % pastelColors.size
        return pastelColors[safeIndex]
    }

    private fun validateNameAndClose(input: String) {
        val result = ShopListRepository.validateNameAndGenerateList(input)

        if (result is Result.Error) {
            Snackbar.make(binding.root, result.exception.message!!, Snackbar.LENGTH_LONG).show()
            Vibrator.error()
        } else if (result is Result.Success) {
            val newList = result.data
            newList.color = getPastelColor(totalLists)
            onConfirmListener?.invoke(newList)
            dialog.dismiss()
            Vibrator.success()
        }


    }

    fun setOnConfirmListener(listener: (ShopList) -> Unit): AddShopListBottomSheetDialog {
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
