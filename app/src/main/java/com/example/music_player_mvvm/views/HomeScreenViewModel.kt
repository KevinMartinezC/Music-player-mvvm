package com.example.music_player_mvvm.views

import android.media.MediaPlayer
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.music_player_mvvm.MediaPlayerHolder
import com.example.music_player_mvvm.Song
import com.example.music_player_mvvm.SongContract

import android.app.Application
import androidx.lifecycle.AndroidViewModel

class HomeScreenViewModel(application: Application) : AndroidViewModel(application) {
     private var _currentSongIndex: MutableLiveData<Int> = MutableLiveData(0)
    val currentSongIndex: LiveData<Int> = _currentSongIndex

    private val _playbackPosition = MutableLiveData<Int>()

    private var _songsBacking = mutableListOf<Song>()
    val songs: List<Song>
        get() = _songsBacking

    fun loadSongsFromProvider() {
        val songs = mutableListOf<Song>()
        val projection = arrayOf(
            SongContract.Columns.ID,
            SongContract.Columns.SONG_NAME,
            SongContract.Columns.SONG_URI,
            SongContract.Columns.ALBUM_ART_URI
        )

        getApplication<Application>().contentResolver.query(SongContract.CONTENT_URI, projection, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                do {
                    val titleIndex = cursor.getColumnIndex(SongContract.Columns.SONG_NAME)
                    val songUriIndex = cursor.getColumnIndex(SongContract.Columns.SONG_URI)
                    val albumArtUriIndex = cursor.getColumnIndex(SongContract.Columns.ALBUM_ART_URI)

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

        _songsBacking.clear()
        _songsBacking.addAll(songs)
    }

    fun updateCurrentSongIndex(newIndex: Int) {
        _currentSongIndex.postValue(newIndex)
    }
    fun updatePlaybackPosition(position: Int) {
        _playbackPosition.postValue(position)
    }
    fun playSelectedSong(position: Int) {
        MediaPlayerHolder.mediaPlayer?.release()
        _currentSongIndex.value = position
        MediaPlayerHolder.mediaPlayer = MediaPlayer.create(getApplication(), _songsBacking[position].songUri)
        MediaPlayerHolder.mediaPlayer?.start()
    }
}
