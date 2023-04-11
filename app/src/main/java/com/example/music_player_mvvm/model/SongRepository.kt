package com.example.music_player_mvvm.model

import android.annotation.SuppressLint
import android.content.Context

@SuppressLint("StaticFieldLeak")
object SongRepository {
    private lateinit var context: Context
    var songs: List<Song> = listOf()

    fun initialize(context: Context) {
        this.context = context
        val contentResolver = context.contentResolver
        val cursor = contentResolver.query(SongProvider.SONG_PROVIDER_URI, null, null, null, null)
        if (cursor != null) {
            songs = SongProvider.getSongsFromCursor(cursor)
            cursor.close()
        }
    }

    fun getDefaultSongs(): List<Song> {
        return songs.take(3)
    }
}
