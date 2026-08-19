package com.example.service

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SpeechService(private val context: Context) {

    companion object {
        private const val TAG = "SpeechService"
    }

    private var speechRecognizer: SpeechRecognizer? = null
    private var isListeningActive = false

    private val _listeningState = MutableStateFlow<SpeechState>(SpeechState.Idle)
    val listeningState: StateFlow<SpeechState> = _listeningState.asStateFlow()

    private val _rmsVolume = MutableStateFlow(0f)
    val rmsVolume: StateFlow<Float> = _rmsVolume.asStateFlow()

    sealed interface SpeechState {
        object Idle : SpeechState
        object Ready : SpeechState
        data class Listening(val partialText: String = "") : SpeechState
        data class Success(val recognizedText: String) : SpeechState
        data class Error(val errorMessage: String, val errorCode: Int) : SpeechState
    }

    fun isRecognitionAvailable(): Boolean {
        return SpeechRecognizer.isRecognitionAvailable(context)
    }

    fun startListening(languageCode: String = "hi-IN", onResult: (String) -> Unit, onError: (String) -> Unit) {
        stopListening()

        if (!isRecognitionAvailable()) {
            val err = if (languageCode.startsWith("hi")) {
                "Aapke device par Speech Recognition uplabdh nahi hai."
            } else {
                "Speech recognition service is not available on this device."
            }
            _listeningState.value = SpeechState.Error(err, -1)
            onError(err)
            return
        }

        try {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        Log.d(TAG, "onReadyForSpeech")
                        isListeningActive = true
                        _listeningState.value = SpeechState.Ready
                    }

                    override fun onBeginningOfSpeech() {
                        Log.d(TAG, "onBeginningOfSpeech")
                        _listeningState.value = SpeechState.Listening()
                    }

                    override fun onRmsChanged(rmsdB: Float) {
                        val normalized = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
                        _rmsVolume.value = normalized
                    }

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        Log.d(TAG, "onEndOfSpeech")
                        isListeningActive = false
                    }

                    override fun onError(error: Int) {
                        isListeningActive = false
                        val errorMsg = mapSpeechError(error, languageCode)
                        Log.w(TAG, "SpeechRecognizer error: $error ($errorMsg)")
                        _listeningState.value = SpeechState.Error(errorMsg, error)
                        onError(errorMsg)
                    }

                    override fun onResults(results: Bundle?) {
                        isListeningActive = false
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val spokenText = matches?.firstOrNull()?.trim() ?: ""
                        if (spokenText.isNotEmpty()) {
                            Log.d(TAG, "Recognized: $spokenText")
                            _listeningState.value = SpeechState.Success(spokenText)
                            onResult(spokenText)
                        } else {
                            val noInputMsg = if (languageCode.startsWith("hi")) {
                                "Kripya dobara bolein, aawaz spasht nahi thi."
                            } else {
                                "No speech detected. Please speak clearly."
                            }
                            _listeningState.value = SpeechState.Error(noInputMsg, SpeechRecognizer.ERROR_NO_MATCH)
                            onError(noInputMsg)
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val partial = matches?.firstOrNull() ?: ""
                        if (partial.isNotEmpty()) {
                            _listeningState.value = SpeechState.Listening(partial)
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageCode)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, languageCode)
                putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, languageCode)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            }

            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start speech recognition", e)
            val err = "Microphone error: ${e.localizedMessage}"
            _listeningState.value = SpeechState.Error(err, -1)
            onError(err)
        }
    }

    fun stopListening() {
        if (isListeningActive || speechRecognizer != null) {
            try {
                speechRecognizer?.stopListening()
                speechRecognizer?.cancel()
                speechRecognizer?.destroy()
            } catch (e: Exception) {
                Log.e(TAG, "Error stopping recognizer", e)
            }
            speechRecognizer = null
            isListeningActive = false
            _listeningState.value = SpeechState.Idle
            _rmsVolume.value = 0f
        }
    }

    private fun mapSpeechError(error: Int, languageCode: String): String {
        val isHindi = languageCode.startsWith("hi")
        return when (error) {
            SpeechRecognizer.ERROR_AUDIO -> if (isHindi) "Audio recording error hua." else "Audio recording error."
            SpeechRecognizer.ERROR_CLIENT -> if (isHindi) "App client error. Dobara koshish karein." else "Client side error. Please retry."
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> if (isHindi) "Microphone permission ki aavashyakta hai." else "Microphone permission is required."
            SpeechRecognizer.ERROR_NETWORK -> if (isHindi) "Network connection nahi mil raha hai." else "Network connection error."
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> if (isHindi) "Network timeout hua." else "Network timeout."
            SpeechRecognizer.ERROR_NO_MATCH -> if (isHindi) "Aawaz spasht nahi thi. Kripya dobara bolein." else "No match found. Please speak clearly."
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> if (isHindi) "Voice engine busy hai." else "Speech recognizer is busy."
            SpeechRecognizer.ERROR_SERVER -> if (isHindi) "Voice recognition server error." else "Server error in speech recognition."
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> if (isHindi) "Koi aawaz nahi suni gayi." else "Speech input timed out."
            else -> if (isHindi) "Voice recognition mein samasya aayi ($error)." else "Speech recognition error ($error)."
        }
    }
}
