package dev.gmarques.compras.ui

import android.content.Context
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import dev.gmarques.compras.utils.App

/**
 * Classe responsável por fornecer feedback de interface, como vibração.
 * Suporta APIs abaixo de 26 utilizando o méto_do `vibrate` legada para compatibilidade.
 */
object Vibrator {

    /**
     * Vibra o dispositivo para fornecer feedback tátil ao usuário em caso de sucesso.
     * Realiza uma vibração de duração moderada.
     */
    fun error() {
        vibrate(300)
    }

    /**
     * Vibra o dispositivo para fornecer feedback tátil ao usuário em caso de erro.
     * Realiza cinco vibrações rápidas.
     */
    fun success() {
        for (i in 1..4) {
            vibrate(50)
            Thread.sleep(110)
        }
    }

    /**
     * Vibra o dispositivo para fornecer feedback tátil ao usuário em caso de interação.
     * Realiza uma vibração curta.
     */
    fun interaction() {
        vibrate(50) // Duração curta
    }

    /**
     * Vibra o dispositivo para fornecer feedback tátil ao usuário.
     * Utiliza `VibrationEffect` para APIs >= 26 e o méto_do `vibrate` legado para versões anteriores.
     *
     * @param duration A duração da vibração em milissegundos.
     */
    private fun vibrate(duration: Long) {
        val context = App.getContext()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {  // API 31 e superior (Android 12+)
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            val vibrationEffect = VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE)
            val combinedVibration = CombinedVibration.createParallel(vibrationEffect)
            vibratorManager.vibrate(combinedVibration)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {  // API 26 a 30 (Android 8 a 11)
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            val vibrationEffect = VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE)
            vibrator.vibrate(vibrationEffect)
        } else {  // APIs abaixo de 26
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(duration)
        }
    }

}
