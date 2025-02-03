package dev.gmarques.compras.ui.profile

import android.app.Activity
import com.google.android.material.bottomsheet.BottomSheetDialog
import dev.gmarques.compras.data.model.ShopList
import dev.gmarques.compras.databinding.BsdSendSyncRequestBinding

class BsdSendSyncRequest(
    targetActivity: Activity,
) {
    constructor(targetActivity: Activity, editShopList: ShopList) : this(targetActivity)

    private var binding = BsdSendSyncRequestBinding.inflate(targetActivity.layoutInflater)
    private val dialog: BottomSheetDialog = BottomSheetDialog(targetActivity)
    private var onConfirmListener: ((ShopList) -> Unit)? = null

    init {
        dialog.setContentView(binding.root)

        binding.apply {

        }
    }



    fun setOnConfirmListener(listener: (ShopList) -> Unit): BsdSendSyncRequest {
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
