package com.example.music_player_mvvm.ui.homeview.viewmodel

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.music_player_mvvm.model.Song
import com.example.music_player_mvvm.model.SongContract

class HomeScreenViewModel : ViewModel() {
    private val songsMutableLiveData = MutableLiveData<List<Song>>()
    val songs: LiveData<List<Song>>
        get() = songsMutableLiveData


}
