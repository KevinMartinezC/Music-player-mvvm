package com.example.music_player_mvvm.viewmodel


import android.app.Application
import android.content.Intent
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.music_player_mvvm.model.media.MediaPlayerHolder
import com.example.music_player_mvvm.model.Song
import com.example.music_player_mvvm.views.PlayScreenFragment.Companion.SONG_TITLE_KEY


class PlayScreenViewModel(application: Application) : AndroidViewModel(application) {


    private val handler = Handler(Looper.getMainLooper())

    private val progressMutableLiveData = MutableLiveData<Int>()
    fun progress(): LiveData<Int> = progressMutableLiveData

    private val playPauseButtonMutableLiveData = MutableLiveData<Int>()
    fun playPauseButton(): LiveData<Int> = playPauseButtonMutableLiveData

    private val songIndexMutableLiveData = MutableLiveData<Int>()
    fun songIndex(): LiveData<Int> = songIndexMutableLiveData

    private val currentSongMutableLiveData = MutableLiveData<Song>()
    fun currentSong(): LiveData<Song> = currentSongMutableLiveData

    private val playbackPositionMutableLiveData = MutableLiveData<Int>()
    val playbackPositionLiveData: LiveData<Int> = playbackPositionMutableLiveData


    fun updatePlaybackPosition(position: Int) {
        playbackPositionMutableLiveData.postValue(position)
    }

    fun playSong() {
        handler.postDelayed(updateSeekBar, 1000)

    }

    private val updateSeekBar = object : Runnable {
        override fun run() {
            MediaPlayerHolder.mediaPlayer?.let { mediaPlayer ->
                progressMutableLiveData.postValue(mediaPlayer.currentPosition)
                handler.postDelayed(this, 1000)
            }
        }
    }

    fun onPlayPauseButtonClick() {
        MediaPlayerHolder.mediaPlayer?.let { mediaPlayer ->
            if (mediaPlayer.isPlaying) {
                mediaPlayer.pause()
                handler.removeCallbacks(updateSeekBar)
                playPauseButtonMutableLiveData.postValue(android.R.drawable.ic_media_play)
            } else {
                mediaPlayer.start()
                handler.postDelayed(updateSeekBar, 1000)
                playPauseButtonMutableLiveData.postValue(android.R.drawable.ic_media_pause)
            }
        }
    }

    fun onPreviousButtonClick(songs: List<Song>) {
        val currentSongIndex = songIndexMutableLiveData.value ?: 0
        val newSongIndex = if (currentSongIndex > 0) {
            currentSongIndex - 1
        } else {
            songs.size - 1
        }
        songIndexMutableLiveData.postValue(newSongIndex)
        updateCurrentSong(songs, newSongIndex)

    }

    fun onNextButtonClick(songs: List<Song>) {
        val currentSongIndex = songIndexMutableLiveData.value ?: 0
        val newSongIndex = if (currentSongIndex < songs.size - 1) {
            currentSongIndex + 1
        } else {
            0
        }
        songIndexMutableLiveData.postValue(newSongIndex)
        updateCurrentSong(songs, newSongIndex)

    }


    private fun sendSongChangedBroadcast(currentSong: Song) {
        val intent = Intent(ACTION_SONG_CHANGED)
        intent.putExtra("song_title", currentSong.title)
        intent.putExtra("song_uri", currentSong.songUri.toString())
        intent.putExtra("album_art_uri", currentSong.albumArtUri.toString())
        LocalBroadcastManager.getInstance(getApplication()).sendBroadcast(intent)
    }

    private fun updateCurrentSong(songs: List<Song>, currentSongIndex: Int) {
        val currentSong = songs.getOrNull(currentSongIndex)
        currentSong?.let {
            currentSongMutableLiveData.postValue(it)
            sendSongChangedBroadcast(it)
        }
    }


    companion object {
        const val ACTION_SONG_CHANGED = "com.example.music_player_mvvm.ACTION_SONG_CHANGED"
    }

}