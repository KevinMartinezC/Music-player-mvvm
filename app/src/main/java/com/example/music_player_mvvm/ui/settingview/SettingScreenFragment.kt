package com.example.music_player_mvvm.ui.settingview

import android.content.ContentValues
import android.os.Bundle
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
import com.example.music_player_mvvm.ui.settingview.viewmodel.SettingScreenViewModel
import com.example.music_player_mvvm.ui.settingview.viewmodel.CustomViewModelFactory
import com.example.music_player_mvvm.ui.settingview.adapter.SettingSongListAdapter


class SettingScreenFragment : Fragment() {

    private val viewModel: SettingScreenViewModel by activityViewModels {
        CustomViewModelFactory(SongRepository)
    }
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
        deleteSongObserver()
    }

    private fun deleteSongObserver() {
        viewModel.deletedSongPosition.observe(viewLifecycleOwner) { position ->
            position?.let {
                songs.removeAt(it)
                recyclerView.adapter?.notifyItemRemoved(it)
                viewModel.resetDeletedSongPosition()
            }
        }
    }

    private fun initViews() {
        recyclerView = binding.recyclerView
        binding.addButton.setOnClickListener {
            addSongs()
        }
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
        viewModel.deleteSong(position, requireActivity())
    }

    private fun onSongClick(position: Int) {
        songs[position].selected = !songs[position].selected
        recyclerView.adapter?.notifyItemChanged(position)
    }

    private fun addSongs() {
        val selectedSongs = songs.filter { it.selected }
        val nonDuplicateSongs = selectedSongs.filter { newSong ->
            !viewModel.songs.value.orEmpty().any { existingSong ->
                existingSong.title == newSong.title
            }
        }
        viewModel.addNewSongs(nonDuplicateSongs)

        for (song in nonDuplicateSongs) {
            val contentValues = ContentValues().apply {
                put(SONG_NAME, song.title)
                put(SONG_URI, song.songUri.toString())
                put(ALBUM_ART_URI, song.albumArtUri.toString())
            }
            requireActivity().contentResolver.insert(SONG_PROVIDER_URI, contentValues)
        }
        songs.forEachIndexed { index, song ->
            if (song.selected) {
                song.selected = false
                recyclerView.adapter?.notifyItemChanged(index)
            }
        }
    }

    private fun addNewSongsToProvider() {

        val newSongs = listOf(
            Song.create(SONG_NAME_FOUR, R.raw.song4, R.drawable.album_art_4),
            Song.create(SONG_NAME_FIVE, R.raw.song5, R.drawable.album_art_5),
            Song.create(SONG_NAME_SIX, R.raw.song6, R.drawable.album_art_6),
            Song.create(SONG_NAME_SEVEN, R.raw.song7, R.drawable.album_art_7),
            Song.create(SONG_NAME_EIGHT, R.raw.song8, R.drawable.album_art_8),
            Song.create(SONG_NAME_NINE, R.raw.song9, R.drawable.album_art_9),
            Song.create(SONG_NAME_TEN, R.raw.song10, R.drawable.album_art_10),
        )
        newSongs.forEach { song ->
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
        songs = viewModel.fetchSongsFromProvider(requireActivity())
        setupRecyclerView()
    }

    companion object {
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
    }
}
