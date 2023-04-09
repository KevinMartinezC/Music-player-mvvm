package com.example.music_player_mvvm.views

import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.music_player_mvvm.model.media.MediaPlayerHolder
import com.example.music_player_mvvm.R
import com.example.music_player_mvvm.model.Song
import com.example.music_player_mvvm.model.SongContract
import com.example.music_player_mvvm.views.adapter.SongListAdapter
import com.example.music_player_mvvm.model.SongRepository
import com.example.music_player_mvvm.databinding.FragmentHomeScreenBinding


class HomeScreenFragment : Fragment() {
    private var currentSongIndex: Int = 0
    private lateinit var recyclerView: RecyclerView
    private lateinit var songs: List<Song>
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

        songs = loadSongsFromProvider()
        SongRepository.songs = songs
        initViews()
        setupRecyclerView()
    }

    private fun initViews() {
        recyclerView = binding.recyclerView
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

    private fun loadSongsFromProvider(): List<Song> {
        val songs = mutableListOf<Song>()
        val projection = arrayOf(
            SongContract.Columns.ID,
            SongContract.Columns.SONG_NAME,
            SongContract.Columns.SONG_URI,
            SongContract.Columns.ALBUM_ART_URI
        )

        requireActivity().contentResolver.query(
            SongContract.CONTENT_URI,
            projection,
            null,
            null,
            null
        )
            ?.use { cursor ->
                if (cursor.moveToFirst()) {
                    do {
                        val titleIndex = cursor.getColumnIndex(SongContract.Columns.SONG_NAME)
                        val songUriIndex = cursor.getColumnIndex(SongContract.Columns.SONG_URI)
                        val albumArtUriIndex =
                            cursor.getColumnIndex(SongContract.Columns.ALBUM_ART_URI)

                        if (titleIndex >= 0 && songUriIndex >= 0 && albumArtUriIndex >= 0) {
                            val title = cursor.getString(titleIndex)
                            val songUri = Uri.parse(cursor.getString(songUriIndex))
                            val albumArtUri = Uri.parse(cursor.getString(albumArtUriIndex))

                            val song = Song(title, songUri, albumArtUri)
                            songs.add(song)
                        }
                    } while (cursor.moveToNext())
                }
            }
        return songs
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

