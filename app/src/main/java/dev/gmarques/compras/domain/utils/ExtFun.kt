package dev.gmarques.compras.domain.utils

import android.content.Context
import android.graphics.Color
import android.text.Spanned
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.text.HtmlCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import dev.gmarques.compras.App
import java.text.Normalizer
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

class ExtFun {
    companion object {

        fun String.formatHtml(): Spanned {
            return HtmlCompat.fromHtml(this, HtmlCompat.FROM_HTML_MODE_LEGACY)
        }

        /**
         * Converte a string com valor monetário ou numérico em um double.
         * Lida com valores em formato de moeda ou diretamente numéricos corretamente formatados ou com erros de formatação.
         * Retorna 0.0 caso usado em uma string vazia
         */
        fun String.currencyToDouble(): Double {
            return if (this.isEmpty()) 0.0
            else try {
                NumberFormat.getCurrencyInstance(Locale.getDefault()).parse(this)!!.toDouble()
            } catch (e: Exception) {
                val newDecSep = "_"
                val decimalSeparatorIndex = max(this.indexOfLast { it == '.' }, this.indexOfLast { it == ',' })

                return if (decimalSeparatorIndex == -1) {
                    this.replace("\\D".toRegex(), "").toDouble()
                } else (this.substring(0, decimalSeparatorIndex) + newDecSep +
                        this.substring(min(decimalSeparatorIndex + 1, this.length)))
                    .replace("[^0-9$newDecSep]".toRegex(), "")
                    .replace(newDecSep, ".")
                    .toDouble()

            }
        }

        /**
         * Remove tudo que nao for numeros de uma string, retornando um numero inteiro
         * retornara 0 se chamado em uma string vazia
         * numeros negativos serao retornados como positivos uma vez que o regex removera o sinal de menos antes de converter a string em numero
         */
        fun String.onlyIntegerNumbers(): Int {
            return this.trim()
                .replace(Regex("[^0-9]"), "")
                .ifBlank { "0" }
                .toInt()
        }

        /**
         * Remove todos os espaços multiplos em branco de uma string, alem dos espaços no inicio e fim
         */
        fun String.removeSpaces(): String {
            return this.replace("\\s+".toRegex(), " ").trim()
        }

        fun String.removeAccents(): String {
            return Normalizer.normalize(this, Normalizer.Form.NFD) // split 'ã' to 'a' + '~'
                .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "") // remove all accents

        }

        //-------------------------------------------------------------------

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
         * Converte um valor em dp para pixels.
         * Se o valor da variavel this for 4 o resultado sera o total de pixels que 4dp equivale
         */
        fun Int.dp(): Int {
            return (this * App.getContext().resources.displayMetrics.density).toInt()
        }

        fun <T> LiveData<T>.observeOnce(owner: LifecycleOwner, observer: (T) -> Unit) {
            observe(owner, object : Observer<T> {
                override fun onChanged(value: T) {
                    removeObserver(this)
                    observer(value)
                }
            })
        }

        fun View.showKeyboard() {
            this.requestFocus() // Garante que a View tenha o foco
            val imm = this.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
        }

        /**
         * Oculta o teclado virtual no Android.
         */
        fun View.hideKeyboard() {
            this.clearFocus()
            val imm = App.getContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(this.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
        }
    }
}