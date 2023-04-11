package com.example.music_player_mvvm.ui.settingview.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.music_player_mvvm.databinding.SongListItemBinding
import com.example.music_player_mvvm.model.Song

@Suppress("DEPRECATION")
class SettingSongListAdapter(
    private val songs: MutableList<Song>,
    private val onSongClickListener: (Int) -> Unit,
    private val onDeleteClickListener: (Int) -> Unit
) : RecyclerView.Adapter<SettingSongListAdapter.SettingSongViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SettingSongViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = SongListItemBinding.inflate(inflater, parent, false)
        return SettingSongViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SettingSongViewHolder, position: Int) {
        holder.bind(songs[position], onSongClickListener, onDeleteClickListener)
    }

    override fun getItemCount(): Int {
        return songs.size
    }

    inner class SettingSongViewHolder(private val binding: SongListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(
            song: Song,
            onSongClickListener: (Int) -> Unit,
            onDeleteClickListener: (Int) -> Unit
        ) {
            binding.songTitleTextView.text = song.title

            // Update the item UI based on the song's selected state
            binding.root.setBackgroundColor(
                if (song.selected) Color.parseColor("#E0E0E0")
                else Color.TRANSPARENT
            )

            // Load album art into the ImageView
            Glide.with(binding.albumArtImageView.context)
                .load(song.albumArtUri)
                .into(binding.albumArtImageView)

            // Set click listener for the whole item
            itemView.setOnClickListener {
                onSongClickListener(adapterPosition)
            }

            // Set click listener for the delete button
            binding.deleteButton.setOnClickListener {
                onDeleteClickListener(adapterPosition)
            }
        }
    }
}
