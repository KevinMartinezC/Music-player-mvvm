package com.example.music_player_mvvm.views

import android.content.ContentValues
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.music_player_mvvm.R
import com.example.music_player_mvvm.databinding.FragmentHomeScreenBinding
import com.example.music_player_mvvm.databinding.FragmentSettingScreenBinding
import com.example.music_player_mvvm.model.Song
import com.example.music_player_mvvm.model.SongProvider.Companion.SONG_PROVIDER_URI
import com.example.music_player_mvvm.viewmodel.SharedViewModel
import com.example.music_player_mvvm.views.adapter.SongListAdapter


class SettingScreenFragment : Fragment() {
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private var songsAdded = false

    private var _binding: FragmentSettingScreenBinding? = null
    private val binding get() = _binding!!
    private lateinit var recyclerView: RecyclerView
    private lateinit var songs: MutableList<Song>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingScreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
        loadSongsFromProvider()

        if (!songsAdded) {
            addNewSongsToProvider()
            songsAdded = true
        }

        binding.addButton.setOnClickListener {
            addSongs()
        }
    }

    private fun initViews() {
        recyclerView = binding.recyclerView

    }

    private fun setupRecyclerView() {
        val layoutManager = LinearLayoutManager(context)
        recyclerView.layoutManager = layoutManager

        val adapter = SettingSongListAdapter(songs, this::onSongClick)
        recyclerView.adapter = adapter

        val dividerItemDecoration = DividerItemDecoration(
            recyclerView.context,
            layoutManager.orientation
        )
        recyclerView.addItemDecoration(dividerItemDecoration)
    }


    private fun onSongClick(position: Int) {
        // Toggle the selected state of the song
        songs[position].selected = !songs[position].selected

        // Update the RecyclerView to reflect the changes
        recyclerView.adapter?.notifyDataSetChanged()
    }



    // Implement your logic for adding new songs
     private fun addSongs() {
        // Add the selected songs to the shared ViewModel
        val selectedSongs = songs.filter { it.selected }
        sharedViewModel.updateSelectedSongs(selectedSongs)

        // Reset the selected state for all songs
        songs.forEach { it.selected = false }

        // Update the RecyclerView to reflect the changes
        recyclerView.adapter?.notifyDataSetChanged()
    }


    private fun addNewSongsToProvider() {
        val newSongs = listOf(
            Song(
                SONG_NAME_FOUR,
                Uri.parse("android.resource://com.example.music_player_mvvm/${R.raw.song4}"),
                Uri.parse("android.resource://com.example.music_player_mvvm/${R.drawable.album_art_4}")),
            Song(
                SONG_NAME_FIVE,
                Uri.parse("android.resource://com.example.music_player_mvvm/${R.raw.song5}"),
                Uri.parse("android.resource://com.example.music_player_mvvm/${R.drawable.album_art_5}")),
            Song(
                SONG_NAME_SIX,
                Uri.parse("android.resource://com.example.music_player_mvvm/${R.raw.song6}"),
                Uri.parse("android.resource://com.example.music_player_mvvm/${R.drawable.album_art_6}")),
            Song(
                SONG_NAME_SEVEN,
                Uri.parse("android.resource://com.example.music_player_mvvm/${R.raw.song7}"),
                Uri.parse("android.resource://com.example.music_player_mvvm/${R.drawable.album_art_7}")),
            Song(
                SONG_NAME_EIGHT,
                Uri.parse("android.resource://com.example.music_player_mvvm/${R.raw.song8}"),
                Uri.parse("android.resource://com.example.music_player_mvvm/${R.drawable.album_art_8}")),
            Song(
                SONG_NAME_NINE,
                Uri.parse("android.resource://com.example.music_player_mvvm/${R.raw.song9}"),
                Uri.parse("android.resource://com.example.music_player_mvvm/${R.drawable.album_art_9}")),
            Song(
                SONG_NAME_TEN,
                Uri.parse("android.resource://com.example.music_player_mvvm/${R.raw.song10}"),
                Uri.parse("android.resource://com.example.music_player_mvvm/${R.drawable.album_art_10}")),
            // Add the remaining three songs with their respective URIs
        )

        for (song in newSongs) {
            val contentValues = ContentValues().apply {
                put("song_name", song.title)
                put("song_uri", song.songUri.toString())
                put("album_art_uri", song.albumArtUri.toString())
            }

            requireActivity().contentResolver.insert(SONG_PROVIDER_URI, contentValues)
        }
        loadSongsFromProvider()
    }

    private fun loadSongsFromProvider() {
        val cursor = requireActivity().contentResolver.query(
            SONG_PROVIDER_URI,
            null,
            null,
            null,
            null
        )

        songs = mutableListOf()

        cursor?.use {
            while (it.moveToNext()) {
                try {
                    val title = it.getString(it.getColumnIndexOrThrow("song_name"))
                    val songUri = Uri.parse(it.getString(it.getColumnIndexOrThrow("song_uri")))
                    val albumArtUri = Uri.parse(it.getString(it.getColumnIndexOrThrow("album_art_uri")))

                    val song = Song(title, songUri, albumArtUri)
                    songs.add(song)
                } catch (e: IllegalArgumentException) {
                    Log.e("SettingScreenFragment", "Error reading song data from provider: ${e.message}")
                }
            }
        }

        setupRecyclerView()
    }

    companion object{
        const val SONG_NAME_FOUR: String = "Sia - Chandelier "
        const val SONG_NAME_FIVE: String = "Camila Cabello - Havana"
        const val SONG_NAME_SIX: String = "MAGIC! - Rude "
        const val SONG_NAME_SEVEN: String = "Alan Walker - Faded"
        const val SONG_NAME_EIGHT: String = "Adele - Someone Like You "
        const val SONG_NAME_NINE: String = "John Legend - All of Me "
        const val SONG_NAME_TEN: String = "Avicii ft. Aloe Blacc - Wake Me Up "

    }
}