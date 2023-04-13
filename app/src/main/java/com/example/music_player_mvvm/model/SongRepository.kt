package com.example.music_player_mvvm.model

import android.annotation.SuppressLint
import android.content.Context

@SuppressLint("StaticFieldLeak")
object SongRepository {
    private const val InitialNumOfSongs = 3
    private lateinit var context: Context
    var songs: List<Song> = listOf()

    fun initialize(context: Context) {
        this.context = context
        val contentResolver = context.contentResolver
        contentResolver.query(
            SongProvider.SONG_PROVIDER_URI,
            null,
            null,
            null,
            null
        )?.let {
            songs = SongProvider.getSongsFromCursor(it)
            it.close()
        }
    }

    fun getDefaultSongs(): List<Song> {
        return songs.take(InitialNumOfSongs)
    }
}
