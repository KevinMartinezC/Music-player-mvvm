package com.example.music_player_mvvm.controllers


import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.music_player_mvvm.MediaPlayerHolder
import com.example.music_player_mvvm.Song


class PlayScreenViewModel : ViewModel() {

    private val handler = Handler(Looper.getMainLooper())

    private val progressMutableLiveData = MutableLiveData<Int>()
    fun progress(): LiveData<Int> = progressMutableLiveData

    private val playPauseButtonMutableLiveData = MutableLiveData<Int>()
    fun playPauseButton(): LiveData<Int> = playPauseButtonMutableLiveData

    private val songIndexMutableLiveData = MutableLiveData<Int>()
    fun songIndex(): LiveData<Int> = songIndexMutableLiveData

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
    }

    fun onNextButtonClick(songs: List<Song>) {
        val currentSongIndex = songIndexMutableLiveData.value ?: 0
        val newSongIndex = if (currentSongIndex < songs.size - 1) {
            currentSongIndex + 1
        } else {
            0
        }
        songIndexMutableLiveData.postValue(newSongIndex)
    }

}
