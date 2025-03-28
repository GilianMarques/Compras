package dev.gmarques.compras.ui.products

import android.app.Activity
import android.view.View.VISIBLE
import com.google.android.material.bottomsheet.BottomSheetDialog
import dev.gmarques.compras.BuildConfig
import dev.gmarques.compras.data.PreferencesHelper
import dev.gmarques.compras.data.model.ShopList
import dev.gmarques.compras.databinding.BsdShoplistMenuDialogBinding

class BsdShopListMenu private constructor(
    targetActivity: Activity,
    private val shopList: ShopList,
    private val renameListener: ((ShopList) -> Unit)?,
    private val removeListener: ((ShopList) -> Unit)?,
    private val orderListener: (() -> Unit)?,
    private val suggestionListener: (() -> Unit)?,
    private val manageCategoriesListener: (() -> Unit)?,
    private val manageEstablishmentsListener: (() -> Unit)?,
) {

    private var binding = BsdShoplistMenuDialogBinding.inflate(targetActivity.layoutInflater)
    private val dialog: BottomSheetDialog = BottomSheetDialog(targetActivity)

    init {
        dialog.setContentView(binding.root)

        binding.apply {
            tvRemove.setOnClickListener {
                removeListener?.invoke(shopList)
                dialog.dismiss()
            }

            tvRename.setOnClickListener {
                renameListener?.invoke(shopList)
                dialog.dismiss()
            }

            tvSortProducts.setOnClickListener {
                orderListener?.invoke()
                dialog.dismiss()
            }

            tvProductSuggestion.setOnClickListener {
                suggestionListener?.invoke()
                dialog.dismiss()
            }

            tvManageCategories.setOnClickListener {
                manageCategoriesListener?.invoke()
                dialog.dismiss()
            }

            tvManageEstablishments.setOnClickListener {
                manageEstablishmentsListener?.invoke()
                dialog.dismiss()
            }



            tvTitle.text = shopList.name
        }
    }

    fun show() {
        dialog.show()
    }

    class Builder(private val targetActivity: Activity, private val shopList: ShopList) {
        private var renameListener: ((ShopList) -> Unit)? = null
        private var removeListener: ((ShopList) -> Unit)? = null
        private var orderListener: (() -> Unit)? = null
        private var suggestionListener: (() -> Unit)? = null
        private var manageCategoriesListener: (() -> Unit)? = null
        private var manageEstablishmentsListener: (() -> Unit)? = null

        fun setRenameListener(listener: (ShopList) -> Unit) = apply {
            this.renameListener = listener
        }

        fun setRemoveListener(listener: (ShopList) -> Unit) = apply {
            this.removeListener = listener
        }

        fun setSortListener(listener: () -> Unit) = apply {
            this.orderListener = listener
        }

        fun setSuggestionListener(listener: () -> Unit) = apply {
            this.suggestionListener = listener
        }

        fun setManageCategoriesListener(listener: () -> Unit) = apply {
            this.manageCategoriesListener = listener
        }

        fun setManageEstablishmentsListener(listener: () -> Unit) = apply {
            this.manageEstablishmentsListener = listener
        }

        fun build(): BsdShopListMenu {

            requireNotNull(renameListener) { "renameListener must be set." }
            requireNotNull(removeListener) { "removeListener must be set." }
            requireNotNull(orderListener) { "orderListener must be set." }
            requireNotNull(suggestionListener) { "suggestionListener must be set." }
            requireNotNull(manageCategoriesListener) { "manageCategoriesListener must be set." }
            requireNotNull(manageEstablishmentsListener) { "manageEstablishmentsListener must be set." }

            return BsdShopListMenu(
                targetActivity,
                shopList,
                renameListener,
                removeListener,
                orderListener,
                suggestionListener,
                manageCategoriesListener,
                manageEstablishmentsListener
            )
        }
    }
}
