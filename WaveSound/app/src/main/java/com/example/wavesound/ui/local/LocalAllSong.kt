package com.example.wavesound.ui.local

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.wavesound.Music
import com.example.wavesound.databinding.FragmentLocalAllSongBinding
import java.io.File

class LocalAllSong : Fragment() {

    private lateinit var binding: FragmentLocalAllSongBinding
    private lateinit var musicAdapter: MusicAdapter

    companion object {

        var MusicListMA: ArrayList<Music> = ArrayList()
        private var originalMusicList: ArrayList<Music> = ArrayList()
        lateinit var musicListSearch : ArrayList<Music>
        var search: Boolean = false
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentLocalAllSongBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        originalMusicList = getAllAdioFiles()
        initializeLayout()


        binding.searchBtn.setOnClickListener {

            val searchText = binding.searchInput.text.toString().trim()
            searchSongs(searchText)

        }
    }

    private fun initializeLayout() {
        search = false
        MusicListMA = getAllAdioFiles()
        binding.localAllSongRV.setHasFixedSize(true)
        binding.localAllSongRV.setItemViewCacheSize(13)
        binding.localAllSongRV.layoutManager = LinearLayoutManager(requireContext())
        musicAdapter = MusicAdapter(requireContext(), MusicListMA)
        binding.localAllSongRV.adapter = musicAdapter
        binding.totalSongs.text = "Total Songs: "+ musicAdapter.itemCount.toString()
    }

    private fun searchSongs(songTxt: String) {
        if (songTxt.isNotEmpty()) {
            musicListSearch = ArrayList()
           for (song in MusicListMA) {
               if (song.title.contains(songTxt, ignoreCase = true) ||
                   song.album.contains(songTxt, ignoreCase = true) ||
                   song.artist.contains(songTxt, ignoreCase = true)) {
                   musicListSearch.add(song)
               }
           }
            search = true
            musicAdapter.updateMusicList(searchList = musicListSearch)
            binding.totalSongs.text = "Total Songs: ${musicAdapter.itemCount}"
        } else {
            search = false
            MusicListMA = originalMusicList
            musicAdapter.setData(MusicListMA)
            binding.totalSongs.text = "Total Songs: ${musicAdapter.itemCount}"

        }
    }


    @SuppressLint("Range")
    private fun getAllAdioFiles(): ArrayList<Music>{
        val tempList = arrayListOf<Music>()
        val selection = MediaStore.Audio.Media.IS_MUSIC + " != 0"
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ALBUM_ID
        )
        val cursor = requireActivity().contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            null,
            null
        )
        if (cursor != null) {
            if(cursor.moveToFirst()){
                do {
                    val titleC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE))?:"Unknown"
                    val idC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID))?:"Unknown"
                    val albumC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM))?:"Unknown"
                    val artistC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST))?:"Unknown"
                    val pathC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA))
                    val durationC = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION))
                    val albumIdC = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)).toString()
                    val uri = Uri.parse("content://media/external/audio/albumart")
                    val artUriC = Uri.withAppendedPath(uri, albumIdC).toString()
                    val music = Music(id = idC, title = titleC, album = albumC, artist = artistC, path = pathC, duration = durationC,
                        artUri = artUriC)
                    val file = File(music.path)
                    if(file.exists())
                        tempList.add(music)
                }while (cursor.moveToNext())
            }
            cursor.close()
        }

        return tempList
    }


}