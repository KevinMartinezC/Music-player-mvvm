package com.example.music_player_mvvm.ui.playerview

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.fragment.app.viewModels
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.music_player_mvvm.R
import com.example.music_player_mvvm.model.media.MediaPlayerHolder
import com.example.music_player_mvvm.model.Song
import com.example.music_player_mvvm.model.SongRepository
import com.example.music_player_mvvm.ui.playerview.viewmodel.PlayScreenViewModel
import com.example.music_player_mvvm.databinding.FragmentPlayScreenBinding
import com.example.music_player_mvvm.ui.playerview.viewmodel.PlayScreenViewModelFactory


class PlayScreenFragment : Fragment() {
    private lateinit var viewModel: PlayScreenViewModel

    private var _binding: FragmentPlayScreenBinding? = null
    private val binding get() = _binding!!
    private lateinit var songs: List<Song>

    var currentSongIndex: Int = VALOR_INITIAL_INDEX
    var playbackPositionBeforeTransition: Int = INITIAL_PLAYBACK_POSITION


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModel: PlayScreenViewModel by viewModels {
            PlayScreenViewModelFactory(requireActivity().application)
        }
        this.viewModel = viewModel

        setupObservers()
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

        songs = SongRepository.songs
        setupButtonClickListeners()
        initSongInfo()
        setupSeekBarChangeListener()
        setupMotionLayoutTransitionListener()
        observerPlaybackPosition()
    }

    private fun observerPlaybackPosition() {
        viewModel.playbackPositionLiveData.observe(viewLifecycleOwner) { position ->
            MediaPlayerHolder.mediaPlayer?.seekTo(position)
        }
    }

    override fun onStop() {
        super.onStop()
        MediaPlayerHolder.mediaPlayer?.let { mediaPlayer ->
            viewModel.updatePlaybackPosition(mediaPlayer.currentPosition)
        }
    }

    override fun onResume() {
        super.onResume()
        val filter = IntentFilter(PlayScreenViewModel.ACTION_SONG_CHANGED)

        // TODO: It could be a pattern
        LocalBroadcastManager.getInstance(requireContext())
            .registerReceiver(songChangedReceiver, filter)
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(songChangedReceiver)
    }


    private fun setupObservers() {
        viewModel.progress.observe(this) { progress ->
            binding.seekBar.progress = progress
        }

        viewModel.playPauseButton.observe(this) { buttonDrawable ->
            binding.playPauseButton.setImageResource(buttonDrawable)
        }

        viewModel.songIndex.observe(this) { newIndex ->
            currentSongIndex = newIndex
            playSong()
        }

        viewModel.currentSong.observe(this) { currentSong ->
            updateSongInfo(currentSong)
        }

    }

    private fun setupButtonClickListeners() = with(binding) {
        playPauseButton.setOnClickListener { viewModel.onPlayPauseButtonClick() }
        previousButton.setOnClickListener { viewModel.onPreviousButtonClick(songs) }
        nextButton.setOnClickListener { viewModel.onNextButtonClick(songs) }
        settingsButton.setOnClickListener {
            findNavController().navigate(R.id.action_playScreenFragment_to_settingScreenFragment)
        }
    }

    private val songChangedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.takeIf { it.action == PlayScreenViewModel.ACTION_SONG_CHANGED }?.let {
                val songTitle = it.getStringExtra(SONG_TITLE).orEmpty()
                val songUri = Uri.parse(it.getStringExtra(SONG_URI).orEmpty())
                val albumArtUri = Uri.parse(it.getStringExtra(ALBUM_ART_URI).orEmpty())
                val song = Song(songTitle, songUri, albumArtUri)
                updateSongInfo(song)
                context?.showSongChangedToast(songTitle)
            }
        }
    }

    private fun Context.showSongChangedToast(songTitle: String) {
        Toast.makeText(
            this,
            getString(R.string.song_changed, songTitle), Toast.LENGTH_SHORT
        ).show()
    }

    private fun initSongInfo() {
        val args = arguments
        val songTitle = args?.getString(SONG_TITLE_KEY).orEmpty()
        binding.songTitleTextView.text = songTitle
        currentSongIndex = songs.indexOfFirst { it.title == songTitle }
        binding.albumArtImageView.setImageURI(
            songs.getOrNull(currentSongIndex)?.albumArtUri ?: Uri.EMPTY
        )
        playSong()
    }

    private fun playSong() {
        viewModel.playSong()

        MediaPlayerHolder.mediaPlayer?.let { mediaPlayer ->
            if (mediaPlayer.isPlaying) {
                mediaPlayer.stop()
            }
            mediaPlayer.reset()
            val songUri = songs[currentSongIndex].songUri
            mediaPlayer.setDataSource(requireContext(), songUri)
            mediaPlayer.prepare()
            mediaPlayer.start()
            binding.seekBar.max = mediaPlayer.duration
            binding.playPauseButton.setImageResource(android.R.drawable.ic_media_pause)
        }
    }

    private fun updateSongInfo(song: Song) {
        binding.songTitleTextView.text = song.title
        Glide.with(this).load(song.albumArtUri).into(binding.albumArtImageView)
    }

    private fun setupSeekBarChangeListener() {
        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    MediaPlayerHolder.mediaPlayer?.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun setupMotionLayoutTransitionListener() {
        val motionLayout = binding.motionLayout
        motionLayout.setTransitionListener(object : MotionTransition {
            override fun onTransitionCompleted(motionLayout: MotionLayout?, currentId: Int) {
                MediaPlayerHolder.mediaPlayer?.let { mediaPlayer ->
                    playbackPositionBeforeTransition = mediaPlayer.currentPosition
                    mediaPlayer.stop()
                    mediaPlayer.reset()

                    val songUri = songs[currentSongIndex].songUri

                    mediaPlayer.setDataSource(requireContext(), songUri)
                    mediaPlayer.prepare()
                    mediaPlayer.seekTo(playbackPositionBeforeTransition)
                    mediaPlayer.start()
                    binding.seekBar.max = mediaPlayer.duration
                    binding.playPauseButton.setImageResource(android.R.drawable.ic_media_pause)
                }
            }
        })
    }

    companion object {
        const val SONG_TITLE_KEY = "songTitle"
        const val SONG_TITLE = "song_title"
        const val SONG_URI = "song_uri"
        const val ALBUM_ART_URI = "album_art_uri"
        const val VALOR_INITIAL_INDEX = 0
        const val INITIAL_PLAYBACK_POSITION = 0
    }
}
interface MotionTransition : MotionLayout.TransitionListener {
    override fun onTransitionStarted(
        motionLayout: MotionLayout?,
        startId: Int,
        endId: Int
    ) {
        /** Default Implementation **/
    }

    override fun onTransitionChange(
        motionLayout: MotionLayout?,
        startId: Int,
        endId: Int,
        progress: Float
    ) {
        /** Default Implementation **/
    }

    override fun onTransitionTrigger(
        motionLayout: MotionLayout?,
        triggerId: Int,
        positive: Boolean,
        progress: Float
    ) {
        /** Default Implementation **/
    }
}
