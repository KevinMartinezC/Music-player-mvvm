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
import com.bumptech.glide.Glide
import com.example.music_player_mvvm.R
import com.example.music_player_mvvm.model.media.MediaPlayerHolder
import com.example.music_player_mvvm.model.Song
import com.example.music_player_mvvm.model.SongRepository
import com.example.music_player_mvvm.ui.playerview.viewmodel.PlayScreenViewModel
import com.example.music_player_mvvm.databinding.FragmentPlayScreenBinding
import com.example.music_player_mvvm.ui.playerview.viewmodel.PlayScreenViewModelFactory


class PlayScreenFragment : Fragment() {
    private lateinit var viewmodel: PlayScreenViewModel

    private var _binding: FragmentPlayScreenBinding? = null
    private val binding get() = _binding!!
    private lateinit var songs: List<Song>
    var currentSongIndex: Int = 0
    var playbackPositionBeforeTransition: Int = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewmodel: PlayScreenViewModel by viewModels {
            PlayScreenViewModelFactory(requireActivity().application)
        }

        this.viewmodel = viewmodel

        viewmodel.progress().observe(this) {
            binding.seekBar.progress = it
        }

        viewmodel.playPauseButton().observe(this) { buttonDrawable ->
            binding.playPauseButton.setImageResource(buttonDrawable)
        }

        viewmodel.songIndex().observe(this) { newIndex ->
            currentSongIndex = newIndex
            playSong()
        }

        viewmodel.currentSong().observe(this) { currentSong ->
            updateSongInfo(currentSong)
        }
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

        viewmodel.playbackPositionLiveData.observe(viewLifecycleOwner) { position ->
            MediaPlayerHolder.mediaPlayer?.seekTo(position)
        }
    }

    override fun onStop() {
        super.onStop()
        MediaPlayerHolder.mediaPlayer.let { mediaPlayer ->
            if (mediaPlayer != null) {
                viewmodel.updatePlaybackPosition(mediaPlayer.currentPosition)
            }
        }
    }

    private val songChangedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent != null && intent.action == PlayScreenViewModel.ACTION_SONG_CHANGED) {
                val songTitle = intent.getStringExtra(SONG_TITLE) ?: ""
                val songUri = Uri.parse(intent.getStringExtra(SONG_URI) ?: "")
                val albumArtUri = Uri.parse(intent.getStringExtra(ALBUM_ART_URI) ?: "")
                val song = Song(songTitle, songUri, albumArtUri)
                updateSongInfo(song)

                context?.let {
                    Toast.makeText(it,
                        getString(R.string.song_changed, songTitle), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val filter = IntentFilter(PlayScreenViewModel.ACTION_SONG_CHANGED)
        LocalBroadcastManager.getInstance(requireContext())
            .registerReceiver(songChangedReceiver, filter)
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(songChangedReceiver)
    }

    private fun setupButtonClickListeners() {
        binding.playPauseButton.setOnClickListener { viewmodel.onPlayPauseButtonClick() }
        binding.previousButton.setOnClickListener { viewmodel.onPreviousButtonClick(songs) }
        binding.nextButton.setOnClickListener { viewmodel.onNextButtonClick(songs) }
    }

    private fun initSongInfo() {
        val args = arguments
        val songTitle = args?.getString(SONG_TITLE_KEY) ?: ""
        binding.songTitleTextView.text = songTitle
        currentSongIndex = songs.indexOfFirst { it.title == songTitle }
        binding.albumArtImageView.setImageURI(
            songs.getOrNull(currentSongIndex)?.albumArtUri ?: Uri.EMPTY
        )
        playSong()
    }

    private fun playSong() {
        viewmodel.playSong()

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
            // handler.postDelayed(updateSeekBar, 1000)
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
        motionLayout.setTransitionListener(object : MotionLayout.TransitionListener {
            override fun onTransitionStarted(
                motionLayout: MotionLayout?,
                startId: Int,
                endId: Int
            ) {
            }

            override fun onTransitionChange(
                motionLayout: MotionLayout?,
                startId: Int,
                endId: Int,
                progress: Float
            ) {
            }

            override fun onTransitionCompleted(motionLayout: MotionLayout?, currentId: Int) {
                MediaPlayerHolder.mediaPlayer?.let { mediaPlayer ->
                    // Store the current position before stopping the MediaPlayer
                    playbackPositionBeforeTransition = mediaPlayer.currentPosition
                    mediaPlayer.stop()
                    mediaPlayer.reset()

                    val songUri = songs[currentSongIndex].songUri

                    mediaPlayer.setDataSource(requireContext(), songUri)
                    mediaPlayer.prepare()
                    // Seek to the position before the transition
                    mediaPlayer.seekTo(playbackPositionBeforeTransition)
                    mediaPlayer.start()
                    binding.seekBar.max = mediaPlayer.duration
                    //  handler.postDelayed(updateSeekBar, 1000)
                    binding.playPauseButton.setImageResource(android.R.drawable.ic_media_pause)

                }
            }

            override fun onTransitionTrigger(
                motionLayout: MotionLayout?,
                triggerId: Int,
                positive: Boolean,
                progress: Float
            ) {
            }
        })
    }

    companion object {
        const val SONG_TITLE_KEY: String = "songTitle"
        const val SONG_TITLE = "song_title"
        const val SONG_URI = "song_uri"
        const val ALBUM_ART_URI = "album_art_uri"
    }
}
