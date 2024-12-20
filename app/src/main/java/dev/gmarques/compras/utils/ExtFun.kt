package dev.gmarques.compras.utils

import android.graphics.Color
import android.text.Spanned
import androidx.core.text.HtmlCompat
import java.text.NumberFormat
import java.text.ParseException
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

        /**
         * Converte a string com valor monetário ou numérico em um double.
         * Lida com valores em formato de moeda ou diretamente numéricos.
         * Pode ser chamada em uma string vazia.
         */
        fun String.currencyToDouble(): Double {
            return try {
                val format = NumberFormat.getCurrencyInstance(Locale.getDefault())
                val parsedValue = format.parse(this.ifBlank { 1.5.toCurrency() })
                parsedValue?.toDouble() ?: 0.0
            } catch (e: ParseException) {
                // Se falhar ao interpretar como moeda, tenta converter diretamente para número
                this.toDoubleOrNull() ?: 0.0
            }
        }


        /**
         * Remove tudo que nao for numeros de uma string, retornando um numero inteiro
         * pode ser chamado um uma string vazia
         */
        fun String.onlyIntegerNumbers(): Int {
            return this.trim()
                .replace(Regex("[^0-9]"), "")
                .ifBlank { "0" }
                .toInt()
        }
    }


}