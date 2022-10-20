package dev.gmarques.compras

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

object Vibrador {
    private var vib: Vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager =
            App.get.applicationContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator

    } else @Suppress("DEPRECATION") App.get.applicationContext.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

    fun vibErro() =
        vib.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))

    fun vibInteracao() =
        vib.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))


    fun vibSucesso() {
        val vibra = 30L
        val espera = 30L
        vib.vibrate(VibrationEffect.createWaveform(longArrayOf(vibra, espera, vibra, espera, vibra, espera),
            VibrationEffect.DEFAULT_AMPLITUDE))
    }
}