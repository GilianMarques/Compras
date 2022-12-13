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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import java.text.NumberFormat


object Extensions {

    fun String.capitalizar(): String =
            if (this.isNotEmpty()) this[0].uppercase() + this.substring(1).lowercase()
            else this

    fun String.formatarHtml(): Spanned =
            HtmlCompat.fromHtml(this, HtmlCompat.FROM_HTML_MODE_LEGACY)

    fun Float.emMoeda(): String = NumberFormat.getCurrencyInstance().format(this)

    fun Double.emMoeda(): String = NumberFormat.getCurrencyInstance().format(this)

    fun TextView.riscarTexto(shouldStrike: Boolean) {
        paintFlags = if (shouldStrike) {
            paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }

    }

    fun EditText.mostrarTeclado() = post {
        requestFocus()
        setSelection(text.length)
        val imm =
                context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
    }

    fun View.ocultarTeclado() {
        val imm = ContextCompat.getSystemService(context,
            InputMethodManager::class.java) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }

    /**
     * Usar apenas e o Layoutmanager do recyclerview for LinearLayoutManager
     */
    fun RecyclerView.smoothScroolToPosition(
        position: Int,
        snapMode: Int = LinearSmoothScroller.SNAP_TO_ANY,
    ) {

        val smoothScroller = object : LinearSmoothScroller(this.context) {
            override fun getVerticalSnapPreference(): Int = snapMode
            override fun getHorizontalSnapPreference(): Int = snapMode
            override fun calculateTimeForScrolling(dx: Int): Int {
                return 65//.coerceAtLeast(100 / childCount)
            }
        }

        val layoutManager = layoutManager as LinearLayoutManager
        val ultimaViewNaTela = layoutManager.findLastCompletelyVisibleItemPosition()

        if (ultimaViewNaTela == -1) return

        if (ultimaViewNaTela <= position)
            smoothScroller.targetPosition =
                    (position + 1).coerceAtMost(adapter!!.itemCount - 1)
        else smoothScroller.targetPosition = (position - 1).coerceAtLeast(0)

        layoutManager.startSmoothScroll(smoothScroller)

    }
}