package dev.gmarques.compras.utils

import android.text.Spanned
import androidx.core.text.HtmlCompat

class ExtFun {
    companion object {

        fun String.formatHtml(): Spanned {
            return HtmlCompat.fromHtml(this, HtmlCompat.FROM_HTML_MODE_LEGACY)
        }
    }


}