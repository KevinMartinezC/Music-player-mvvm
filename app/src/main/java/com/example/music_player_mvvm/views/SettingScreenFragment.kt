package com.example.music_player_mvvm.views

import android.content.ContentUris
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
import com.example.music_player_mvvm.databinding.FragmentSettingScreenBinding
import com.example.music_player_mvvm.model.Song
import com.example.music_player_mvvm.model.SongProvider.Companion.SONG_PROVIDER_URI
import com.example.music_player_mvvm.model.SongRepository
import com.example.music_player_mvvm.viewmodel.SharedViewModel


class SettingScreenFragment : Fragment() {
    private var songsLoaded = false

    private val sharedViewModel: SharedViewModel by activityViewModels {
        CustomViewModelFactory(SongRepository)
    }
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

        addNewSongsToProvider()

        loadSongsFromProvider()

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

        val adapter = SettingSongListAdapter(
            songs,
            this::onSongClick,
            this::onDeleteButtonClick
        )
        recyclerView.adapter = adapter

        val dividerItemDecoration = DividerItemDecoration(
            recyclerView.context,
            layoutManager.orientation
        )
        recyclerView.addItemDecoration(dividerItemDecoration)
    }

    private fun onDeleteButtonClick(position: Int) {
        val songToDelete = songs[position]

        // Delete the song from the SongProvider
        val deleteUri = ContentUris.withAppendedId(SONG_PROVIDER_URI, position.toLong())
        requireActivity().contentResolver.delete(deleteUri, null, null)

        // Remove the song from the local list and update the RecyclerView
        songs.removeAt(position)
        recyclerView.adapter?.notifyDataSetChanged()
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
        sharedViewModel.addNewSongs(selectedSongs)

        // Insert the selected songs into the SongProvider
        for (song in selectedSongs) {
            val contentValues = ContentValues().apply {
                put(SONG_NAME, song.title)
                put(SONG_URI, song.songUri.toString())
                put(ALBUM_ART_URI, song.albumArtUri.toString())
            }

            requireActivity().contentResolver.insert(SONG_PROVIDER_URI, contentValues)
        }

        // Reset the selected state for all songs
        songs.forEach { it.selected = false }

        // Update the RecyclerView to reflect the changes
        recyclerView.adapter?.notifyDataSetChanged()
    }


    private fun addNewSongsToProvider() {
        if (songsLoaded) {
            return
        }
        val newSongs = listOf(
            Song(
                SONG_NAME_FOUR,
                Uri.parse("${URI_PATH}${R.raw.song4}"),
                Uri.parse("${URI_PATH}${R.drawable.album_art_4}")),
            Song(
                SONG_NAME_FIVE,
                Uri.parse("${URI_PATH}${R.raw.song5}"),
                Uri.parse("${URI_PATH}${R.drawable.album_art_5}")),
            Song(
                SONG_NAME_SIX,
                Uri.parse("${URI_PATH}${R.raw.song6}"),
                Uri.parse("${URI_PATH}${R.drawable.album_art_6}")),
            Song(
                SONG_NAME_SEVEN,
                Uri.parse("${URI_PATH}${R.raw.song7}"),
                Uri.parse("${URI_PATH}${R.drawable.album_art_7}")),
            Song(
                SONG_NAME_EIGHT,
                Uri.parse("${URI_PATH}${R.raw.song8}"),
                Uri.parse("${URI_PATH}${R.drawable.album_art_8}")),
            Song(
                SONG_NAME_NINE,
                Uri.parse("${URI_PATH}${R.raw.song9}"),
                Uri.parse("${URI_PATH}${R.drawable.album_art_9}")),
            Song(
                SONG_NAME_TEN,
                Uri.parse("${URI_PATH}${R.raw.song10}"),
                Uri.parse("${URI_PATH}${R.drawable.album_art_10}")),
            // Add the remaining three songs with their respective URIs
        )

        for (song in newSongs) {
            val contentValues = ContentValues().apply {
                put(SONG_NAME, song.title)
                put(SONG_URI, song.songUri.toString())
                put(ALBUM_ART_URI, song.albumArtUri.toString())
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
                    val title = it.getString(it.getColumnIndexOrThrow(SONG_NAME))
                    val songUri = Uri.parse(it.getString(it.getColumnIndexOrThrow(SONG_URI)))
                    val albumArtUri = Uri.parse(it.getString(it.getColumnIndexOrThrow(ALBUM_ART_URI)))

                    val song = Song(title, songUri, albumArtUri)
                    val isNew = songs.contains(song)
                    if(!isNew){
                        songs.add(song)
                    }
                   songs = songs.distinct().toMutableList()
                } catch (e: IllegalArgumentException) {
                    Log.e(getString(R.string.settingscreenfragment),
                        getString(R.string.error_reading_song_data_from_provider, e.message))
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
        const val SONG_NAME_TEN: String = "Avicii ft - Wake Me Up "
        const val SONG_NAME = "song_name"
        const val SONG_URI = "song_uri"
        const val ALBUM_ART_URI = "album_art_uri"
        const val URI_PATH = "android.resource://com.example.music_player_mvvm/"


    }
}