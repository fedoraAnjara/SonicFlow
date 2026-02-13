package com.example.sonicflow.data.repository

import android.content.ComponentName
import android.content.Context
import android.media.MediaPlayer
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.sonicflow.domain.model.AudioTrack
import com.example.sonicflow.domain.repository.MusicPlayerRepository
import com.example.sonicflow.presentation.player.RepeatMode
import com.example.sonicflow.presentation.player.ShuffleMode
import com.example.sonicflow.service.MusicService
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton
import androidx.media3.common.util.UnstableApi
import com.example.sonicflow.data.audio.AudioFocusManager
import com.example.sonicflow.data.audio.WaveformExtractor
import com.example.sonicflow.data.preferences.PlaybackPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow


@UnstableApi
@Singleton
class MusicPlayerRepositoryImpl @Inject constructor(
    private val mediaPlayer: MediaPlayer,
    private val context: Context,
    private val waveformExtractor: WaveformExtractor,
    private val audioFocusManager: AudioFocusManager,
    private val playbackPreferences: PlaybackPreferences

) : MusicPlayerRepository {

    private val _volume = MutableStateFlow(0.7f)
    override val volume: Flow<Float> = _volume.asStateFlow()

    private val _waveformData = MutableStateFlow<List<Float>>(emptyList())
    override val waveformData: StateFlow<List<Float>> = _waveformData.asStateFlow()

    private val _queue = MutableStateFlow<List<AudioTrack>>(emptyList())
    override val queue: StateFlow<List<AudioTrack>> = _queue.asStateFlow()

    // Modes de lecture
    private val _repeatMode = MutableStateFlow(RepeatMode.OFF)
    override val repeatMode: StateFlow<RepeatMode> = _repeatMode.asStateFlow()

    private val _shuffleMode = MutableStateFlow(ShuffleMode.OFF)
    override val shuffleMode: StateFlow<ShuffleMode> = _shuffleMode.asStateFlow()

    // Événement de fin de piste
    private val _trackEnded = MutableStateFlow(false)
    override val trackEnded: StateFlow<Boolean> = _trackEnded.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

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
    private var wasPlayingBeforeFocusLoss = false

    private fun startAutoSave() {
        scope.launch {
            while (true) {
                kotlinx.coroutines.delay(5000) // Sauvegarder toutes les 5 secondes
                val track = _currentTrack.value
                if (track != null) {
                    playbackPreferences.savePlaybackState(
                        trackId = track.id,
                        position = _currentPosition.value,
                        isPlaying = _isPlaying.value
                    )
                }
            }
        }
    }

    init {
        initializeController()
        startAutoSave()
        setupAudioFocus()
    }

    override suspend fun restorePlaybackState(allTracks: List<AudioTrack>) {
        playbackPreferences.lastTrackId.collect { trackId ->
            if (trackId != null) {
                val track = allTracks.find { it.id == trackId }
                if (track != null) {
                    playTrack(track)

                    // Restaurer la position
                    playbackPreferences.lastPosition.collect { position ->
                        seekTo(position)

                        // Restaurer l'état play/pause
                        playbackPreferences.wasPlaying.collect { wasPlaying ->
                            if (!wasPlaying) {
                                pauseTrack()
                            }
                        }
                        return@collect // Sort après la première valeur
                    }
                }
            }
        }
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
                if (playbackState == Player.STATE_ENDED) {
                    // Notifier que la piste est terminée
                    _trackEnded.value = true

                    // Gérer automatiquement selon le mode
                    handleTrackEnd()
                }
            }
        })
    }

    private fun handleTrackEnd() {
        when (_repeatMode.value) {
            RepeatMode.ONE -> {
                // Répéter la piste actuelle
                seekTo(0)
                resume()
            }
            RepeatMode.ALL -> {
                // Passer à la suivante (ou recommencer)
                playNext()
            }
            RepeatMode.OFF -> {
                // Passer à la suivante si disponible, sinon arrêter
                val queueList = _queue.value
                val currentTrack = _currentTrack.value
                val currentIdx = queueList.indexOfFirst { it.id == currentTrack?.id }

                if (currentIdx != -1 && currentIdx < queueList.size - 1) {
                    playNext()
                } else {
                    // Fin de la playlist
                    pauseTrack()
                }
            }
        }

        // Réinitialiser le flag
        _trackEnded.value = false
    }

    override fun setRepeatMode(mode: RepeatMode) {
        _repeatMode.value = mode
    }

    override fun setShuffleMode(mode: ShuffleMode) {
        _shuffleMode.value = mode
    }

    override fun setVolume(volume: Float) {
        val clampedVolume = volume.coerceIn(0f, 1f)
        _volume.value = clampedVolume
        mediaController?.volume = clampedVolume
    }

    private fun setupAudioFocus() {
        audioFocusManager.setCallbacks(
            onFocusLost = {
                // Perte permanente du focus - arrêter la lecture
                pauseTrack()
                wasPlayingBeforeFocusLoss = false
            },
            onFocusGained = {
                // Récupération du focus - reprendre si on jouait avant
                if (wasPlayingBeforeFocusLoss) {
                    resume()
                    wasPlayingBeforeFocusLoss = false
                }
            },
            onFocusLossTransient = {
                // Interruption temporaire (appel) - mettre en pause
                wasPlayingBeforeFocusLoss = _isPlaying.value
                pauseTrack()
            },
            onFocusLossTransientCanDuck = {
                // Notification - baisser le volume
                val currentVolume = _volume.value
                mediaController?.volume = currentVolume * 0.3f

                // Restaurer le volume après quelques secondes
                scope.launch {
                    delay(3000)
                    mediaController?.volume = currentVolume
                }
            }
        )
    }

    override fun playTrack(track: AudioTrack) {
        if (!audioFocusManager.requestAudioFocus()) {
            // Si on n'obtient pas le focus, ne pas jouer
            return
        }
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

        // Générer la waveform en arrière-plan
        scope.launch {
            generateWaveform(track.data)
        }
    }

    override fun setPlaylist(tracks: List<AudioTrack>, startIndex: Int) {
        playlist = tracks
        currentIndex = startIndex

        _queue.value = tracks

        if (tracks.isNotEmpty() && startIndex in tracks.indices) {
            playTrack(tracks[startIndex])
        }
    }

    override fun setQueue(tracks: List<AudioTrack>) {
        _queue.value = tracks
        playlist = tracks
        // Ne pas réinitialiser currentIndex pour maintenir la position
        if (currentIndex >= tracks.size) {
            currentIndex = 0
        }
    }

    override fun addToQueue(tracks: List<AudioTrack>) {
        _queue.value = _queue.value + tracks
        playlist = _queue.value
    }

    override fun removeFromQueue(track: AudioTrack) {
        _queue.value = _queue.value.filter { it.id != track.id }
        playlist = _queue.value

        // Ajuster l'index si nécessaire
        if (currentIndex >= playlist.size && playlist.isNotEmpty()) {
            currentIndex = playlist.size - 1
        }
    }

    override fun clearQueue() {
        _queue.value = emptyList()
        playlist = emptyList()
        currentIndex = 0
        pauseTrack()
    }

    override fun pauseTrack() {
        mediaController?.pause()
        _isPlaying.value = false
        positionJob?.cancel()
    }

    override suspend fun generateWaveform(audioPath: String) {
        withContext(Dispatchers.IO) {
            val waveform = waveformExtractor.extractWaveform(audioPath, targetSamples = 100)
            _waveformData.value = waveform
        }
    }

    override fun resume() {
        if (!audioFocusManager.requestAudioFocus()) {
            return
        }
        mediaController?.play()
        _isPlaying.value = true
        startPositionUpdates()
    }

    override fun seekTo(position: Long) {
        mediaController?.seekTo(position)
    }

    override fun playNext() {
        val queueList = _queue.value
        val currentTrack = _currentTrack.value

        if (queueList.isEmpty()) return

        val currentIdx = queueList.indexOfFirst { it.id == currentTrack?.id }

        when (_repeatMode.value) {
            RepeatMode.ONE -> {
                // En mode repeat one, rejouer la même piste
                currentTrack?.let { playTrack(it) }
            }
            RepeatMode.ALL -> {
                // En mode repeat all, boucler sur la playlist
                if (currentIdx == queueList.size - 1) {
                    currentIndex = 0
                    playTrack(queueList[currentIndex])
                } else if (currentIdx != -1 && currentIdx < queueList.size - 1) {
                    currentIndex = currentIdx + 1
                    playTrack(queueList[currentIndex])
                }
            }
            RepeatMode.OFF -> {
                // En mode normal, s'arrêter à la fin
                if (currentIdx != -1 && currentIdx < queueList.size - 1) {
                    currentIndex = currentIdx + 1
                    playTrack(queueList[currentIndex])
                } else {
                    // Fin de la playlist
                    pauseTrack()
                }
            }
        }
    }

    override fun playPrevious() {
        val queueList = _queue.value
        val currentTrack = _currentTrack.value

        if (queueList.isEmpty()) return

        // Si on est à plus de 3 secondes, revenir au début
        if (_currentPosition.value > 3000) {
            seekTo(0)
            return
        }

        val currentIdx = queueList.indexOfFirst { it.id == currentTrack?.id }

        if (currentIdx > 0) {
            currentIndex = currentIdx - 1
            playTrack(queueList[currentIndex])
        } else if (_repeatMode.value == RepeatMode.ALL) {
            // En mode repeat all, aller à la fin
            currentIndex = queueList.size - 1
            playTrack(queueList[currentIndex])
        } else {
            // Sinon, juste revenir au début de la piste actuelle
            seekTo(0)
        }
    }

    fun release() {
        positionJob?.cancel()
        scope.cancel()
        audioFocusManager.abandonAudioFocus()
        MediaController.releaseFuture(controllerFuture ?: return)
        mediaController = null
    }
}