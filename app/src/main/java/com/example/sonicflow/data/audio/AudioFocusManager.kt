package com.example.sonicflow.data.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioFocusManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var audioFocusRequest: AudioFocusRequest? = null
    private var hasAudioFocus = false

    private var onAudioFocusLost: (() -> Unit)? = null
    private var onAudioFocusGained: (() -> Unit)? = null
    private var onAudioFocusLossTransient: (() -> Unit)? = null
    private var onAudioFocusLossTransientCanDuck: (() -> Unit)? = null

    private val audioFocusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                // Récupération du focus audio - reprendre la lecture normale
                hasAudioFocus = true
                onAudioFocusGained?.invoke()
            }
            AudioManager.AUDIOFOCUS_LOSS -> {
                // Perte permanente du focus - arrêter la lecture
                hasAudioFocus = false
                onAudioFocusLost?.invoke()
                // Ne pas abandonner automatiquement - sera fait lors du cleanup
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                // Perte temporaire du focus (appel téléphonique) - pause
                onAudioFocusLossTransient?.invoke()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                // Perte temporaire mais peut continuer en sourdine (notification)
                onAudioFocusLossTransientCanDuck?.invoke()
            }
        }
    }

    /**
     * Demande le focus audio
     */
    fun requestAudioFocus(): Boolean {
        if (hasAudioFocus) return true

        val result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Android 8.0+
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()

            audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(audioAttributes)
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener(audioFocusChangeListener)
                .build()

            audioManager.requestAudioFocus(audioFocusRequest!!)
        } else {
            // Android 7.1 et inférieur
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(
                audioFocusChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )
        }

        hasAudioFocus = result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        return hasAudioFocus
    }

    /**
     * Abandonne le focus audio
     */
    fun abandonAudioFocus() {
        if (!hasAudioFocus) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let {
                audioManager.abandonAudioFocusRequest(it)
            }
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(audioFocusChangeListener)
        }

        hasAudioFocus = false
    }

    /**
     * Configure les callbacks pour les changements de focus
     */
    fun setCallbacks(
        onFocusLost: () -> Unit,
        onFocusGained: () -> Unit,
        onFocusLossTransient: () -> Unit,
        onFocusLossTransientCanDuck: () -> Unit
    ) {
        this.onAudioFocusLost = onFocusLost
        this.onAudioFocusGained = onFocusGained
        this.onAudioFocusLossTransient = onFocusLossTransient
        this.onAudioFocusLossTransientCanDuck = onFocusLossTransientCanDuck
    }

    fun hasAudioFocus(): Boolean = hasAudioFocus
}