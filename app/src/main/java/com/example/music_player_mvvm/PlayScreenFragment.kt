package com.example.music_player_mvvm

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.music_player_mvvm.databinding.FragmentPlayScreenBinding
import com.example.music_player_mvvm.views.DetailActivityView
import com.example.musicplayer.ui.controllers.DetailActivityController



class PlayScreenFragment : Fragment() {
    private lateinit var detailActivityView: DetailActivityView
    private lateinit var detailActivityController: DetailActivityController
    private var _binding: FragmentPlayScreenBinding? = null
    private val binding get() = _binding!!

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(CURRENT_SONG_KEY, detailActivityController.currentSongIndex)
        outState.putInt(PLAYBACK_POSITION_KEY, MediaPlayerHolder.mediaPlayer?.currentPosition ?: 0)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlayScreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        detailActivityView = DetailActivityView(binding)
        detailActivityController =
            DetailActivityController(detailActivityView, this, SongRepository.songs)
        detailActivityController.initSongInfo()
        detailActivityController.setupSeekBarChangeListener()
        detailActivityController.setupButtonClickListeners()
        detailActivityController.setupMotionLayoutTransitionListener()

        if (savedInstanceState != null) {
            detailActivityController.currentSongIndex =
                savedInstanceState.getInt(CURRENT_SONG_KEY)
            detailActivityController.playbackPositionBeforeTransition =
                savedInstanceState.getInt(PLAYBACK_POSITION_KEY)
        }
    }
    companion object{
        const val CURRENT_SONG_KEY: String = "currentSongIndex"
        const val  PLAYBACK_POSITION_KEY: String ="playbackPosition"
        const val SONG_TITLE_KEY = "songTitle"
    }

}