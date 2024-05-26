package com.example.wavesound.ui.local

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.wavesound.Playlist
import com.example.wavesound.R
import com.example.wavesound.databinding.PlaylistViewBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

// Clase que se encarga de manejar la vista de las playlist
class PlaylistViewAdapter (private val context: Context, private var playlistList: ArrayList<Playlist>) : RecyclerView.Adapter<PlaylistViewAdapter.MyHolder>() {

    // Clase interna para el ViewHolder
    class MyHolder(binding: PlaylistViewBinding) : RecyclerView.ViewHolder(binding.root) {
        val image = binding.playlistImg
        val name = binding.playlistName
        val root = binding.root
        val delete = binding.playlistDeleteBtn
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        return MyHolder(PlaylistViewBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    // Se llama cuando se desplaza la lista
    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        holder.name.text = playlistList[position].name
        holder.name.isSelected = true
        holder.delete.setOnClickListener {
            val builder = MaterialAlertDialogBuilder(context)
            builder.setTitle(playlistList[position].name)
                .setMessage("Do you want to delete playlist?")
                .setPositiveButton("Yes"){ dialog, _ ->
                    LocalPlayList.musicPlaylist.ref.removeAt(position)
                    refreshPlaylist()
                    dialog.dismiss()
                }
                .setNegativeButton("No"){dialog, _ ->
                    dialog.dismiss()
                }
            val customDialog = builder.create()
            customDialog.show()
            customDialog.getButton(DialogInterface.BUTTON_POSITIVE)?.setTextColor(Color.WHITE)
            customDialog.getButton(DialogInterface.BUTTON_NEGATIVE)?.setTextColor(Color.WHITE)
        }
        holder.root.setOnClickListener {
            val intent = Intent(context, PlayListDetails::class.java)
            intent.putExtra("index", position)
            ContextCompat.startActivity(context, intent, null)
            //Toast.makeText(context, "Playlist clicked", Toast.LENGTH_SHORT).show()
        }
        if (LocalPlayList.musicPlaylist.ref[position].playlist.size > 0){
            Glide.with(context)
                .load(LocalPlayList.musicPlaylist.ref[position].playlist[0].artUri)
                .apply(RequestOptions().placeholder(R.drawable.logob).centerCrop())
                .into(holder.image)
        }
    }

    // Devuelve el numero de elementos de la lista
    override fun getItemCount(): Int {
       return playlistList.size
    }

    // Actualiza la lista de playlist
    fun refreshPlaylist(){
        playlistList = ArrayList()
        playlistList.addAll(LocalPlayList.musicPlaylist.ref)
        notifyDataSetChanged()
    }
}