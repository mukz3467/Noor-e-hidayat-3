package com.example.data.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.data.repository.QuranAudioManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class PrayerNotificationSettings(
    val fajrEnabled: Boolean = true,
    val dhuhrEnabled: Boolean = true,
    val asrEnabled: Boolean = true,
    val maghribEnabled: Boolean = true,
    val ishaEnabled: Boolean = true,
    val adhanSound: String = "makkah", // "makkah", "madina", "fajr", "system", "silent"
    val vibrationEnabled: Boolean = true,
    val leadTimeMinutes: Int = 0 // 0, 5, 10, 15
)

object PrayerNotificationManager {

    const val CHANNEL_ID = "noor_prayer_channel"
    private const val PREFS_NAME = "prayer_notif_prefs"

    private val _settings = MutableStateFlow(PrayerNotificationSettings())
    val settings: StateFlow<PrayerNotificationSettings> = _settings.asStateFlow()

    private var isInitialized = false

    fun init(context: Context) {
        if (isInitialized) return
        isInitialized = true
        createNotificationChannel(context)
        loadSettings(context)
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Prayer Times & Adhan"
            val descriptionText = "Notifications and Adhan calls for obligatory prayers"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun loadSettings(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        _settings.value = PrayerNotificationSettings(
            fajrEnabled = prefs.getBoolean("fajr_enabled", true),
            dhuhrEnabled = prefs.getBoolean("dhuhr_enabled", true),
            asrEnabled = prefs.getBoolean("asr_enabled", true),
            maghribEnabled = prefs.getBoolean("maghrib_enabled", true),
            ishaEnabled = prefs.getBoolean("isha_enabled", true),
            adhanSound = prefs.getString("adhan_sound", "makkah") ?: "makkah",
            vibrationEnabled = prefs.getBoolean("vibration_enabled", true),
            leadTimeMinutes = prefs.getInt("lead_time_minutes", 0)
        )
    }

    fun updateSettings(context: Context, newSettings: PrayerNotificationSettings) {
        _settings.value = newSettings
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().apply {
            putBoolean("fajr_enabled", newSettings.fajrEnabled)
            putBoolean("dhuhr_enabled", newSettings.dhuhrEnabled)
            putBoolean("asr_enabled", newSettings.asrEnabled)
            putBoolean("maghrib_enabled", newSettings.maghribEnabled)
            putBoolean("isha_enabled", newSettings.ishaEnabled)
            putString("adhan_sound", newSettings.adhanSound)
            putBoolean("vibration_enabled", newSettings.vibrationEnabled)
            putInt("lead_time_minutes", newSettings.leadTimeMinutes)
            apply()
        }
    }

    fun sendTestNotification(context: Context, prayerName: String = "Asr", prayerTime: String = "04:15 PM") {
        init(context)
        if (!hasNotificationPermission(context)) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val soundType = _settings.value.adhanSound
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("🕌 Time for $prayerName Prayer ($prayerTime)")
            .setContentText("Hayya 'alas-Salah! Prayer is better than sleep and worldly pursuits.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        if (_settings.value.vibrationEnabled) {
            builder.setVibrate(longArrayOf(0, 400, 200, 400))
        }

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(101, builder.build())

        // Play Adhan audio preview if not silent
        if (soundType != "silent") {
            val adhanId = if (soundType == "madina") "madina" else if (soundType == "fajr") "fajr" else "makkah"
            QuranAudioManager.playAzan(context, adhanId)
        }
    }
}
