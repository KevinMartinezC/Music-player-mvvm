package com.example.music_player_mvvm

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri

class SongProvider : ContentProvider() {
    private val songs = listOf(
        Song(
            SONG_NAME_ONE,
            Uri.parse("android.resource://com.example.music_player_mvvm/${R.raw.song1}"),
            Uri.parse("android.resource://com.example.music_player_mvvm/${R.drawable.album_art_1}")
        ),
        Song(
            SONG_NAME_TWO,
            Uri.parse("android.resource://com.example.music_player_mvvm/${R.raw.song2}"),
            Uri.parse("android.resource://com.example.music_player_mvvm/${R.drawable.album_art_2}")
        ),
        Song(
            SONG_NAME_THREE,
            Uri.parse("android.resource://com.example.music_player_mvvm/${R.raw.song3}"),
            Uri.parse("android.resource://com.example.music_player_mvvm/${R.drawable.album_art_3}")
        )
    )

    override fun onCreate(): Boolean {
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?
    ): Cursor {
        val matrixCursor = MatrixCursor(arrayOf("_id", "song_name", "song_uri", "album_art_uri"))

        songs.forEachIndexed { index, song ->
            matrixCursor.addRow(arrayOf(index, song.title, song.songUri.toString(), song.albumArtUri.toString()))
        }

        return matrixCursor
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        throw UnsupportedOperationException("Not supported. Read-only provider.")
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<String>?
    ): Int {
        throw UnsupportedOperationException("Not supported. Read-only provider.")
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        throw UnsupportedOperationException("Not supported. Read-only provider.")
    }

    override fun getType(uri: Uri): String? {
        return null
    }
    companion object {
        const val SONG_NAME_ONE: String = "Bar Liar"
        const val SONG_NAME_TWO: String = "Girls Like You"
        const val SONG_NAME_THREE: String = "See You Again"
    }
}
