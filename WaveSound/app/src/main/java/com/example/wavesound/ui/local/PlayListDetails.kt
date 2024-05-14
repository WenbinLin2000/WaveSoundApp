package com.example.wavesound.ui.local

import android.content.Intent
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

class PlayListDetails : AppCompatActivity() {

    lateinit var binding: ActivityPlayListDetailsBinding
    lateinit var adapter: MusicAdapter

    companion object {
        var currentPlaylistPos: Int = -1
    }

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
        //LocalPlayList.musicPlaylist.ref[currentPlaylistPos].playlist.addAll(LocalAllSong.MusicListMA)
        adapter = MusicAdapter(this, LocalPlayList.musicPlaylist.ref[currentPlaylistPos].playlist, playlistDetails = true)
        binding.playlistDetailsRV.adapter = adapter

        binding.backBtnPD.setOnClickListener { finish() }
        binding.addBtnPD.setOnClickListener {
            startActivity(Intent(this, SelectionActivity::class.java))

        }
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
        }


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

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