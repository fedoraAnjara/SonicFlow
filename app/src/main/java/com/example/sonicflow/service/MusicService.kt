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
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MusicService : MediaSessionService(){
    private var mediaSession: MediaSession? = null

    private lateinit var player: ExoPlayer

    override  fun  onCreate(){
        super.onCreate()
        initializePlayer()
        initializeMediaSession()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
    }
    private  fun initializePlayer(){
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()

        player = ExoPlayer.Builder(this)
            .setAudioAttributes(audioAttributes,true)
            .setHandleAudioBecomingNoisy(true)
            .build()
    }

    private  val CHANNEL_ID = "music_channel"
    private  val NOTIFICATION_ID = 1

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

    private fun initializeMediaSession(){
        val sessionActivityIntent = Intent(this, MainActivity::class.java)
        val sessionActivityPendingIntent= PendingIntent.getActivity(
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
        stopForeground(true)
        stopSelf()
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy(){
        player.release()
        mediaSession?.release()
        mediaSession = null
        super.onDestroy()
    }
}