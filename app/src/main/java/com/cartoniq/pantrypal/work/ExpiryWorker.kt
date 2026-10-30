package com.cartoniq.pantrypal.work

import android.Manifest
import android.annotation.SuppressLint
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
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.cartoniq.pantrypal.MainActivity
import com.cartoniq.pantrypal.R
import com.cartoniq.pantrypal.data.AppDatabase
import com.cartoniq.pantrypal.data.DateUtil
import com.cartoniq.pantrypal.viewmodel.KEY_ONBOARDED
import com.cartoniq.pantrypal.viewmodel.KEY_REMINDERS
import com.cartoniq.pantrypal.viewmodel.PREFS_NAME
import java.util.Calendar
import java.util.concurrent.TimeUnit

class ExpiryWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    @SuppressLint("MissingPermission")
    override suspend fun doWork(): Result {
        val ctx = applicationContext
        // Don't create the database or notify before the user has finished onboarding.
        val prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (!prefs.getBoolean(KEY_ONBOARDED, false) || !prefs.getBoolean(KEY_REMINDERS, true)) {
            return Result.success()
        }
        val today = DateUtil.today()
        val expiring = AppDatabase.get(ctx).pantryDao().getAllPantry()
            .filter { it.daysLeft(today) in -2..2 } // expired items nag for 2 days, not forever
            .sortedBy { it.expiryDate }
        if (expiring.isEmpty()) return Result.success()

        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return Result.success()
        }

        createChannel(ctx)
        val names = expiring.take(4).joinToString(", ") { it.name }
        val title = ctx.resources.getQuantityString(R.plurals.notif_title, expiring.size, expiring.size)
        val intent = Intent(ctx, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pending = PendingIntent.getActivity(
            ctx, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(ctx, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(ctx.getString(R.string.notif_text, names))
            .setStyle(NotificationCompat.BigTextStyle().bigText(ctx.getString(R.string.notif_text, names)))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pending)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(ctx).notify(NOTIFICATION_ID, notification)
        return Result.success()
    }

    companion object {
        const val CHANNEL_ID = "expiry_reminders"
        private const val NOTIFICATION_ID = 1001
        private const val WORK_NAME = "daily_expiry_check"

        fun createChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= 26) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    context.getString(R.string.notif_channel_name),
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply { description = context.getString(R.string.notif_channel_desc) }
                context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
            }
        }

        fun setEnabled(context: Context, enabled: Boolean) {
            if (enabled) schedule(context) else WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }

        /** Schedules a 24h periodic check whose first run happens at the next 8:00 AM. */
        fun schedule(context: Context) {
            val now = Calendar.getInstance()
            val next = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 8)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            if (!next.after(now)) next.add(Calendar.DAY_OF_YEAR, 1)
            val delay = next.timeInMillis - now.timeInMillis
            val request = PeriodicWorkRequestBuilder<ExpiryWorker>(24, TimeUnit.HOURS)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .build()
            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
        }
    }
}
