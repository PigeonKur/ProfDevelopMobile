package com.example.profdevelop.presentation.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

object Haptics {

    /** Управляется из настроек пользователя. */
    @Volatile
    var enabled: Boolean = true

    /**
     * Короткая вибрация средней силы — «огонёк загорается» при продлении серии.
     */
    fun streakFlame(context: Context) {
        vibrate(context, durationMs = 80, amplitude = 160)
    }

    /**
     * Лёгкий тик при заполнении XP-шкалы.
     */
    fun xpTick(context: Context) {
        vibrate(context, durationMs = 25, amplitude = 80)
    }

    private fun vibrate(context: Context, durationMs: Long, amplitude: Int) {
        if (!enabled) return
        val vibrator = obtainVibrator(context) ?: return
        if (!vibrator.hasVibrator()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(durationMs, amplitude))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(durationMs)
        }
    }

    private fun obtainVibrator(context: Context): Vibrator? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)
                ?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }
}
