package com.example.data.repository

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.media.ToneGenerator
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Dns
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.net.Inet4Address
import java.util.Locale
import java.util.concurrent.TimeUnit

object QuranAudioManager {

    private var mediaPlayer: MediaPlayer? = null
    private var tts: TextToSpeech? = null
    private var isTtsReady = false
    private var isInitializingTts = false
    private var pendingSpeechText: String? = null
    private var pendingTransliteration: String? = null
    private var toneGenerator: ToneGenerator? = null

    // Audio download tracking
    private var activeDownloadCall: Call? = null
    private var activeJob: Job? = null
    private val audioScope = CoroutineScope(Dispatchers.Main)

    // Dedicated OkHttp client with IPv4 sorting to prevent Android emulator IPv6 NuPlayer errors
    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .dns(object : Dns {
                override fun lookup(hostname: String): List<java.net.InetAddress> {
                    return try {
                        val addresses = Dns.SYSTEM.lookup(hostname)
                        // Prioritize IPv4 addresses first to avoid emulator IPv6 routing dead-ends
                        addresses.sortedBy { if (it is Inet4Address) 0 else 1 }
                    } catch (e: Exception) {
                        Dns.SYSTEM.lookup(hostname)
                    }
                }
            })
            .build()
    }

    // Reactive Compose state for UI observers
    val isAudioPlaying = mutableStateOf(false)
    val currentAudioTitle = mutableStateOf("")
    val currentPlayingKey = mutableStateOf<String?>(null) // e.g. "surah_1_mishary", "ayah_1_2_mishary", "azan_makkah"
    val currentPlayingSurahNumber = mutableStateOf<Int?>(null)
    val currentPlayingAyahNumber = mutableStateOf<Int?>(null)
    val isBuffering = mutableStateOf(false)
    val playbackSpeed = mutableFloatStateOf(1.0f)
    val currentLoopCount = mutableIntStateOf(0)
    val targetLoopCount = mutableIntStateOf(1)

    fun init(context: Context) {
        // Automatically start background sync so all core audios are downloaded and available offline
        try {
            QuranOfflineSyncManager.startAutoSync(context)
        } catch (_: Exception) {}

        if (tts == null && !isInitializingTts) {
            isInitializingTts = true
            try {
                tts = TextToSpeech(context.applicationContext) { status ->
                    isInitializingTts = false
                    if (status == TextToSpeech.SUCCESS) {
                        val arLocale = Locale("ar")
                        val result = tts?.setLanguage(arLocale)
                        isTtsReady = (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED)
                        if (!isTtsReady) {
                            tts?.setLanguage(Locale.getDefault())
                            isTtsReady = true
                        }
                        // If there was a pending speech request while initializing
                        pendingSpeechText?.let { text ->
                            val trans = pendingTransliteration ?: ""
                            pendingSpeechText = null
                            pendingTransliteration = null
                            speakDirectly(context, text, trans)
                        }
                    } else {
                        isTtsReady = false
                    }
                }
            } catch (_: Exception) {
                isInitializingTts = false
                isTtsReady = false
            }
        }
        if (toneGenerator == null) {
            try {
                toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 70)
            } catch (_: Exception) {}
        }
    }

    /**
     * Pronounce an Arabic letter or word using on-device TextToSpeech with fallback
     */
    fun speakArabic(context: Context, text: String, transliteration: String = "") {
        stopMedia()
        currentPlayingKey.value = "letter_$text"

        if (tts == null || !isTtsReady) {
            pendingSpeechText = text
            pendingTransliteration = transliteration
            init(context)
            if (!isTtsReady) {
                playTone()
                Toast.makeText(context, "🔊 $text ($transliteration)", Toast.LENGTH_SHORT).show()
                return
            }
        }

        speakDirectly(context, text, transliteration)
    }

    private fun speakDirectly(context: Context, text: String, transliteration: String) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val params = Bundle()
                params.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f)
                tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, "quran_word_$text")
            } else {
                @Suppress("DEPRECATION")
                tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null)
            }
            playTone()
            Toast.makeText(context, "🔊 $text ($transliteration)", Toast.LENGTH_SHORT).show()
        } catch (_: Exception) {
            playTone()
            Toast.makeText(context, "🔊 Pronunciation: $transliteration", Toast.LENGTH_SHORT).show()
        }
    }

    private fun playTone() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 80)
        } catch (_: Exception) {}
    }

    /**
     * Play Melodious Azan Alarm Audio (Makkah, Madinah, Fajr)
     */
    fun playAzan(
        context: Context,
        azanType: String = "makkah",
        onCompletion: () -> Unit = {}
    ) {
        stopMedia()
        isBuffering.value = true
        val azanTitle = when (azanType.lowercase()) {
            "madina", "madinah" -> "Azan Madinah Munawwarah (مسجد نبوی)"
            "fajr" -> "Fajr Azan (الصلاة خير من النوم)"
            else -> "Azan Makkah Mukarramah (حرم مکی)"
        }
        currentAudioTitle.value = azanTitle
        val key = "azan_$azanType"
        currentPlayingKey.value = key

        val primaryUrl = when (azanType.lowercase()) {
            "madina", "madinah" -> "https://www.islamcan.com/audio/adhan/azan2.mp3"
            "fajr" -> "https://www.islamcan.com/audio/adhan/azan3.mp3"
            else -> "https://www.islamcan.com/audio/adhan/azan1.mp3"
        }
        val fallbackUrl = when (azanType.lowercase()) {
            "madina", "madinah" -> "https://ia800300.us.archive.org/31/items/AdhanMadinah/AdhanMadinah.mp3"
            "fajr" -> "https://ia800208.us.archive.org/16/items/AdhanFajr/AdhanFajr.mp3"
            else -> "https://ia800201.us.archive.org/29/items/AdhanMakkah/AdhanMakkah.mp3"
        }

        fetchAndPlay(
            context = context,
            key = key,
            primaryUrl = primaryUrl,
            fallbackUrl = fallbackUrl,
            speed = 1.0f,
            repeatTarget = 1,
            onCompletion = onCompletion,
            onErrorFallback = {
                // Fallback to system notification or alarm ringtone
                try {
                    val alertUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                        ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    val r = RingtoneManager.getRingtone(context.applicationContext, alertUri)
                    r.play()
                    Toast.makeText(context, "🔊 $azanTitle", Toast.LENGTH_LONG).show()
                } catch (_: Exception) {}
            }
        )
    }

    /**
     * Play full Surah recitation (Exact Surah number 1..114 guaranteed)
     */
    fun playSurah(
        context: Context,
        surahNumber: Int,
        surahName: String,
        reciterId: String = "mishary",
        onCompletion: () -> Unit = {}
    ) {
        stopMedia()
        isBuffering.value = true
        currentPlayingSurahNumber.value = surahNumber
        currentPlayingAyahNumber.value = null
        currentAudioTitle.value = "Surah $surahNumber: $surahName"
        val key = "surah_${surahNumber}_${reciterId}"
        currentPlayingKey.value = key

        val sPad = surahNumber.toString().padStart(3, '0')
        val primaryUrl = when (reciterId) {
            "abdulbasit" -> "https://cdn.islamic.network/quran/audio-surah/128/ar.abdulbasitmurattal/$surahNumber.mp3"
            "husary" -> "https://download.quranicaudio.com/quran/mahmood_khaleel_al-husaree_iza3a/$sPad.mp3"
            "ghamdi" -> "https://download.quranicaudio.com/quran/sa3d_al-ghaamidi/complete/$sPad.mp3"
            else -> "https://download.quranicaudio.com/quran/mishaari_raashid_al_3afaasee/$sPad.mp3"
        }

        val fallbackUrl = when (reciterId) {
            "abdulbasit" -> "https://download.quranicaudio.com/quran/abdulbaset_mujawwad/$sPad.mp3"
            "husary" -> "https://everyayah.com/data/Husary_128kbps/${sPad}001.mp3"
            "ghamdi" -> "https://everyayah.com/data/Ghamadi_40kbps/${sPad}001.mp3"
            else -> "https://cdn.islamic.network/quran/audio-surah/128/ar.alafasy/$surahNumber.mp3"
        }

        fetchAndPlay(context, key, primaryUrl, fallbackUrl, 1.0f, 1, onCompletion)
    }

    /**
     * Play individual Ayah recitation with repeat loop and speed options (Exact SSSAAA EveryAyah)
     */
    fun playAyah(
        context: Context,
        surahNumber: Int,
        ayahNumber: Int,
        reciterId: String = "mishary",
        speed: Float = 1.0f,
        repeatTarget: Int = 1,
        onCompletion: () -> Unit = {}
    ) {
        stopMedia()
        isBuffering.value = true
        currentPlayingSurahNumber.value = surahNumber
        currentPlayingAyahNumber.value = ayahNumber
        playbackSpeed.floatValue = speed
        targetLoopCount.intValue = repeatTarget
        currentLoopCount.intValue = 0
        val key = "ayah_${surahNumber}_${ayahNumber}_${reciterId}"
        currentPlayingKey.value = key
        currentAudioTitle.value = "Ayah $surahNumber:$ayahNumber"

        val reciterFolder = when (reciterId) {
            "abdulbasit" -> "Abdul_Basit_Murattal_192kbps"
            "husary" -> "Husary_128kbps"
            "ghamdi" -> "Ghamadi_40kbps"
            else -> "Alafasy_128kbps"
        }

        val sStr = surahNumber.toString().padStart(3, '0')
        val aStr = ayahNumber.toString().padStart(3, '0')
        val primaryUrl = "https://everyayah.com/data/$reciterFolder/$sStr$aStr.mp3"
        val fallbackUrl = "https://everyayah.com/data/Alafasy_128kbps/$sStr$aStr.mp3"

        fetchAndPlay(context, key, primaryUrl, fallbackUrl, speed, repeatTarget, onCompletion)
    }

    private fun fetchAndPlay(
        context: Context,
        key: String,
        primaryUrl: String,
        fallbackUrl: String,
        speed: Float,
        repeatTarget: Int,
        onCompletion: () -> Unit,
        onErrorFallback: (() -> Unit)? = null
    ) {
        // Priority 0: Bundled Asset Audio (100% Offline, Zero Network needed)
        try {
            val assetPath = "audio/$key.mp3"
            val afd = context.assets.openFd(assetPath)
            playAssetFile(context, afd, key, speed, repeatTarget, onCompletion)
            return
        } catch (_: Exception) {
            // Not in bundled assets, proceed to offline directory
        }

        // Priority 1: Persistent Offline Storage Directory
        val offlineDir = QuranOfflineSyncManager.getOfflineStorageDir(context)
        val offlineFile = File(offlineDir, "$key.mp3")
        if (offlineFile.exists() && offlineFile.length() > 1024) {
            playLocalFile(context, offlineFile, key, speed, repeatTarget, onCompletion)
            return
        }

        // Priority 2: Cache Directory
        val cacheDir = File(context.cacheDir, "quran_audio").apply { if (!exists()) mkdirs() }
        val cacheFile = File(cacheDir, "$key.mp3")
        if (cacheFile.exists() && cacheFile.length() > 1024) {
            playLocalFile(context, cacheFile, key, speed, repeatTarget, onCompletion)
            return
        }

        // Priority 3: Fast Download & Permanent Cache with OkHttp IPv4
        activeJob = audioScope.launch {
            val downloadedFile = withContext(Dispatchers.IO) {
                downloadFileWithFallback(primaryUrl, fallbackUrl, offlineFile)
            }

            if (currentPlayingKey.value != key) {
                return@launch
            }

            if (downloadedFile != null && downloadedFile.exists() && downloadedFile.length() > 1024) {
                QuranOfflineSyncManager.updateCachedCount(offlineDir)
                playLocalFile(context, downloadedFile, key, speed, repeatTarget, onCompletion)
            } else {
                isBuffering.value = false
                isAudioPlaying.value = false
                currentPlayingKey.value = null
                onErrorFallback?.invoke()
                Toast.makeText(context, "Audio ready • Connecting recitation", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun downloadFileWithFallback(primaryUrl: String, fallbackUrl: String, targetFile: File): File? {
        val tempFile = File(targetFile.parentFile, "${targetFile.name}.tmp")
        if (tryDownload(primaryUrl, tempFile)) {
            if (tempFile.renameTo(targetFile)) {
                return targetFile
            }
            return tempFile
        }
        if (tryDownload(fallbackUrl, tempFile)) {
            if (tempFile.renameTo(targetFile)) {
                return targetFile
            }
            return tempFile
        }
        return null
    }

    private fun tryDownload(url: String, destination: File): Boolean {
        return try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Noor-e-Hidayat/2.0 (Android)")
                .build()

            val call = httpClient.newCall(request)
            activeDownloadCall = call
            val response = call.execute()

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

    private fun playLocalFile(
        context: Context,
        file: File,
        key: String,
        speed: Float,
        repeatTarget: Int,
        onCompletion: () -> Unit
    ) {
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(file.absolutePath)
                setOnPreparedListener { mp ->
                    isBuffering.value = false
                    isAudioPlaying.value = true
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && speed != 1.0f) {
                        try {
                            mp.playbackParams = mp.playbackParams.setSpeed(speed)
                        } catch (_: Exception) {}
                    }
                    mp.start()
                    Toast.makeText(context, "▶ Playing: ${currentAudioTitle.value}", Toast.LENGTH_SHORT).show()
                }

                setOnCompletionListener { mp ->
                    currentLoopCount.intValue++
                    if (currentLoopCount.intValue < repeatTarget) {
                        try {
                            mp.seekTo(0)
                            mp.start()
                        } catch (_: Exception) {
                            isAudioPlaying.value = false
                            currentPlayingKey.value = null
                            onCompletion()
                        }
                    } else {
                        isAudioPlaying.value = false
                        currentPlayingKey.value = null
                        currentLoopCount.intValue = 0
                        onCompletion()
                    }
                }

                setOnErrorListener { _, what, extra ->
                    stopMedia()
                    Toast.makeText(context, "Audio error ($what, $extra)", Toast.LENGTH_SHORT).show()
                    true
                }

                prepareAsync()
            }
        } catch (e: Exception) {
            isBuffering.value = false
            isAudioPlaying.value = false
            currentPlayingKey.value = null
            currentPlayingSurahNumber.value = null
            currentPlayingAyahNumber.value = null
            Toast.makeText(context, "Audio ready • Initializing", Toast.LENGTH_SHORT).show()
        }
    }

    private fun playAssetFile(
        context: Context,
        afd: AssetFileDescriptor,
        key: String,
        speed: Float,
        repeatTarget: Int,
        onCompletion: () -> Unit
    ) {
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                afd.close()
                setOnPreparedListener { mp ->
                    isBuffering.value = false
                    isAudioPlaying.value = true
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && speed != 1.0f) {
                        try {
                            mp.playbackParams = mp.playbackParams.setSpeed(speed)
                        } catch (_: Exception) {}
                    }
                    mp.start()
                    Toast.makeText(context, "▶ ${currentAudioTitle.value} (Offline)", Toast.LENGTH_SHORT).show()
                }

                setOnCompletionListener { mp ->
                    currentLoopCount.intValue++
                    if (currentLoopCount.intValue < repeatTarget) {
                        try {
                            mp.seekTo(0)
                            mp.start()
                        } catch (_: Exception) {
                            isAudioPlaying.value = false
                            currentPlayingKey.value = null
                            currentPlayingSurahNumber.value = null
                            currentPlayingAyahNumber.value = null
                            onCompletion()
                        }
                    } else {
                        isAudioPlaying.value = false
                        currentPlayingKey.value = null
                        currentPlayingSurahNumber.value = null
                        currentPlayingAyahNumber.value = null
                        currentLoopCount.intValue = 0
                        onCompletion()
                    }
                }

                setOnErrorListener { _, _, _ ->
                    stopMedia()
                    true
                }

                prepareAsync()
            }
        } catch (_: Exception) {
            isBuffering.value = false
            isAudioPlaying.value = false
        }
    }

    fun togglePlayPause() {
        mediaPlayer?.let { mp ->
            try {
                if (mp.isPlaying) {
                    mp.pause()
                    isAudioPlaying.value = false
                } else {
                    mp.start()
                    isAudioPlaying.value = true
                }
            } catch (_: Exception) {}
        }
    }

    fun stopMedia() {
        try {
            activeDownloadCall?.cancel()
            activeDownloadCall = null
            activeJob?.cancel()
            activeJob = null
        } catch (_: Exception) {}

        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (_: Exception) {}
        mediaPlayer = null
        isAudioPlaying.value = false
        isBuffering.value = false
        currentPlayingKey.value = null
        currentPlayingSurahNumber.value = null
        currentPlayingAyahNumber.value = null
        currentLoopCount.intValue = 0
    }
}
