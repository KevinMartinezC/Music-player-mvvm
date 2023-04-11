package com.example.music_player_mvvm.ui.homeview

import android.media.MediaPlayer
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.music_player_mvvm.model.media.MediaPlayerHolder
import com.example.music_player_mvvm.R
import com.example.music_player_mvvm.model.Song
import com.example.music_player_mvvm.ui.homeview.adapter.SongListAdapter
import com.example.music_player_mvvm.model.SongRepository
import com.example.music_player_mvvm.databinding.FragmentHomeScreenBinding
import com.example.music_player_mvvm.ui.homeview.viewmodel.HomeScreenViewModel
import com.example.music_player_mvvm.ui.settingview.viewmodel.SettingScreenViewModel
import com.example.music_player_mvvm.ui.settingview.viewmodel.CustomViewModelFactory
import com.example.music_player_mvvm.ui.playerview.PlayScreenFragment


class HomeScreenFragment : Fragment() {
    private var defaultSongs: List<Song> = listOf()

    private val viewmodel: HomeScreenViewModel by viewModels()
    private val sharedViewModel: SettingScreenViewModel by activityViewModels {
        CustomViewModelFactory(SongRepository)
    }

    private var currentSongIndex: Int = 0
    private lateinit var recyclerView: RecyclerView
    private var songs: MutableList<Song> = mutableListOf()
    private var _binding: FragmentHomeScreenBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeScreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        SongRepository.initialize(requireActivity())

        initViews()

        viewmodel.loadSongsFromProvider(requireActivity().contentResolver)
        defaultSongs = SongRepository.getDefaultSongs()

        sharedViewModel.songs.observe(viewLifecycleOwner) { newSongs ->
            songs = newSongs.toMutableList()
            setupRecyclerView()
        }

    }

    private fun initViews() {
        recyclerView = binding.recyclerView

        binding.playButton.setOnClickListener {
            playPlaylist()
        }

        binding.randomStartButton.setOnClickListener {
            toggleRandomStart()
        }

        binding.settingsButton.setOnClickListener {
            findNavController().navigate(R.id.action_homeScreenFragment_to_settingScreenFragment)
        }
    }

    private fun playPlaylist() {
        if (songs.isNotEmpty()) {
            currentSongIndex = 0 // Always set the index to the first song
            playSelectedSong(currentSongIndex)
            navigateToDetailActivity(currentSongIndex)
        } else {
            Toast.makeText(
                context,
                getString(R.string.no_songs_in_the_playlist),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun toggleRandomStart() {
        if (songs.isNotEmpty()) {
            currentSongIndex = (0 until songs.size).random()
            playSelectedSong(currentSongIndex)
            navigateToDetailActivity(currentSongIndex)
        } else {
            Toast.makeText(
                context,
                getString(R.string.no_songs_in_the_playlist),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun setupRecyclerView() {
        val layoutManager = LinearLayoutManager(context)
        recyclerView.layoutManager = layoutManager

        val adapter = SongListAdapter(songs, this::onSongClick)
        recyclerView.adapter = adapter

        val dividerItemDecoration = DividerItemDecoration(
            recyclerView.context,
            layoutManager.orientation
        )
        recyclerView.addItemDecoration(dividerItemDecoration)
    }

    private fun onSongClick(position: Int) {
        playSelectedSong(position)
        navigateToDetailActivity(position)
    }

    private fun playSelectedSong(position: Int) {
        MediaPlayerHolder.mediaPlayer?.release()
        currentSongIndex = position
        MediaPlayerHolder.mediaPlayer = MediaPlayer.create(context, songs[position].songUri)
        MediaPlayerHolder.mediaPlayer?.start()
    }

    private fun navigateToDetailActivity(position: Int) {
        // Create a bundle to pass the song title
        val bundle = Bundle().apply {
            putString(PlayScreenFragment.SONG_TITLE_KEY, songs[position].title)
        }
        // Navigate to the PlayScreenFragment with the bundle
        findNavController().navigate(R.id.action_homeScreenFragment_to_playScreenFragment, bundle)
    }
}

