package com.example.data.repository

import android.content.Context
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Dns
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.net.Inet4Address
import java.util.concurrent.TimeUnit

object QuranOfflineSyncManager {

    val isSyncing = mutableStateOf(false)
    val syncProgress = mutableFloatStateOf(0f)
    val offlineAudioCount = mutableIntStateOf(0)
    val isAutoSyncComplete = mutableStateOf(false)

    private var syncJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(12, TimeUnit.SECONDS)
            .readTimeout(25, TimeUnit.SECONDS)
            .dns(object : Dns {
                override fun lookup(hostname: String): List<java.net.InetAddress> {
                    return try {
                        val addresses = Dns.SYSTEM.lookup(hostname)
                        addresses.sortedBy { if (it is Inet4Address) 0 else 1 }
                    } catch (e: Exception) {
                        Dns.SYSTEM.lookup(hostname)
                    }
                }
            })
            .build()
    }

    /**
     * Start background auto-download of all core audios (Azan alarm bells, popular Surahs, Learning audios)
     */
    fun startAutoSync(context: Context) {
        if (isSyncing.value || isAutoSyncComplete.value) return

        val offlineDir = getOfflineStorageDir(context)

        // 1. First extract all pre-bundled offline assets (Azans & core Surahs) to storage
        try {
            val assetAudios = context.assets.list("audio") ?: emptyArray()
            for (assetName in assetAudios) {
                val destFile = File(offlineDir, assetName)
                if (!destFile.exists() || destFile.length() < 1024) {
                    context.assets.open("audio/$assetName").use { input ->
                        destFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                }
            }
        } catch (_: Exception) {}

        updateCachedCount(offlineDir)

        syncJob = scope.launch {
            isSyncing.value = true

            val syncList = getAutoSyncItems()
            var downloaded = 0
            val total = syncList.size

            for (item in syncList) {
                val targetFile = File(offlineDir, item.filename)
                if (!targetFile.exists() || targetFile.length() < 1024) {
                    downloadFile(item.url, item.fallbackUrl, targetFile)
                }
                downloaded++
                withContext(Dispatchers.Main) {
                    syncProgress.floatValue = downloaded.toFloat() / total
                    updateCachedCount(offlineDir)
                }
            }

            withContext(Dispatchers.Main) {
                isSyncing.value = false
                isAutoSyncComplete.value = true
                updateCachedCount(offlineDir)
            }
        }
    }

    fun getOfflineStorageDir(context: Context): File {
        return File(context.filesDir, "quran_offline_audio").apply {
            if (!exists()) mkdirs()
        }
    }

    fun updateCachedCount(dir: File) {
        try {
            val count = dir.listFiles { file -> file.isFile && file.length() > 1024 }?.size ?: 0
            offlineAudioCount.intValue = count
        } catch (_: Exception) {}
    }

    private fun downloadFile(url: String, fallbackUrl: String?, targetFile: File) {
        val tempFile = File(targetFile.parentFile, "${targetFile.name}.tmp")
        if (tryDownload(url, tempFile)) {
            tempFile.renameTo(targetFile)
            return
        }
        if (!fallbackUrl.isNullOrBlank()) {
            if (tryDownload(fallbackUrl, tempFile)) {
                tempFile.renameTo(targetFile)
            }
        }
    }

    private fun tryDownload(url: String, destination: File): Boolean {
        return try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Noor-e-Hidayat/2.0 (Android; OfflineAutoSync)")
                .build()

            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                response.close()
                return false
            }
            val body = response.body ?: return false
            destination.outputStream().use { output ->
                body.byteStream().use { input ->
                    input.copyTo(output)
                }
            }
            destination.length() > 1024
        } catch (_: Exception) {
            try { destination.delete() } catch (_: Exception) {}
            false
        }
    }

    private data class SyncItem(
        val filename: String,
        val url: String,
        val fallbackUrl: String? = null
    )

    private fun getAutoSyncItems(): List<SyncItem> {
        val list = mutableListOf<SyncItem>()

        // 1. Azan Alarm Bells (Makkah, Madinah, Fajr)
        list.add(
            SyncItem(
                filename = "azan_makkah.mp3",
                url = "https://www.islamcan.com/audio/adhan/azan1.mp3",
                fallbackUrl = "https://ia800201.us.archive.org/29/items/AdhanMakkah/AdhanMakkah.mp3"
            )
        )
        list.add(
            SyncItem(
                filename = "azan_madina.mp3",
                url = "https://www.islamcan.com/audio/adhan/azan2.mp3",
                fallbackUrl = "https://ia800300.us.archive.org/31/items/AdhanMadinah/AdhanMadinah.mp3"
            )
        )
        list.add(
            SyncItem(
                filename = "azan_fajr.mp3",
                url = "https://www.islamcan.com/audio/adhan/azan3.mp3",
                fallbackUrl = "https://ia800208.us.archive.org/16/items/AdhanFajr/AdhanFajr.mp3"
            )
        )

        // 2. High-Frequency Core Surahs (Full recitation: Al-Fatihah, Al-Ikhlas, Al-Falaq, An-Nas, Al-Kawthar, Al-Asr, Al-Mulk)
        val coreSurahs = listOf(1, 112, 113, 114, 108, 103, 110, 111, 109, 67, 36, 55)
        for (s in coreSurahs) {
            val sPad = s.toString().padStart(3, '0')
            list.add(
                SyncItem(
                    filename = "surah_${s}_mishary.mp3",
                    url = "https://download.quranicaudio.com/quran/mishaari_raashid_al_3afaasee/$sPad.mp3",
                    fallbackUrl = "https://cdn.islamic.network/quran/audio-surah/128/ar.alafasy/$s.mp3"
                )
            )
        }

        // 3. Learning Academy Ayahs (Surah Al-Fatihah 1..7 and Al-Ikhlas 1..4)
        for (a in 1..7) {
            val aPad = a.toString().padStart(3, '0')
            list.add(
                SyncItem(
                    filename = "ayah_1_${a}_mishary.mp3",
                    url = "https://everyayah.com/data/Alafasy_128kbps/001$aPad.mp3",
                    fallbackUrl = null
                )
            )
        }
        for (a in 1..4) {
            val aPad = a.toString().padStart(3, '0')
            list.add(
                SyncItem(
                    filename = "ayah_112_${a}_mishary.mp3",
                    url = "https://everyayah.com/data/Alafasy_128kbps/112$aPad.mp3",
                    fallbackUrl = null
                )
            )
        }

        return list
    }
}
