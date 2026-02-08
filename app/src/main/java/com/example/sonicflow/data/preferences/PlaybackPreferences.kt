package com.example.sonicflow.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "playback_prefs")

@Singleton
class PlaybackPreferences @Inject constructor(
    private val context: Context
) {
    private object Keys {
        val LAST_TRACK_ID = longPreferencesKey("last_track_id")
        val LAST_POSITION = longPreferencesKey("last_position")
        val WAS_PLAYING = booleanPreferencesKey("was_playing")
    }

    // Sauvegarder l'état de lecture
    suspend fun savePlaybackState(
        trackId: Long,
        position: Long,
        isPlaying: Boolean
    ) {
        context.dataStore.edit { prefs ->
            prefs[Keys.LAST_TRACK_ID] = trackId
            prefs[Keys.LAST_POSITION] = position
            prefs[Keys.WAS_PLAYING] = isPlaying
        }
    }

    // Récupérer l'ID de la dernière chanson
    val lastTrackId: Flow<Long?> = context.dataStore.data.map { prefs ->
        prefs[Keys.LAST_TRACK_ID]
    }

    // Récupérer la dernière position
    val lastPosition: Flow<Long> = context.dataStore.data.map { prefs ->
        prefs[Keys.LAST_POSITION] ?: 0L
    }

    // Savoir si la musique jouait
    val wasPlaying: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.WAS_PLAYING] ?: false
    }

    // Effacer l'état (optionnel)
    suspend fun clearPlaybackState() {
        context.dataStore.edit { it.clear() }
    }
}