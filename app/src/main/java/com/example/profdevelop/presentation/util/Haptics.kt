package com.example.profdevelop.presentation.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

object Haptics {
    /** Короткая вибрация средней силы — момент «зажигания» серии. */
    fun streakFlame(context: Context) {
        vibrate(context, durationMs = 80, amplitude = MEDIUM_AMPLITUDE)
    }

    /** Лёгкая вибрация при заполнении полоски XP. */
    fun xpTick(context: Context) {
        vibrate(context, durationMs = 25, amplitude = LIGHT_AMPLITUDE)
    }

    private fun vibrate(context: Context, durationMs: Long, amplitude: Int) {
        val vibrator = resolveVibrator(context) ?: return
        if (!vibrator.hasVibrator()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                VibrationEffect.createOneShot(durationMs, amplitude)
            } else {
                VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE)
            }
            vibrator.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(durationMs)
        }
    }

    private fun resolveVibrator(context: Context): Vibrator? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            manager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    private const val LIGHT_AMPLITUDE = 80
    private const val MEDIUM_AMPLITUDE = 160
}
