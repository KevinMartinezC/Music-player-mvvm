package com.example.music_player_mvvm.ui.playerview.viewmodel


import android.app.Application
import android.content.Intent
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.music_player_mvvm.model.media.MediaPlayerHolder
import com.example.music_player_mvvm.model.Song


class PlayScreenViewModel(application: Application) : AndroidViewModel(application) {

    private val handler = Handler(Looper.getMainLooper())

    private val progressMutableLiveData = MutableLiveData<Int>()
    val progress: LiveData<Int>
        get() {
            progressMutableLiveData.value = INITIAL_PROGRESS_VALUE
            return progressMutableLiveData
        }

    private val playPauseButtonMutableLiveData = MutableLiveData<Int>()
    val playPauseButton: LiveData<Int>
        get() = playPauseButtonMutableLiveData

    private val songIndexMutableLiveData = MutableLiveData<Int>()
    val songIndex: LiveData<Int>
        get() = songIndexMutableLiveData

    private val currentSongMutableLiveData = MutableLiveData<Song>()
    val currentSong: LiveData<Song>
        get() = currentSongMutableLiveData

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
        val currentSongIndex = songIndexMutableLiveData.value ?: INITIAL_INDEX
        val newSongIndex = if (currentSongIndex > 0) {
            currentSongIndex - 1
        } else {
            songs.size - 1
        }
        songIndexMutableLiveData.postValue(newSongIndex)
        updateCurrentSong(songs, newSongIndex)

    }

    fun onNextButtonClick(songs: List<Song>) {
        val currentSongIndex = songIndexMutableLiveData.value ?: INITIAL_INDEX
        val newSongIndex = if (currentSongIndex < songs.size - 1) {
            currentSongIndex + 1
        } else {
            INITIAL_INDEX
        }
        songIndexMutableLiveData.postValue(newSongIndex)
        updateCurrentSong(songs, newSongIndex)
    }

    private fun sendSongChangedBroadcast(currentSong: Song) {
        val intent = Intent(ACTION_SONG_CHANGED).apply {
            putExtra(SONG_TITLE, currentSong.title)
            putExtra(SONG_URI, currentSong.songUri.toString())
            putExtra(ALBUM_ART_URI, currentSong.albumArtUri.toString())
        }

        LocalBroadcastManager.getInstance(getApplication()).sendBroadcast(intent)
    }

    private fun updateCurrentSong(songs: List<Song>, currentSongIndex: Int) {
        songs.getOrNull(currentSongIndex)?.let {
            currentSongMutableLiveData.postValue(it)
            sendSongChangedBroadcast(it)
        }
    }

    companion object {
        const val ACTION_SONG_CHANGED = "com.example.music_player_mvvm.ACTION_SONG_CHANGED"
        const val SONG_TITLE = "song_title"
        const val SONG_URI = "song_uri"
        const val ALBUM_ART_URI = "album_art_uri"
        const val INITIAL_PROGRESS_VALUE = 0
        const val INITIAL_INDEX = 0
    }
}
