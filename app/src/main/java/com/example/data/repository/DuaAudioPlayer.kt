package com.example.data.repository

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.widget.Toast
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

object DuaAudioPlayer {

    private var tts: TextToSpeech? = null
    private var isTtsReady = false
    private var isInitializing = false
    private var toneGenerator: ToneGenerator? = null

    private val _currentDuaId = MutableStateFlow<String?>(null)
    val currentDuaId: StateFlow<String?> = _currentDuaId.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _repeatCount = MutableStateFlow(1)
    val repeatCount: StateFlow<Int> = _repeatCount.asStateFlow()

    private val _currentLoop = MutableStateFlow(1)
    val currentLoop: StateFlow<Int> = _currentLoop.asStateFlow()

    private var activeDuaText: String = ""
    private var activeDuaTranslit: String = ""
    private var activeContext: Context? = null

    fun init(context: Context) {
        activeContext = context.applicationContext
        if (tts == null && !isInitializing) {
            isInitializing = true
            try {
                tts = TextToSpeech(context.applicationContext) { status ->
                    isInitializing = false
                    if (status == TextToSpeech.SUCCESS) {
                        val arLocale = Locale("ar")
                        val result = tts?.setLanguage(arLocale)
                        isTtsReady = (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED)
                        if (!isTtsReady) {
                            tts?.setLanguage(Locale.getDefault())
                            isTtsReady = true
                        }
                        try {
                            tts?.setSpeechRate(0.85f)
                            tts?.setPitch(1.0f)
                        } catch (_: Exception) {}

                        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                            override fun onStart(utteranceId: String?) {
                                _isPlaying.value = true
                            }

                            override fun onDone(utteranceId: String?) {
                                handleUtteranceDone()
                            }

                            @Deprecated("Deprecated in Java")
                            override fun onError(utteranceId: String?) {
                                _isPlaying.value = false
                                _currentDuaId.value = null
                            }

                            override fun onError(utteranceId: String?, errorCode: Int) {
                                _isPlaying.value = false
                                _currentDuaId.value = null
                            }
                        })
                    } else {
                        isTtsReady = false
                    }
                }
            } catch (_: Exception) {
                isInitializing = false
                isTtsReady = false
            }
        }

        if (toneGenerator == null) {
            try {
                toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 70)
            } catch (_: Exception) {}
        }
    }

    private fun handleUtteranceDone() {
        if (_currentLoop.value < _repeatCount.value) {
            _currentLoop.value = _currentLoop.value + 1
            speakCurrentSegment()
        } else {
            _isPlaying.value = false
            _currentDuaId.value = null
            _currentLoop.value = 1
        }
    }

    fun playDua(context: Context, duaId: String, arabicText: String, transliteration: String, repeatTarget: Int = 1) {
        init(context)
        activeContext = context.applicationContext

        // If clicking the same dua that is already playing, pause it
        if (_currentDuaId.value == duaId && _isPlaying.value) {
            pause()
            return
        }

        stop()

        _currentDuaId.value = duaId
        _repeatCount.value = repeatTarget
        _currentLoop.value = 1
        activeDuaText = arabicText
        activeDuaTranslit = transliteration

        speakCurrentSegment()
    }

    private fun speakCurrentSegment() {
        val utteranceId = "dua_${_currentDuaId.value}_loop_${_currentLoop.value}"
        _isPlaying.value = true

        if (tts != null && isTtsReady) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    val params = Bundle()
                    params.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f)
                    tts?.speak(activeDuaText, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
                } else {
                    @Suppress("DEPRECATION")
                    tts?.speak(activeDuaText, TextToSpeech.QUEUE_FLUSH, null)
                }
                return
            } catch (_: Exception) {}
        }

        // A beep is not a recitation. Do not present it as audio when Arabic TTS is unavailable.
        activeContext?.let {
            Toast.makeText(it, "Arabic pronunciation is unavailable on this device. No Qari recording is bundled for this dua.", Toast.LENGTH_LONG).show()
        }
        _isPlaying.value = false
        _currentDuaId.value = null
    }

    fun pause() {
        try {
            tts?.stop()
        } catch (_: Exception) {}
        _isPlaying.value = false
    }

    fun resume() {
        if (_currentDuaId.value != null && activeDuaText.isNotBlank()) {
            speakCurrentSegment()
        }
    }

    fun stop() {
        try {
            tts?.stop()
        } catch (_: Exception) {}
        _isPlaying.value = false
        _currentDuaId.value = null
        _currentLoop.value = 1
    }

    fun setRepeatCount(count: Int) {
        if (count > 0) {
            _repeatCount.value = count
        }
    }
}
