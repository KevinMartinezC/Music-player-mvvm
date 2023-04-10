package com.example.music_player_mvvm.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.music_player_mvvm.model.Song
import com.example.music_player_mvvm.model.SongRepository


class SharedViewModel(private val songRepository: SongRepository) : ViewModel() {
    private val _songs = MutableLiveData<List<Song>>(songRepository.getDefaultSongs())
    val songs: LiveData<List<Song>> = _songs

    private val _songsLoaded = MutableLiveData(false)
    val songsLoaded: LiveData<Boolean> = _songsLoaded


    fun setSongs(songs: List<Song>) {
        _songs.value = songs
        _songsLoaded.value = true
    }


    fun addNewSongs(newSongs: List<Song>) {
        _songs.value = _songs.value.orEmpty() + newSongs
    }
}


