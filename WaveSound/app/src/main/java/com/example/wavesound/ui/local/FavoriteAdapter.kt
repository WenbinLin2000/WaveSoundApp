package com.example.wavesound.ui.local

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
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

// Adaptador para la lista de canciones favoritas
class FavoriteAdapter(private val context: Context, private var musicList: ArrayList<Music>) : RecyclerView.Adapter<FavoriteAdapter.MyViewHolder>() {

    // Clase que define los elementos de la vista de la canción
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
        //Carga de la información de la canción en el holder
        holder.title.text = musicList[position].title
        holder.album.text = musicList[position].album
        holder.duration.text = formatDuration(musicList[position].duration)
        Glide.with(context)
            .load(musicList[position].artUri)
            .apply (RequestOptions().placeholder(R.drawable.logob).centerCrop())
            .into(holder.image)

        // Al hacer click en la canción, se abre la actividad de reproducción
        holder.root.setOnClickListener {
            val intent = Intent(context, PlayerActivity::class.java)
            intent.putExtra("index", position)
            intent.putExtra("class", "FavoriteAdapter")
            ContextCompat.startActivity(context, intent, null)
        }

    }

    // Devuelve la cantidad de canciones en la lista
    override fun getItemCount(): Int {
        return musicList.size
    }

    // Actualiza la lista de canciones favoritas
    @SuppressLint("NotifyDataSetChanged")
    fun updateFavorites(newList: ArrayList<Music>){
        musicList = ArrayList()
        musicList.addAll(newList)
        notifyDataSetChanged()
    }
}