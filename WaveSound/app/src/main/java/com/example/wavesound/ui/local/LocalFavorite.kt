package com.example.wavesound.ui.local

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.wavesound.Music
import com.example.wavesound.checkPlaylist
import com.example.wavesound.databinding.FragmentLocalFavoriteBinding


class LocalFavorite : Fragment() {
    companion object {
        var favoriteSongs: ArrayList<Music> = ArrayList()
        var favoritesChanged: Boolean = false
    }
    private lateinit var binding: FragmentLocalFavoriteBinding
    private lateinit var adapter: FavoriteAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreate(savedInstanceState)
        favoriteSongs = checkPlaylist(favoriteSongs)
        binding = FragmentLocalFavoriteBinding.inflate(inflater, container, false)
        binding.localFavoriteSongRV.setHasFixedSize(true)
        binding.localFavoriteSongRV.setItemViewCacheSize(13)
        binding.localFavoriteSongRV.layoutManager = LinearLayoutManager(requireContext())
        adapter = FavoriteAdapter(requireContext(), favoriteSongs)
        binding.localFavoriteSongRV.adapter = adapter

        favoritesChanged = false



        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()
        if(favoritesChanged) {
            adapter.updateFavorites(favoriteSongs)
            favoritesChanged = false
        }
    }

}