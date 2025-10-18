package dev.gmarques.compras.presenter.products

import android.app.Activity
import android.view.View
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import dev.gmarques.compras.data.PreferencesHelper
import dev.gmarques.compras.data.PreferencesHelper.PrefsDefaultValue
import dev.gmarques.compras.data.PreferencesHelper.PrefsKeys.Companion.BOUGHT_PRODUCTS_AT_END
import dev.gmarques.compras.data.PreferencesHelper.PrefsKeys.Companion.SORT_ASCENDING
import dev.gmarques.compras.data.PreferencesHelper.PrefsKeys.Companion.SORT_CRITERIA
import dev.gmarques.compras.databinding.BsdSortProductsDialogBinding
import dev.gmarques.compras.domain.SortCriteria

class BsdSortProducts(
    targetActivity: Activity,
    private var onConfirmListener: (() -> Unit),
) {

    private var binding = BsdSortProductsDialogBinding.inflate(targetActivity.layoutInflater)
    private val dialog: BottomSheetDialog = BottomSheetDialog(targetActivity)

    private var sortCriteria: SortCriteria = PrefsDefaultValue.SORT_CRITERIA
    private var sortAscending = PrefsDefaultValue.SORT_ASCENDING
    private var boughtProductsAtEnd = PrefsDefaultValue.BOUGHT_PRODUCTS_AT_END

    init {
        dialog.setContentView(binding.root)
        loadPreferences()
        setupListeners()
        updateUiWithDefaults()
    }

    private fun loadPreferences() {
        val prefs = PreferencesHelper()
        sortCriteria = SortCriteria.fromValue(prefs.getValue(SORT_CRITERIA, PrefsDefaultValue.SORT_CRITERIA.value))!!
        sortAscending = prefs.getValue(SORT_ASCENDING, PrefsDefaultValue.SORT_ASCENDING)
        boughtProductsAtEnd = prefs.getValue(BOUGHT_PRODUCTS_AT_END, PrefsDefaultValue.BOUGHT_PRODUCTS_AT_END)
    }

    private fun updateUiWithDefaults() = binding.apply {

        rbName.isChecked = sortCriteria == SortCriteria.NAME
        rbCreationDate.isChecked = sortCriteria == SortCriteria.CREATION_DATE
        rbCategory.isChecked = sortCriteria == SortCriteria.CATEGORY
        rbPosition.isChecked = sortCriteria == SortCriteria.POSITION

        swAscendingOrder.isChecked = sortAscending
        swBoughtAtEnd.isChecked = boughtProductsAtEnd


    }

    private fun setupListeners() = binding.apply {

        rbName.setOnCheckedChangeListener { _, checked ->
            if (checked) sortCriteria = SortCriteria.NAME
        }

        rbCategory.setOnCheckedChangeListener { _, checked ->
            if (checked) sortCriteria = SortCriteria.CATEGORY
        }

        rbCreationDate.setOnCheckedChangeListener { _, checked ->
            if (checked) sortCriteria = SortCriteria.CREATION_DATE
        }

        rbPosition.setOnCheckedChangeListener { _, checked ->
            if (checked) sortCriteria = SortCriteria.POSITION
        }

        swAscendingOrder.setOnCheckedChangeListener { _, checked ->
            sortAscending = checked
        }

        swBoughtAtEnd.setOnCheckedChangeListener { _, checked ->
            boughtProductsAtEnd = checked
        }

        fabConfirm.setOnClickListener {
            savePrefsAndDismiss()
        }

    }

    private fun savePrefsAndDismiss() {
        val prefs = PreferencesHelper()

        prefs.saveValue(SORT_CRITERIA, sortCriteria.value)
        prefs.saveValue(SORT_ASCENDING, sortAscending)
        prefs.saveValue(BOUGHT_PRODUCTS_AT_END, boughtProductsAtEnd)

        onConfirmListener()
        dialog.dismiss()
    }

    fun show() {

        dialog.show()

        
        val behavior = BottomSheetBehavior.from(binding.root.parent as View)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }


}
