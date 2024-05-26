package com.example.wavesound.ui.local

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.wavesound.Music
import com.example.wavesound.R
import com.example.wavesound.databinding.AddSongDialogBinding
import com.example.wavesound.databinding.FragmentLocalAllSongBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

// Fragmento para mostrar todas las canciones locales
class LocalAllSong : Fragment() {
    companion object {
        var MusicListMA: ArrayList<Music> = ArrayList()
        private var originalMusicList: ArrayList<Music> = ArrayList()
        lateinit var musicListSearch : ArrayList<Music>
        var search: Boolean = false
    }

    private lateinit var binding: FragmentLocalAllSongBinding
    private lateinit var musicAdapter: MusicAdapter
    private lateinit var DownloadName: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLocalAllSongBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Se ejecuta cuando la vista ha sido creada
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        originalMusicList = getAllAdioFiles()
        initializeLayout()

        binding.addSongBtn.setOnClickListener {
            customAlertDialog()
        }
        binding.searchBtn.setOnClickListener {

            val searchText = binding.searchInput.text.toString().trim()
            searchSongs(searchText)
        }
    }

    // Funcion para inicializar la vista y rellenarla con las canciones
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

    // Funcion para obtener todas las canciones de la memoria interna
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

    // Funcion para buscar canciones
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
            musicAdapter.updateMusicList(MusicListMA)
            binding.totalSongs.text = "Total Songs: ${musicAdapter.itemCount}"

        }
    }

    // Funcion para mostrar un dialogo personalizado
    private fun customAlertDialog(){
        val customDialog = LayoutInflater.from(requireContext()).inflate(R.layout.add_song_dialog, binding.root, false)
        val binder = AddSongDialogBinding.bind(customDialog)
        val builder = MaterialAlertDialogBuilder(requireContext())
        val dialog = builder.setView(customDialog)
            .setTitle("Add Song")
            .setPositiveButton("ADD"){ dialog, _ ->
                val songURL = binder.songURL.text
                val songName = binder.songName.text
                DownloadName = songName.toString()
                Log.d("Song URL1", songURL.toString())
                if(songURL != null)
                    if(songURL.isNotEmpty()) {
                        convertAndDownloadAudio(songURL.toString(), songName.toString())

                    }
                dialog.dismiss()
            }.create()
        dialog.show()
        dialog.getButton(DialogInterface.BUTTON_POSITIVE)?.setTextColor(Color.WHITE)
    }

    // Funcion para convertir y descargar el audio
    private fun convertAndDownloadAudio(youtubeUrl: String, audioName: String) {
        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        val json = JSONObject().apply {
            put("url", youtubeUrl)
            put("audioName", audioName)
        }
        val requestBody = json.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

        val request = Request.Builder()
            .url("http://192.168.1.105:3000/downloadAudioLocal") // Actualiza IP del servidor
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                Log.e("NetworkError", "Error al contactar con el servidor: ${e.message}", e)
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Error al contactar con el servidor", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                response.use {
                    if (it.isSuccessful) {
                        val responseJson = JSONObject(it.body?.string() ?: "")
                        val downloadUrl = responseJson.getString("path")
                        downloadMp3FromUrl(requireContext(), downloadUrl)
                    } else {
                        Log.e("ServerError", "Fallo al convertir el video: ${response.message}")
                        requireActivity().runOnUiThread {
                            Toast.makeText(requireContext(), "Fallo al convertir el video", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        })
    }

    //Funcion para descargar el archivo mp3
    private fun downloadMp3FromUrl(context: Context, url: String) {
        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle("Descargando MP3")
            .setDescription("Descargando tu archivo")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_MUSIC, (DownloadName+".mp3"))

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)
        requireActivity().runOnUiThread {
            Toast.makeText(context, "Descarga iniciada", Toast.LENGTH_SHORT).show()
        }
    }
}