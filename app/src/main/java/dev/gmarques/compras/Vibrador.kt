package dev.gmarques.compras

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class Vibrador {
    private var vib: Vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager =
            App.get.applicationContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator

    } else @Suppress("DEPRECATION") App.get.applicationContext.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

    fun vibErro() =
        vib.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))


    fun vibSucesso() =
        vib.vibrate(VibrationEffect.createWaveform(longArrayOf(10, 30, 10, 30, 10, 30),
            VibrationEffect.DEFAULT_AMPLITUDE))
}