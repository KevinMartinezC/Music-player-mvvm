package com.example.music_player_mvvm.views

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.example.music_player_mvvm.MediaPlayerHolder
import com.example.music_player_mvvm.Song
import com.example.music_player_mvvm.SongRepository
import com.example.music_player_mvvm.databinding.FragmentPlayScreenBinding
import com.example.music_player_mvvm.controllers.DetailActivityController


class PlayScreenFragment : Fragment() {
    private lateinit var detailActivityView: DetailActivityView
    private lateinit var detailActivityController: DetailActivityController
    private lateinit var viewModel: HomeScreenViewModel
    private lateinit var songs: List<Song>

    private var _binding: FragmentPlayScreenBinding? = null
    private val binding get() = _binding!!


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(CURRENT_SONG_KEY, viewModel.currentSongIndex.value ?: 0)
        outState.putInt(PLAYBACK_POSITION_KEY, MediaPlayerHolder.mediaPlayer?.currentPosition ?: 0)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this).get(HomeScreenViewModel::class.java)
        _binding = FragmentPlayScreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.loadSongsFromProvider() // add this line to load songs
        detailActivityView = DetailActivityView(binding)
        detailActivityController = DetailActivityController(
            detailActivityView,
            this,
            viewModel.songs,
            viewModel
        )
        // Create the view model and observe the currentSongIndex property
        viewModel = ViewModelProvider(requireActivity())[HomeScreenViewModel::class.java]

        // Retrieve the arguments and pass them to the DetailActivityController
        val args = arguments
        if (args != null) {
            detailActivityController.initSongInfo(args)
        }
        detailActivityController.setupSeekBarChangeListener()
        detailActivityController.setupButtonClickListeners()
        detailActivityController.setupMotionLayoutTransitionListener()


        if (savedInstanceState != null) {
            viewModel.updateCurrentSongIndex(savedInstanceState.getInt(CURRENT_SONG_KEY))
            detailActivityController.playbackPositionBeforeTransition =
                savedInstanceState.getInt(PLAYBACK_POSITION_KEY)
            MediaPlayerHolder.mediaPlayer?.seekTo(detailActivityController.playbackPositionBeforeTransition)
        }
    }

    companion object{
        const val CURRENT_SONG_KEY: String = "currentSongIndex"
        const val  PLAYBACK_POSITION_KEY: String ="playbackPosition"
        const val SONG_TITLE_KEY = "songTitle"
        const val CURRENT_SONG_INDEX_KEY: String = "currentSongIndex"

    }
}
