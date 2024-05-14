package com.example.wavesound.ui.local

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.wavesound.MusicPlaylist
import com.example.wavesound.Playlist
import com.example.wavesound.R
import com.example.wavesound.databinding.AddPlaylistDialogBinding
import com.example.wavesound.databinding.FragmentLocalPlayListBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.SimpleDateFormat
import java.util.Locale

class LocalPlayList : Fragment() {

    companion object {
        var musicPlaylist: MusicPlaylist = MusicPlaylist()
    }

    private lateinit var binding: FragmentLocalPlayListBinding
    private lateinit var adapter: PlaylistViewAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreate(savedInstanceState)
        binding = FragmentLocalPlayListBinding.inflate(inflater, container, false)
        binding.localPlaylistRV.setHasFixedSize(true)
        binding.localPlaylistRV.setItemViewCacheSize(13)
        binding.localPlaylistRV.layoutManager = GridLayoutManager(requireContext(),2)
        adapter = PlaylistViewAdapter(requireContext(), playlistList =  musicPlaylist.ref)
        binding.localPlaylistRV.adapter = adapter
        binding.addPlaylistBtn.setOnClickListener {
            customAlertDialog()
        }

        return binding.root
    }

    private fun customAlertDialog(){
        val customDialog = LayoutInflater.from(requireContext()).inflate(R.layout.add_playlist_dialog, binding.root, false)
        val binder = AddPlaylistDialogBinding.bind(customDialog)
        val builder = MaterialAlertDialogBuilder(requireContext())
        val dialog = builder.setView(customDialog)
            .setTitle("Playlist Details")
            .setPositiveButton("ADD"){ dialog, _ ->
                val playlistName = binder.playlistName.text
                val createdBy = binder.yourName.text
                if(playlistName != null && createdBy != null)
                    if(playlistName.isNotEmpty() && createdBy.isNotEmpty())
                    {
                        addPlaylist(playlistName.toString(), createdBy.toString())
                    }
                dialog.dismiss()
            }.create()
        dialog.show()

    }

    private fun addPlaylist(name: String, createdBy: String){
        var playlistExists = false
        for(i in musicPlaylist.ref) {
            if (name == i.name){
                playlistExists = true
                break
            }
        }
        if(!playlistExists){
           val tempPlaylist = Playlist()
            tempPlaylist.name = name
            tempPlaylist.playlist = ArrayList()
            tempPlaylist.createdBy = createdBy
            val calendar = java.util.Calendar.getInstance().time
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
            tempPlaylist.createdOn = sdf.format(calendar)
            musicPlaylist.ref.add(tempPlaylist)
            adapter.refreshPlaylist()
        }
    }

    override fun onResume() {
        super.onResume()
        adapter.notifyDataSetChanged()
    }
}