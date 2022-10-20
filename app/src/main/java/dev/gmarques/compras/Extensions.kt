package dev.gmarques.compras

import android.graphics.Paint
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

    }

}