package com.example.wavesound.ui.local

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.wavesound.Music
import com.example.wavesound.R
import com.example.wavesound.databinding.AddSongDialogBinding
import com.example.wavesound.databinding.FragmentLocalAllSongBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

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

        binding.addSongBtn.setOnClickListener {
            customAlertDialog()
        }
        binding.searchBtn.setOnClickListener {

            val searchText = binding.searchInput.text.toString().trim()
            searchSongs(searchText)

        }
    }

    private fun customAlertDialog(){
        val customDialog = LayoutInflater.from(requireContext()).inflate(R.layout.add_song_dialog, binding.root, false)
        val binder = AddSongDialogBinding.bind(customDialog)
        val builder = MaterialAlertDialogBuilder(requireContext())
        val dialog = builder.setView(customDialog)
            .setTitle("Add Song")
            .setPositiveButton("ADD"){ dialog, _ ->
                val songURL = binder.songURL.text
                Log.d("Song URL1", songURL.toString())
                if(songURL != null)
                    if(songURL.isNotEmpty()) {
                        /*
                        val destinationPath = "/storage/emulated/0/Download/archivo.mp3"

                        Log.d("Song URL2", songURL.toString())
                        val mp3Downloader = Mp3Downloader()
                        mp3Downloader.downloadMp3FromUrl(songURL.toString(), destinationPath)

                        val task = DownloadAndConvertTask(requireContext())
                        task.execute(songURL.toString())
                        */

                    }
                dialog.dismiss()
            }.create()
        dialog.show()

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

    private inner class DownloadAndConvertTask(private val context: Context) : AsyncTask<String, Void, Boolean>() {

        override fun doInBackground(vararg urls: String): Boolean {
            val youtubeUrl = urls[0]
            val videoPath = downloadVideo(youtubeUrl)
            if (videoPath != null) {
                val mp3Path = convertToMP3(videoPath)
                return mp3Path != null
            }
            return false
        }

        private fun downloadVideo(youtubeUrl: String): String? {
            var videoPath: String? = null
            try {
                val url = URL(youtubeUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.connect()
                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val inputStream = BufferedInputStream(connection.inputStream)
                    val file = File(context.filesDir, "video.mp4") // Guardar en el directorio de almacenamiento interno de la aplicación
                    val outputStream = FileOutputStream(file)
                    val buffer = ByteArray(1024)
                    var bytesRead: Int
                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                    }
                    outputStream.close()
                    inputStream.close()
                    videoPath = file.absolutePath
                }
                Log.d("DownloadAndConvertTask", "Video descargado.")
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return videoPath
        }

        private fun convertToMP3(videoPath: String): String? {
            var mp3Path: String? = null
            try {
                val videoFile = File(videoPath)
                val mp3File = File(context.filesDir, "audio.mp3") // Guardar en el directorio de almacenamiento interno de la aplicación
                // Ejemplo: Convertir el video a mp3 usando MobileFFmpeg
                val command = arrayOf(
                    "-i", videoFile.absolutePath,
                    "-vn",
                    "-acodec", "libmp3lame",
                    "-ar", "44100",
                    "-ac", "2",
                    "-b:a", "192k",
                    mp3File.absolutePath
                )
                val result = com.arthenica.mobileffmpeg.FFmpeg.execute(command)
                    mp3Path = mp3File.absolutePath
                Log.d("DownloadAndConvertTask", "Video convertido a MP3.")
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return mp3Path
        }



        override fun onPostExecute(result: Boolean) {
            super.onPostExecute(result)
            // Aquí puedes manejar el resultado de la tarea, como mostrar un mensaje de éxito o error
            if (result) {
                Log.d("DownloadAndConvertTask", "Video descargado y convertido a MP3.")
            } else {
                Log.e("DownloadAndConvertTask", "Error al descargar y convertir el video a MP3.")
            }
        }
    }

    class Mp3Downloader {

        fun downloadMp3FromUrl(url: String, destinationPath: String) {
            val client = OkHttpClient()

            val request = Request.Builder()
                .url(url)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    // Manejar errores
                }

                override fun onResponse(call: Call, response: Response) {
                    if (response.isSuccessful) {
                        val inputStream = response.body?.byteStream()
                        val outputFile = File(destinationPath)

                        FileOutputStream(outputFile).use { output ->
                            inputStream?.copyTo(output)
                        }
                    } else {
                        // Manejar respuestas no exitosas
                    }
                }
            })
        }
    }
}