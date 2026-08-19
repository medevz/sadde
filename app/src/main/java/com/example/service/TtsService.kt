package com.example.service

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class TtsService(private val context: Context) {

    companion object {
        private const val TAG = "TtsService"
    }

    private var textToSpeech: TextToSpeech? = null
    private var isInitialized = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private var pendingSpeechText: String? = null
    private var pendingLanguage: String = "hi"
    private var pendingSpeechRate: Float = 1.0f
    private var pendingSpeechPitch: Float = 1.0f

    init {
        initializeTts()
    }

    private fun initializeTts() {
        textToSpeech = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isInitialized = true
                Log.d(TAG, "TTS Initialized successfully")
                setupListener()
                pendingSpeechText?.let { text ->
                    speak(text, pendingLanguage, pendingSpeechRate, pendingSpeechPitch)
                    pendingSpeechText = null
                }
            } else {
                Log.e(TAG, "TTS Initialization failed with status: $status")
            }
        }
    }

    private fun setupListener() {
        textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                _isSpeaking.value = true
            }

            override fun onDone(utteranceId: String?) {
                _isSpeaking.value = false
            }

            override fun onError(utteranceId: String?) {
                _isSpeaking.value = false
                Log.e(TAG, "TTS playback error for utterance: $utteranceId")
            }
        })
    }

    fun setLanguage(languageCode: String) {
        pendingLanguage = languageCode
        if (!isInitialized || textToSpeech == null) return
        val locale = when {
            languageCode.startsWith("hi") -> Locale("hi", "IN")
            languageCode.startsWith("en") -> Locale("en", "IN")
            else -> Locale.getDefault()
        }
        val langResult = textToSpeech?.setLanguage(locale)
        if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
            textToSpeech?.setLanguage(Locale.US)
        }
    }

    fun setSpeechRate(rate: Float) {
        pendingSpeechRate = rate.coerceIn(0.5f, 2.0f)
        textToSpeech?.setSpeechRate(pendingSpeechRate)
    }

    fun setPitch(pitch: Float) {
        pendingSpeechPitch = pitch.coerceIn(0.5f, 2.0f)
        textToSpeech?.setPitch(pendingSpeechPitch)
    }

    fun speak(
        text: String,
        languageCode: String = pendingLanguage,
        speechRate: Float = pendingSpeechRate,
        speechPitch: Float = pendingSpeechPitch,
        onComplete: (() -> Unit)? = null
    ) {
        if (!isInitialized || textToSpeech == null) {
            pendingSpeechText = text
            pendingLanguage = languageCode
            pendingSpeechRate = speechRate
            pendingSpeechPitch = speechPitch
            return
        }

        try {
            val locale = when {
                languageCode.startsWith("hi") -> Locale("hi", "IN")
                languageCode.startsWith("en") -> Locale("en", "IN")
                else -> Locale.getDefault()
            }

            val langResult = textToSpeech?.setLanguage(locale)
            if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Fallback to English US or default
                textToSpeech?.setLanguage(Locale.US)
            }

            textToSpeech?.setSpeechRate(speechRate.coerceIn(0.5f, 2.0f))
            textToSpeech?.setPitch(speechPitch.coerceIn(0.5f, 2.0f))

            val utteranceId = "shreya_utt_${System.currentTimeMillis()}"
            val params = Bundle()
            textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
        } catch (e: Exception) {
            Log.e(TAG, "Error executing TTS speak", e)
            _isSpeaking.value = false
        }
    }

    fun stop() {
        try {
            textToSpeech?.stop()
            _isSpeaking.value = false
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping TTS", e)
        }
    }

    fun shutdown() {
        try {
            stop()
            textToSpeech?.shutdown()
            textToSpeech = null
            isInitialized = false
        } catch (e: Exception) {
            Log.e(TAG, "Error shutting down TTS", e)
        }
    }
}
