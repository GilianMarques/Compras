package dev.gmarques.compras.ui.products

import android.app.Activity
import android.view.View.VISIBLE
import com.google.android.material.bottomsheet.BottomSheetDialog
import dev.gmarques.compras.BuildConfig
import dev.gmarques.compras.data.PreferencesHelper
import dev.gmarques.compras.data.model.ShopList
import dev.gmarques.compras.databinding.BsdShoplistMenuDialogBinding

class BsdShopListMenu(
    targetActivity: Activity,
    private val shopList: ShopList,
    private var renameListener: ((ShopList) -> Unit),
    private var removeListener: ((ShopList) -> Unit),
    private var orderListener: (() -> Unit),
    private var suggestionListener: (() -> Unit),
) {

    private var binding = BsdShoplistMenuDialogBinding.inflate(targetActivity.layoutInflater)
    private val dialog: BottomSheetDialog = BottomSheetDialog(targetActivity)


    init {
        dialog.setContentView(binding.root)

        binding.apply {

            tvRemove.setOnClickListener {
                removeListener(shopList)
                dialog.dismiss()
            }

            tvRename.setOnClickListener {
                renameListener(shopList)
                dialog.dismiss()
            }

            tvSortProducts.setOnClickListener {
                orderListener()
                dialog.dismiss()
            }

            tvProductSuggestion.setOnClickListener {
                suggestionListener()
                dialog.dismiss()
            }

            if (BuildConfig.DEBUG) {
                cbProductionDatabase.visibility = VISIBLE

                cbProductionDatabase.isChecked =
                    PreferencesHelper().getValue(PreferencesHelper.PrefsKeys.PRODUCTION_DATABASE, false)

                cbProductionDatabase.setOnCheckedChangeListener { _, checked ->
                    PreferencesHelper().saveValue(PreferencesHelper.PrefsKeys.PRODUCTION_DATABASE, checked)
                }
            }

            tvTitle.text = shopList.name
        }

    }


    fun show() {
        dialog.show()
    }


}
