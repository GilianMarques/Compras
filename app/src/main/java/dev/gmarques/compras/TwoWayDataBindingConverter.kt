package dev.gmarques.compras

import androidx.databinding.InverseMethod


object TwoWayDataBindingConverter {

    @InverseMethod("stringParaFloat")
    @JvmStatic
    fun floatParaString(value: Float): String {
        return value.toString()
    }

    @JvmStatic
    fun stringParaFloat( value: String): Float {
        return value.toFloat()
    }
}