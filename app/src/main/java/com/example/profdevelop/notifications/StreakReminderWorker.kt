package com.example.profdevelop.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.profdevelop.MainActivity
import com.example.profdevelop.R
import com.example.profdevelop.data.local.AuthPreferencesDataSource
import com.example.profdevelop.data.local.SettingsPreferencesDataSource
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit

/**
 * Раз в день проверяет — если у пользователя сегодня ещё не было активности и
 * есть текущая серия (огонёк горит), показываем нотификацию «Серия в опасности 😱»,
 * чтобы он успел зайти и пройти урок до 24:00. Расписание управляется
 * настройкой "Напоминание" + час в Settings.
 */
class StreakReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val ctx = applicationContext

        val settings = SettingsPreferencesDataSource(ctx).flow.first()
        if (!settings.dailyReminderEnabled) return Result.success()

        val auth = AuthPreferencesDataSource(ctx).getStoredSession() ?: return Result.success()
        val user = auth.user

        val today = LocalDate.now().toString()
        val activeToday = user.lastActiveDate?.take(10) == today
        if (activeToday) return Result.success()
        if (user.streakDays <= 0) return Result.success()

        showReminderNotification(ctx, streakDays = user.streakDays)
        return Result.success()
    }

    private fun showReminderNotification(ctx: Context, streakDays: Int) {
        val mgr = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Серия и напоминания",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Уведомления о ежедневной серии и обучении"
            }
            mgr.createNotificationChannel(channel)
        }

        val intent = Intent(ctx, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pi = PendingIntent.getActivity(
            ctx,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(ctx, CHANNEL_ID)
            .setSmallIcon(R.drawable.burn)
            .setContentTitle("Серия x$streakDays под угрозой")
            .setContentText("Осталось мало времени \uD83D\uDE31 Заскочи на 5 минут, чтобы продлить огонёк.")
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "Осталось мало времени \uD83D\uDE31 До конца дня всего пара часов — пройди один урок на 100%, чтобы серия не сгорела."
                )
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pi)
            .setAutoCancel(true)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                ctx,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }
        NotificationManagerCompat.from(ctx).notify(NOTIFICATION_ID, notification)
    }

    companion object {
        const val CHANNEL_ID = "streak_reminder"
        const val NOTIFICATION_ID = 1001
        const val WORK_NAME = "streak_reminder_work"

        fun schedule(context: Context, hourOfDay: Int) {
            val initialDelayMinutes = computeInitialDelayMinutes(hourOfDay)
            val request = PeriodicWorkRequestBuilder<StreakReminderWorker>(
                1, TimeUnit.DAYS
            )
                .setInitialDelay(initialDelayMinutes, TimeUnit.MINUTES)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }

        private fun computeInitialDelayMinutes(hourOfDay: Int): Long {
            val hour = hourOfDay.coerceIn(0, 23)
            val now = LocalDateTime.now()
            val next = LocalDateTime.of(now.toLocalDate(), LocalTime.of(hour, 0))
                .let { if (it.isBefore(now)) it.plusDays(1) else it }
            val nowMillis = now.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val nextMillis = next.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            return ((nextMillis - nowMillis) / 60_000L).coerceAtLeast(1L)
        }
    }
}
