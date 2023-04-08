package com.example.music_player_mvvm.views


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.music_player_mvvm.R
import com.example.music_player_mvvm.SongListAdapter
import com.example.music_player_mvvm.SongRepository.songs
import com.example.music_player_mvvm.databinding.FragmentHomeScreenBinding


class HomeScreenFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SongListAdapter
    private lateinit var viewModel: HomeScreenViewModel

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

        // Create the view model and observe the currentSongIndex property
        viewModel = ViewModelProvider(this)[HomeScreenViewModel::class.java]

        // Load the songs from the content provider and update the view model's songs property
        viewModel.loadSongsFromProvider()

        // Initialize the views and set up the RecyclerView
        recyclerView = binding.recyclerView
        adapter = SongListAdapter(viewModel.songs, this::onSongClick)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.addItemDecoration(DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL))

        // Observe the currentSongIndex after initializing the adapter
        viewModel.currentSongIndex.observe(viewLifecycleOwner) { index ->
            // Update the adapter to highlight the selected song
            adapter.setSelectedSongIndex(index)
        }
    }

    private fun onSongClick(position: Int) {
        viewModel.playSelectedSong(position)
        navigateToDetailActivity(position)
    }

    private fun navigateToDetailActivity(position: Int) {
        val song = viewModel.songs.getOrNull(position) ?: return
        val bundle = Bundle().apply {
            putString(PlayScreenFragment.SONG_TITLE_KEY, song.title)
            putInt(PlayScreenFragment.CURRENT_SONG_INDEX_KEY, position)
        }

        // Navigate to the PlayScreenFragment with the bundle
        findNavController().navigate(R.id.action_homeScreenFragment_to_playScreenFragment, bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
