package com.example.music_player_mvvm.controllers

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.SeekBar
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.music_player_mvvm.MediaPlayerHolder
import com.example.music_player_mvvm.views.PlayScreenFragment
import com.example.music_player_mvvm.Song
import com.example.music_player_mvvm.views.DetailActivityView
import com.example.music_player_mvvm.views.HomeScreenViewModel


class DetailActivityController(
    private val view: DetailActivityView,
    private val fragment: Fragment,
    private val songs: List<Song>,
    private val viewModel: HomeScreenViewModel


) {
    var playbackPositionBeforeTransition: Int = 0
    private val handler = Handler(Looper.getMainLooper())

    /**
     * The 'updateSeekBar' runnable is responsible for periodically updating the progress of the SeekBar
     * using the 'handler', ensuring that the updates are performed on the main thread.
     */
    private val updateSeekBar = object : Runnable {
        override fun run() {
            MediaPlayerHolder.mediaPlayer?.let { mediaPlayer ->
                view.seekBar.progress = mediaPlayer.currentPosition
                handler.postDelayed(this, 1000)
            }
        }
    }

    fun setupButtonClickListeners() {
        view.playPauseButton.setOnClickListener { onPlayPauseButtonClick() }
        view.previousButton.setOnClickListener { onPreviousButtonClick() }
        view.nextButton.setOnClickListener { onNextButtonClick() }
    }

    fun initSongInfo(args: Bundle?) {

        val songTitle = args?.getString(PlayScreenFragment.SONG_TITLE_KEY) ?: ""
        val currentSongIndex = args?.getInt(PlayScreenFragment.CURRENT_SONG_INDEX_KEY) ?: 0
        viewModel.updateCurrentSongIndex(currentSongIndex)
        view.songTitleTextView.text = songTitle
        view.albumArtImageView.setImageURI(
            songs.getOrNull(currentSongIndex)?.albumArtUri ?: Uri.EMPTY
        )

        playSong()
    }


    private fun playSong(startPosition: Int = 0) {
        val currentSongIndex = viewModel.currentSongIndex.value ?: return

        if (songs.isEmpty() || currentSongIndex < 0 || currentSongIndex >= songs.size) {
            return
        }

        MediaPlayerHolder.mediaPlayer?.let { mediaPlayer ->
            if (mediaPlayer.isPlaying) {
                mediaPlayer.stop()
            }
            mediaPlayer.reset()
            // Set the data source using a Uri
            val songUri = songs[currentSongIndex].songUri

            mediaPlayer.setDataSource(fragment.requireContext(), songUri)
            mediaPlayer.prepare()
            mediaPlayer.seekTo(startPosition)
            mediaPlayer.start()
            // Reset the playback position to 0 for each new song
            viewModel.updatePlaybackPosition(0)
            view.seekBar.progress = 0
            view.seekBar.max = mediaPlayer.duration
            handler.postDelayed(updateSeekBar, 1000)

            view.playPauseButton.setImageResource(android.R.drawable.ic_media_pause)
        }
    }


    private fun onPreviousButtonClick() {
        val currentSongIndex = viewModel.currentSongIndex.value
        if (currentSongIndex != null) {
            val newIndex = if (currentSongIndex > 0) currentSongIndex - 1 else songs.size - 1
            viewModel.updateCurrentSongIndex(newIndex)
            playSong()
            updateSongInfo()
        }
    }

    private fun onNextButtonClick() {
        val currentSongIndex = viewModel.currentSongIndex.value
        if (currentSongIndex != null) {
            val newIndex = if (currentSongIndex < songs.size - 1) currentSongIndex + 1 else 0
            Log.d("DetailActivityController", "newIndex: $newIndex")
            viewModel.updateCurrentSongIndex(newIndex)
            playSong()
            updateSongInfo()
        }
    }


    private fun onPlayPauseButtonClick() {
        MediaPlayerHolder.mediaPlayer?.let { mediaPlayer ->
            if (mediaPlayer.isPlaying) {
                mediaPlayer.pause()
                view.playPauseButton.setImageResource(android.R.drawable.ic_media_play)
                handler.removeCallbacks(updateSeekBar)
            } else {
                mediaPlayer.start()
                view.playPauseButton.setImageResource(android.R.drawable.ic_media_pause)
                handler.postDelayed(updateSeekBar, 1000)
            }
        }
    }

    private fun updateSongInfo() {
        val currentSongIndex = viewModel.currentSongIndex.value ?: return

        if (currentSongIndex < 0 || currentSongIndex >= songs.size) {
            return
        }
        val songTitles = songs.map { it.title }
        val albumArts = songs.map { it.albumArtUri }

        view.songTitleTextView.text = songTitles[currentSongIndex]
        Glide.with(fragment).load(albumArts[currentSongIndex]).into(view.albumArtImageView)
    }


    /**
     * Sets up the SeekBar change listener, which handles user interactions with the SeekBar.
     * When the user changes the SeekBar's progress, it updates the MediaPlayer's playback position accordingly.
     */
    fun setupSeekBarChangeListener() {
        view.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    MediaPlayerHolder.mediaPlayer?.seekTo(progress)
                    viewModel.updatePlaybackPosition(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    fun setupMotionLayoutTransitionListener() {
        val motionLayout = view.motionLayout
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

                    val currentSongIndex = viewModel.currentSongIndex.value ?: return
                    val songUri = songs[currentSongIndex].songUri

                    mediaPlayer.setDataSource(fragment.requireContext(), songUri)
                    mediaPlayer.prepare()
                    // Seek to the position before the transition
                    mediaPlayer.seekTo(playbackPositionBeforeTransition)
                    mediaPlayer.start()
                    view.seekBar.max = mediaPlayer.duration
                    handler.postDelayed(updateSeekBar, 1000)
                    view.playPauseButton.setImageResource(android.R.drawable.ic_media_pause)
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

}
