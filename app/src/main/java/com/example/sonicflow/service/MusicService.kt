package com.example.sonicflow.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.example.sonicflow.MainActivity
import com.example.sonicflow.R
import com.example.sonicflow.data.audio.AudioFocusManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MusicService : MediaSessionService() {
    private var mediaSession: MediaSession? = null
    private lateinit var player: ExoPlayer

    @Inject
    lateinit var audioFocusManager: AudioFocusManager

    private val CHANNEL_ID = "music_channel"
    private val NOTIFICATION_ID = 1

    override fun onCreate() {
        super.onCreate()
        initializePlayer()
        setupAudioFocusCallbacks()
        initializeMediaSession()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
    }

    private fun initializePlayer() {
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()

        player = ExoPlayer.Builder(this)
            .setAudioAttributes(audioAttributes, false) // false car on gère manuellement
            .setHandleAudioBecomingNoisy(true)
            .build()

        // Écouter les changements d'état du player
        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_READY -> {
                        // Le player est prêt
                    }
                    Player.STATE_ENDED -> {
                        // Lecture terminée
                        audioFocusManager.abandonAudioFocus()
                    }
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (isPlaying) {
                    // Demander le focus audio quand on commence à jouer
                    if (!audioFocusManager.requestAudioFocus()) {
                        // Si on n'obtient pas le focus, mettre en pause
                        player.pause()
                    }
                }
            }
        })
    }

    private fun setupAudioFocusCallbacks() {
        audioFocusManager.setCallbacks(
            onFocusLost = {
                // Perte permanente du focus - arrêter la lecture
                player.pause()
            },
            onFocusGained = {
                // Récupération du focus - reprendre la lecture si c'était en cours
                if (!player.isPlaying && player.playbackState == Player.STATE_READY) {
                    player.play()
                }
            },
            onFocusLossTransient = {
                // Perte temporaire (appel) - mettre en pause
                if (player.isPlaying) {
                    player.pause()
                }
            },
            onFocusLossTransientCanDuck = {
                // Notification - réduire le volume
                player.volume = 0.3f
            }
        )
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Music Player",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notification for music player"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("SonicFlow")
            .setContentText("Musique en lecture")
            .setSmallIcon(R.drawable.msc)
            .setOngoing(true)
            .build()
    }

    private fun initializeMediaSession() {
        val sessionActivityIntent = Intent(this, MainActivity::class.java)
        val sessionActivityPendingIntent = PendingIntent.getActivity(
            this,
            0,
            sessionActivityIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        mediaSession = MediaSession.Builder(this, player)
            .setSessionActivity(sessionActivityPendingIntent)
            .build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        player.stop()
        audioFocusManager.abandonAudioFocus()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        audioFocusManager.abandonAudioFocus()
        player.release()
        mediaSession?.release()
        mediaSession = null
        super.onDestroy()
    }
}