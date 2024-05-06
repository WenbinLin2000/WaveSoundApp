package com.example.wavesound

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.wavesound.databinding.MusicViewBinding

class MusicAdapter(private val context: Context, private val musicList: ArrayList<Music>) : RecyclerView.Adapter<MusicAdapter.MyViewHolder>() {
    class MyViewHolder(binding: MusicViewBinding) : RecyclerView.ViewHolder(binding.root) {
        val title = binding.songNameMV
        val album = binding.songAlbumMV
        val image = binding.imageMV
        val duration = binding.songDuration
        val root = binding.root
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

        holder.root.setOnClickListener {
            val intent = Intent(context, PlayerActivity::class.java)
            ContextCompat.startActivity(context, intent, null)
        }

    }

    override fun getItemCount(): Int {
        return musicList.size
    }
}