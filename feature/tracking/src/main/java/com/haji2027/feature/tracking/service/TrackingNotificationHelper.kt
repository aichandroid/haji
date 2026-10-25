package com.haji2027.feature.tracking.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.haji2027.feature.tracking.domain.MovementMode

/**
 * Pengelola notifikasi status bar persisten untuk Android Foreground Service.
 * Selaras dengan FR-TRACK-004 dan Bagian 17.5.2 SYSTEM_DESIGN_DOCUMENT.md.
 */
class TrackingNotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "channel_hajj_tracking"
        const val CHANNEL_NAME = "Pelacakan Lokasi Keluarga Haji"
        const val NOTIFICATION_ID = 1001

        const val ACTION_STOP_TRACKING = "com.haji2027.tracking.ACTION_STOP_TRACKING"
    }

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW // Rendah: hening tanpa suara berulang
            ).apply {
                description = "Notifikasi persisten saat jemaah berbagi lokasi dengan keluarga"
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Membentuk notifikasi persisten yang tidak dapat di-swipe dismiss jemaah.
     */
    fun buildNotification(
        movementMode: MovementMode = MovementMode.MOVING,
        lastUpdatedText: String? = null
    ): Notification {
        val statusText = when (movementMode) {
            MovementMode.MOVING -> "Status: Bergerak (Pembaruan tiap 30 dtk)"
            MovementMode.STATIONARY -> "Status: Istirahat / Tenda (Pembaruan tiap 5 mnt)"
        }

        val contentText = if (lastUpdatedText != null) {
            "$statusText • $lastUpdatedText"
        } else {
            statusText
        }

        // PendingIntent untuk menghentikan pelacakan langsung dari notifikasi
        val stopIntent = Intent(context, LocationTrackingService::class.java).apply {
            action = ACTION_STOP_TRACKING
        }
        val stopPendingIntent = PendingIntent.getService(
            context,
            1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // PendingIntent untuk membuka aplikasi
        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        val openAppPendingIntent = if (launchIntent != null) {
            PendingIntent.getActivity(
                context,
                0,
                launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        } else null

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentTitle("Berbagi Lokasi Keluarga Aktif")
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true) // Tidak dapat di-swipe dismiss (FR-TRACK-004)
            .setAutoCancel(false)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Hentikan",
                stopPendingIntent
            )

        if (openAppPendingIntent != null) {
            builder.setContentIntent(openAppPendingIntent)
            builder.addAction(
                android.R.drawable.ic_menu_view,
                "Buka Aplikasi",
                openAppPendingIntent
            )
        }

        return builder.build()
    }

    fun updateNotification(movementMode: MovementMode, lastUpdatedText: String? = null) {
        val notification = buildNotification(movementMode, lastUpdatedText)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}
