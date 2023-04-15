package com.example.music_player_mvvm.model

import android.net.Uri
import androidx.annotation.DrawableRes

data class Song(
    val title: String,
    val songUri: Uri,
    val albumArtUri: Uri,
    var selected: Boolean = false,
) {
    companion object {
        fun create(
            name: String,
            songFile: Int,
            @DrawableRes songImageRes: Int,
        ): Song = Song(
            title = name,
            songUri = Uri.parse("${SongProvider.URI_PATH}$songFile"),
            albumArtUri = Uri.parse("${SongProvider.URI_PATH}$songImageRes")
        )
    }
}
