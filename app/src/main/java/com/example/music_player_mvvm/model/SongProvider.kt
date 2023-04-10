package com.example.music_player_mvvm.model

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import com.example.music_player_mvvm.R

class SongProvider : ContentProvider() {
    private val _songs = mutableListOf(

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

    val songs: List<Song>
        get() = _songs.toList()
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
        if (values == null) {
            throw IllegalArgumentException("ContentValues cannot be null")
        }

        val title = values.getAsString("song_name")
        val songUri = Uri.parse(values.getAsString("song_uri"))
        val albumArtUri = Uri.parse(values.getAsString("album_art_uri"))

        val song = Song(title, songUri, albumArtUri)
        _songs.add(song)

        return ContentUris.withAppendedId(uri, (_songs.size - 1).toLong())
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

   /* companion object {
        // Add your constants here, e.g.:
        // const val AUTHORITY = "com.example.music_player_mvvm.SongProvider"
        // val SONG_PROVIDER_URI: Uri = Uri.parse("content://$AUTHORITY/songs")
    }*/

    companion object {
        const val SONG_NAME_ONE: String = "Bar Liar"
        const val SONG_NAME_TWO: String = "Girls Like You"
        const val SONG_NAME_THREE: String = "See You Again"
        const val AUTHORITY = "com.example.music_player_mvvm.provider"
         val SONG_PROVIDER_URI: Uri = Uri.parse("content://$AUTHORITY/songs")
    }
}
