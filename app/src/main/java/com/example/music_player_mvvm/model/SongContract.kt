package com.example.music_player_mvvm.model

import android.net.Uri

object SongContract {
    private const val AUTHORITY = "com.example.music_player_mvvm.provider"
    private const val BASE_PATH = "songs"
    val CONTENT_URI: Uri = Uri.parse("content://$AUTHORITY/$BASE_PATH")

    object Columns {
        const val ID = "_id"
        const val SONG_NAME = "song_name"
        const val SONG_URI = "song_uri"
        const val ALBUM_ART_URI = "album_art_uri"
    }
}
