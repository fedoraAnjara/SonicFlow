package com.example.sonicflow.data.repository

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import com.example.sonicflow.domain.repository.AudioRepository
import kotlinx.coroutines.withContext
import com.example.sonicflow.domain.model.AudioTrack
import kotlinx.coroutines.Dispatchers
import android.provider.MediaStore

class AudioRepositoryImpl(
    private val contect: Context
) : AudioRepository{
    private fun getAlbumArtUri(albumId: Long): Uri {
        return ContentUris.withAppendedId(
            Uri.parse("content://media/external/audio/albumart"),
            albumId
        )
    }
    override suspend fun getAudioTracks(): List<AudioTrack> = withContext(Dispatchers.IO){
        val audioList = mutableListOf<AudioTrack>()

        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        val projection = arrayOf (
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"

        val  sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        val cursor = contect.contentResolver.query(
            collection,
            projection,
            selection,
            null,
            sortOrder
        )

        cursor?.use { c ->
            val idColumn = c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = c.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumColumn = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val albumIdColumn = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val durationColumn = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val dataColumn = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)

            while (c.moveToNext()) {
                val id = c.getLong(idColumn)
                val title = c.getString(titleColumn) ?: "Unknown"
                val artist = c.getString(artistColumn) ?: "Unknown Artist"
                val album = c.getString(albumColumn) ?: "Unknown Album"
                val albumId = c.getLong(albumIdColumn)
                val duration = c.getLong(durationColumn)
                val data = c.getString(dataColumn) ?: ""

                audioList.add(
                    AudioTrack(
                        id = id,
                        title = title,
                        artist = artist,
                        album = album,
                        albumId = albumId,
                        duration = duration,
                        data = data
                    )
                )
            }
        }

        return@withContext audioList


    }
}