package com.example.wavesound.ui.local

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.wavesound.Music
import com.example.wavesound.PlayerActivity
import com.example.wavesound.R
import com.example.wavesound.databinding.MusicViewBinding
import com.example.wavesound.formatDuration

class MusicAdapter(private val context: Context, private val musicList: ArrayList<Music>,
                   private val playlistDetails: Boolean = false,
                    private val selectionActivity: Boolean = false)
    : RecyclerView.Adapter<MusicAdapter.MyViewHolder>() {
    class MyViewHolder(binding: MusicViewBinding) : RecyclerView.ViewHolder(binding.root) {
        val title = binding.songNameMV
        val album = binding.songAlbumMV
        val image = binding.imageMV
        val duration = binding.songDuration
        val root = binding.root
    }

    private fun sendIntent(ref: String, pos: Int){
        val intent = Intent(context, PlayerActivity::class.java)
        intent.putExtra("index", pos)
        intent.putExtra("class", ref)
        ContextCompat.startActivity(context, intent, null)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(MusicViewBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.title.text = musicList[position].title
        holder.album.text = musicList[position].album
        holder.duration.text = formatDuration(musicList[position].duration)
        Glide.with(context)
            .load(musicList[position].artUri)
            .apply (RequestOptions().placeholder(R.drawable.logob).centerCrop())
            .into(holder.image)

        when {
            playlistDetails -> holder.root.setOnClickListener {
                sendIntent(ref = "PlaylistDetailsAdapter", pos = position)
            }
            selectionActivity -> holder.root.setOnClickListener {
                if (addSong(musicList[position]))
                    holder.root.setBackgroundColor(ContextCompat.getColor(context, R.color.button))
                else
                    holder.root.setBackgroundColor(Color.TRANSPARENT)
            }
            else ->{
                holder.root.setOnClickListener {
                    when {
                        LocalAllSong.search -> sendIntent(ref = "MusicAdapterSearch", pos = position)
                        else -> {
                            sendIntent(ref = "MusicAdapter", pos = position)
                        }
                    }
                }
            }
        }


    }

    override fun getItemCount(): Int {
        return musicList.size
    }

    fun updateMusicList(searchList: ArrayList<Music>) {
        musicList.clear()
        musicList.addAll(searchList)
        notifyDataSetChanged()
    }

    fun setData(newMusicList: ArrayList<Music>) {
        musicList.clear()
        musicList.addAll(newMusicList)
        notifyDataSetChanged()
    }

    private fun addSong(song: Music): Boolean {
        LocalPlayList.musicPlaylist.ref[PlayListDetails.currentPlaylistPos].playlist.forEachIndexed { index, music ->
            if (song.id == music.id) {
                LocalPlayList.musicPlaylist.ref[PlayListDetails.currentPlaylistPos].playlist.removeAt(index)
                return false
            }
        }
        LocalPlayList.musicPlaylist.ref[PlayListDetails.currentPlaylistPos].playlist.add(song)
        return true
    }

    fun refreshPlaylist() {
        musicList.clear()
        musicList.addAll(LocalPlayList.musicPlaylist.ref[PlayListDetails.currentPlaylistPos].playlist)
        notifyDataSetChanged()
    }
}