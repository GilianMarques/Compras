package dev.gmarques.compras

import android.content.Context
import android.graphics.Paint
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import java.text.NumberFormat


class Extensions {
    companion object {

        fun String.capitalizar(): String = this[0].uppercase() + this.substring(1).lowercase()

        fun Float.emMoeda(): String = NumberFormat.getCurrencyInstance().format(this)

        fun TextView.strikeThrough(shouldStrike: Boolean) {
            paintFlags = if (shouldStrike) {
                paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }

        }

        fun EditText.showKeyboard() = post {
            requestFocus()
            setSelection(text.length)
            val imm =
                context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
        }


        fun View.hideSoftKeyboard(mEtSearch: EditText, context: Context) {
            mEtSearch.clearFocus()
            val imm: InputMethodManager =
                context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(mEtSearch.windowToken, 0)
        }
    }
}