package com.example.data.notification

import android.content.BroadcastReceiver
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.R
import com.example.data.repository.QuranAudioManager

class PrayerAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getStringExtra(EXTRA_ID) ?: return
        val prayerName = intent.getStringExtra(EXTRA_NAME) ?: "Prayer"
        val sound = intent.getStringExtra(EXTRA_SOUND) ?: "makkah"
        val vibration = intent.getBooleanExtra(EXTRA_VIBRATE, true)

        PrayerNotificationManager.postPrayerNotification(context, prayerName, sound, vibration, id.hashCode())
        PrayerNotificationManager.onAlarmDelivered(context, id)

        if (sound != "silent") AdhanPlaybackService.start(context, sound)
    }

    companion object {
        const val EXTRA_ID = "prayer_alarm_id"
        const val EXTRA_NAME = "prayer_alarm_name"
        const val EXTRA_SOUND = "prayer_alarm_sound"
        const val EXTRA_VIBRATE = "prayer_alarm_vibrate"
    }
}

class PrayerBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED -> PrayerNotificationManager.init(context)
        }
    }
}

class AdhanPlaybackService : Service() {
    private val handler = Handler(Looper.getMainLooper())
    private val timeout = Runnable { finishPlayback() }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val sound = intent?.getStringExtra(PrayerAlarmReceiver.EXTRA_SOUND) ?: "makkah"
        val notification = NotificationCompat.Builder(this, PrayerNotificationManager.CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Playing Adhan")
            .setContentText("Noor-e-Hidayat prayer audio")
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(202, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
        } else {
            startForeground(202, notification)
        }

        handler.removeCallbacks(timeout)
        handler.postDelayed(timeout, 8 * 60 * 1000L)
        QuranAudioManager.playAzan(applicationContext, sound) { finishPlayback() }
        return START_NOT_STICKY
    }

    private fun finishPlayback() {
        QuranAudioManager.stopMedia()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        handler.removeCallbacks(timeout)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        fun start(context: Context, sound: String) {
            val intent = Intent(context, AdhanPlaybackService::class.java)
                .putExtra(PrayerAlarmReceiver.EXTRA_SOUND, sound)
            try {
                ContextCompat.startForegroundService(context, intent)
            } catch (_: Exception) {
                // The prayer notification remains available if Android blocks background audio.
            }
        }
    }
}
