package com.example.music_player_mvvm

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.constraintlayout.motion.widget.MotionLayout
import com.bumptech.glide.Glide
import com.example.music_player_mvvm.databinding.FragmentPlayScreenBinding


class PlayScreenFragment : Fragment() {

    private var _binding: FragmentPlayScreenBinding? = null
    private val binding get() = _binding!!

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var songs: List<Song>
    var currentSongIndex: Int = 0
    var playbackPositionBeforeTransition: Int = 0

    private val updateSeekBar = object : Runnable {
        override fun run() {
            MediaPlayerHolder.mediaPlayer?.let { mediaPlayer ->
                binding.seekBar.progress = mediaPlayer.currentPosition
                handler.postDelayed(this, 1000)
            }
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


        songs =  SongRepository.songs
        setupButtonClickListeners()
        initSongInfo()
        setupSeekBarChangeListener()
        setupMotionLayoutTransitionListener()
    }


    private fun setupButtonClickListeners() {
        binding.playPauseButton.setOnClickListener { onPlayPauseButtonClick() }
        binding.previousButton.setOnClickListener { onPreviousButtonClick() }
        binding.nextButton.setOnClickListener { onNextButtonClick() }
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
            handler.postDelayed(updateSeekBar, 1000)
            binding.playPauseButton.setImageResource(android.R.drawable.ic_media_pause)
        }
    }

    private fun onPreviousButtonClick() {
        if (currentSongIndex > 0) {
            currentSongIndex -= 1
        } else {
            currentSongIndex = songs.size - 1
        }
        playSong()
        updateSongInfo()
    }

    private fun onNextButtonClick() {
        if (currentSongIndex < songs.size - 1) {
            currentSongIndex += 1
        } else {
            currentSongIndex = 0
        }
        playSong()
        updateSongInfo()
    }

    private fun onPlayPauseButtonClick() {
        MediaPlayerHolder.mediaPlayer?.let { mediaPlayer ->
            if (mediaPlayer.isPlaying) {
                mediaPlayer.pause()
                binding.playPauseButton.setImageResource(android.R.drawable.ic_media_play)
                handler.removeCallbacks(updateSeekBar)
            } else {
                mediaPlayer.start()
                binding.playPauseButton.setImageResource(android.R.drawable.ic_media_pause)
                handler.postDelayed(updateSeekBar, 1000)
            }
        }
    }

    private fun updateSongInfo() {
        val songTitles = songs.map { it.title }
        val albumArts = songs.map { it.albumArtUri }

        binding.songTitleTextView.text = songTitles[currentSongIndex]
        Glide.with(this).load(albumArts[currentSongIndex]).into(binding.albumArtImageView)

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
                    handler.postDelayed(updateSeekBar, 1000)
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

    }
}
