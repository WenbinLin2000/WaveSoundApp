package com.example.wavesound.ui.local

import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.wavesound.R
import com.example.wavesound.checkPlaylist
import com.example.wavesound.databinding.ActivityPlayListDetailsBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.GsonBuilder

// Clase que muestra los detalles de una lista de reproduccion
class PlayListDetails : AppCompatActivity() {
    companion object {
        var currentPlaylistPos: Int = -1
    }

    lateinit var binding: ActivityPlayListDetailsBinding
    lateinit var adapter: MusicAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPlayListDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        currentPlaylistPos = intent.extras?.getInt("index") as Int

        try{LocalPlayList.musicPlaylist.ref[currentPlaylistPos].playlist =
            checkPlaylist(playlist = LocalPlayList.musicPlaylist.ref[currentPlaylistPos].playlist)}
        catch(e: Exception){}

        binding.playlistDetailsRV.setItemViewCacheSize(10)
        binding.playlistDetailsRV.setHasFixedSize(true)
        binding.playlistDetailsRV.layoutManager = LinearLayoutManager(this)
        adapter = MusicAdapter(this, LocalPlayList.musicPlaylist.ref[currentPlaylistPos].playlist, playlistDetails = true)
        binding.playlistDetailsRV.adapter = adapter

        binding.backBtnPD.setOnClickListener { finish() }

        // Agrega canciones a la lista de reproduccion
        binding.addBtnPD.setOnClickListener {
            startActivity(Intent(this, SelectionActivity::class.java))
        }

        // Elimina todas las canciones de la lista de reproduccion
        binding.removeAllPD.setOnClickListener {
            val builder = MaterialAlertDialogBuilder(this)
            builder.setTitle("Remove All")
                .setMessage("Do you want to remove all songs from this playlist?")
                .setPositiveButton("Yes") { dialog, _ ->
                    LocalPlayList.musicPlaylist.ref[currentPlaylistPos].playlist.clear()
                    adapter.refreshPlaylist()
                    dialog.dismiss()
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
            val customDialog = builder.create()
            customDialog.show()
            customDialog.getButton(DialogInterface.BUTTON_POSITIVE)?.setTextColor(Color.WHITE)
            customDialog.getButton(DialogInterface.BUTTON_NEGATIVE)?.setTextColor(Color.WHITE)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    // Actualiza la lista de reproduccion
    override fun onResume() {
        super.onResume()
        binding.playlistNamePD.text = LocalPlayList.musicPlaylist.ref[currentPlaylistPos].name
        binding.moreInfoPD.text = "Total Songs: ${adapter.itemCount}\n\n"+
                "Created By: \n${LocalPlayList.musicPlaylist.ref[currentPlaylistPos].createdBy}\n"+
                "Created On: \n${LocalPlayList.musicPlaylist.ref[currentPlaylistPos].createdOn}"
        if (adapter.itemCount > 0) {
            Glide.with(this)
                .load(LocalPlayList.musicPlaylist.ref[currentPlaylistPos].playlist[0].artUri)
                .apply (RequestOptions().placeholder(R.drawable.logob).centerCrop())
                .into(binding.playlistImgPD)
        }
        adapter.notifyDataSetChanged()
        val editor = getSharedPreferences("FAVORITES", MODE_PRIVATE).edit()
        val jsonStringPlaylist = GsonBuilder().create().toJson(LocalPlayList.musicPlaylist)
        editor.putString("MusicPlaylist", jsonStringPlaylist)
        editor.apply()
    }
}