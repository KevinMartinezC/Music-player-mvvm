package com.example.music_player_mvvm.ui.homeview.viewmodel

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.music_player_mvvm.model.Song
import com.example.music_player_mvvm.model.SongContract
import java.lang.Exception

class HomeScreenViewModel : ViewModel() {
    private val songsMutableLiveData = MutableLiveData<List<Song>>()
    val songs: LiveData<List<Song>>
        get() = songsMutableLiveData

    // TODO: Why function instead of val?

    fun loadSongsFromProvider(contentResolver: ContentResolver) {
        val songs = mutableListOf<Song>()
        val projection = arrayOf(
            SongContract.Columns.ID,
            SongContract.Columns.SONG_NAME,
            SongContract.Columns.SONG_URI,
            SongContract.Columns.ALBUM_ART_URI
        )

        contentResolver.query(
            SongContract.CONTENT_URI,
            projection,
            null,
            null,
            null
        )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    do {
                        val titleIndex = cursor.getColumnIndex(SongContract.Columns.SONG_NAME)
                        val songUriIndex = cursor.getColumnIndex(SongContract.Columns.SONG_URI)
                        val albumArtUriIndex =
                            cursor.getColumnIndex(SongContract.Columns.ALBUM_ART_URI)

                        if (titleIndex >= 0 && songUriIndex >= 0 && albumArtUriIndex >= 0) {
                            val title = cursor.getString(titleIndex)
                            val songUri = Uri.parse(cursor.getString(songUriIndex))
                            val albumArtUri = Uri.parse(cursor.getString(albumArtUriIndex))

                            val song = Song(title, songUri, albumArtUri)
                            songs.add(song)
                        }
                    } while (cursor.moveToNext())
                }
        }
        songsMutableLiveData.postValue(songs)
    }
}
