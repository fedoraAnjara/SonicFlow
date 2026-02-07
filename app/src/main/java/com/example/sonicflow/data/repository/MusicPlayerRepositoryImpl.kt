package com.example.sonicflow.data.repository

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.sonicflow.domain.model.AudioTrack
import com.example.sonicflow.domain.repository.MusicPlayerRepository
import com.example.sonicflow.service.MusicService
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton
import androidx.media3.common.util.UnstableApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.*

@UnstableApi
@Singleton
class MusicPlayerRepositoryImpl @Inject constructor(
    private val context: Context
) : MusicPlayerRepository{
    private var mediaController: MediaController? = null
    private var controllerFuture: ListenableFuture<MediaController>? = null

    private val _currentTrack = MutableStateFlow<AudioTrack?>(null)
    override val currentTrack: StateFlow<AudioTrack?> = _currentTrack.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    override val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    override val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    override val duration: StateFlow<Long> = _duration.asStateFlow()

    private var positionJob: Job? = null

    private var playlist = listOf<AudioTrack>()
    private var currentIndex = 0

    init {
        initializeController()
    }

    private fun startPositionUpdates() {
        positionJob?.cancel()
        positionJob = CoroutineScope(Dispatchers.Main).launch {
            while (isActive && mediaController?.isPlaying == true) {
                _currentPosition.value = mediaController?.currentPosition ?: 0L
                _duration.value = mediaController?.duration ?: 0L
                delay(300)
            }
        }
    }


    private fun initializeController() {
        val sessionToken = SessionToken(
            context,
            ComponentName(context, MusicService::class.java)
        )

        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()

        controllerFuture?.addListener(
            {
                mediaController = controllerFuture?.get()
                setupPlayerListener()
            },
            MoreExecutors.directExecutor()
        )
    }

    private fun setupPlayerListener() {
        mediaController?.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying

                if (isPlaying) {
                    startPositionUpdates()
                } else {
                    positionJob?.cancel()
                }
            }


            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    _duration.value = mediaController?.duration ?: 0L
                }
            }
        })
    }

    override fun playTrack(track: AudioTrack) {
        _currentTrack.value = track

        val mediaItem = MediaItem.Builder()
            .setUri(track.data)
            .setMediaId(track.id.toString())
            .build()

        mediaController?.apply {
            setMediaItem(mediaItem)
            prepare()
            play()
        }
    }

    override fun setPlaylist(tracks: List<AudioTrack>, startIndex: Int) {
        playlist = tracks
        currentIndex = startIndex
        if (tracks.isNotEmpty() && startIndex in tracks.indices) {
            playTrack(tracks[startIndex])
        }
    }

    override fun pauseTrack() {
        mediaController?.pause()
        _isPlaying.value = false
        positionJob?.cancel()
    }


    override fun resume() {
        mediaController?.play()
        _isPlaying.value = true
        startPositionUpdates()
    }

    override fun seekTo(position: Long) {
        mediaController?.seekTo(position)
    }

    override fun playNext() {
        if (currentIndex < playlist.size - 1) {
            currentIndex++
            playTrack(playlist[currentIndex])
        }
    }

    override fun playPrevious() {
        if (currentIndex > 0) {
            currentIndex--
            playTrack(playlist[currentIndex])
        }
    }

    fun release() {
        MediaController.releaseFuture(controllerFuture ?: return)
        mediaController = null
    }
    }