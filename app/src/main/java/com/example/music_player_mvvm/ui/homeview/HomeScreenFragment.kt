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
import com.example.music_player_mvvm.ui.playerview.PlayScreenFragment.Companion.VALOR_INITIAL_INDEX


class HomeScreenFragment : Fragment() {
    private lateinit var viewModel: HomeScreenViewModel

    //private val viewModel: HomeScreenViewModel by viewModels()
    private val sharedViewModel: SettingScreenViewModel by activityViewModels {
        CustomViewModelFactory(SongRepository)
    }

    // TODO: Move to constant

    private var currentSongIndex: Int = VALOR_INITIAL_INDEX
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
        addSongs()
        observeDeletedSongs()
    }

    private fun observeDeletedSongs() {
        sharedViewModel.deletedSongPosition.observe(viewLifecycleOwner) { position ->
            position?.let {
                // Remove the song from the local list
                songs.removeAt(it)
                recyclerView.adapter?.notifyItemRemoved(it)
            }
        }
    }

    private fun addSongs() {
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
            currentSongIndex = 0
            playSelectedSong(currentSongIndex)
            navigateToDetailActivity(currentSongIndex)
        } else {
            showNoSongsToast()
        }
    }

    private fun toggleRandomStart() {
        if (songs.isNotEmpty()) {
            currentSongIndex = (0 until songs.size).random()
            playSelectedSong(currentSongIndex)
            navigateToDetailActivity(currentSongIndex)
        } else {
            showNoSongsToast()
        }
    }

    private fun showNoSongsToast() {
        context?.let { ctx ->
            Toast.makeText(
                ctx,
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
        val bundle = Bundle().apply {
            putString(PlayScreenFragment.SONG_TITLE_KEY, songs[position].title)
        }
        findNavController().navigate(R.id.action_homeScreenFragment_to_playScreenFragment, bundle)
    }
}

