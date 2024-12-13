package dev.gmarques.compras.utils

import android.graphics.Color
import android.text.Spanned
import androidx.core.text.HtmlCompat
import java.text.NumberFormat
import java.util.Locale

class ExtFun {
    companion object {

        fun String.formatHtml(): Spanned {
            return HtmlCompat.fromHtml(this, HtmlCompat.FROM_HTML_MODE_LEGACY)
        }

        /**
         * Formata o valor Double como moeda considerando a localidade do usuário.
         */
        fun Double.toCurrency(locale: Locale = Locale.getDefault()): String {
            val currencyFormatter = NumberFormat.getCurrencyInstance(locale)
            return currencyFormatter.format(this)
        }

        fun Int.adjustSaturation(@Suppress("SameParameterValue") factor: Float): Int {
            // Converte a cor para HSL
            val hsl = FloatArray(3)
            Color.colorToHSV(this, hsl)

            // Ajusta a saturação (clamp entre 0 e 1)
            hsl[1] = (hsl[1] * factor).coerceIn(0f, 1f)

            // Converte de volta para a cor RGB
            return Color.HSVToColor(hsl)
        }
    }


}