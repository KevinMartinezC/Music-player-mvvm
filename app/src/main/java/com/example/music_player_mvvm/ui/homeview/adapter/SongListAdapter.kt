package com.example.music_player_mvvm.ui.homeview.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.music_player_mvvm.R
import com.example.music_player_mvvm.model.Song


@Suppress("DEPRECATION")
class SongListAdapter(
    private val songs: List<Song>,
    private val onSongClickListener: (Int) -> Unit
) : RecyclerView.Adapter<SongListAdapter.SongViewHolder>() {

    inner class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.songTitleTextView)
        val image: ImageView = itemView.findViewById(R.id.albumArtImageView)

        init {
            itemView.setOnClickListener {
                onSongClickListener(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.song_list_item_home, parent, false
        )
        return SongViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = songs[position]
        holder.title.text = song.title
        Glide.with(holder.image.context).load(song.albumArtUri).into(holder.image)
    }

    override fun getItemCount(): Int {
        return songs.size

    }
}
