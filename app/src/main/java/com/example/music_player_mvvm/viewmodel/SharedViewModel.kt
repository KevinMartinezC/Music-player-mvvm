package com.example.music_player_mvvm.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.music_player_mvvm.model.Song

class SharedViewModel : ViewModel() {
    private val _selectedSongs = MutableLiveData<List<Song>>(emptyList())
    val selectedSongs: LiveData<List<Song>> get() = _selectedSongs

    fun updateSelectedSongs(songs: List<Song>) {
        _selectedSongs.value = songs
    }
}
