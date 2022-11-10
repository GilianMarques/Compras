package dev.gmarques.compras

import android.content.Context
import android.graphics.Paint
import android.text.Spanned
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import java.text.NumberFormat


class Extensions {
    companion object {

        fun String.capitalizar(): String = this[0].uppercase() + this.substring(1).lowercase()

        fun String.fromHtml(): Spanned = HtmlCompat.fromHtml(this, HtmlCompat.FROM_HTML_MODE_LEGACY)


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


        fun View.hideSoftKeyboard() {
            val imm = ContextCompat.getSystemService(context, InputMethodManager::class.java) as InputMethodManager
            imm.hideSoftInputFromWindow(windowToken, 0)
        }
    }
}