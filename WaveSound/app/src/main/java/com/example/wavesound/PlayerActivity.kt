package com.example.wavesound

import android.content.ComponentName
import android.content.DialogInterface
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
import android.os.IBinder
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.wavesound.databinding.ActivityPlayerBinding
import com.example.wavesound.ui.local.LocalAllSong
import com.example.wavesound.ui.local.LocalFavorite
import com.example.wavesound.ui.local.LocalPlayList
import com.example.wavesound.ui.local.MusicService
import com.example.wavesound.ui.local.PlayListDetails
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class PlayerActivity : AppCompatActivity(), ServiceConnection {

    companion object {
        lateinit var musicListPA : ArrayList<Music>
        var songPosition: Int = 0
        var isPlaying:Boolean = false
        var isShuffleEnabled = false
        var isRepeatEnabled = false
        var musicService: MusicService? = null
        @Suppress("StaticFieldLeak")
        lateinit var binding: ActivityPlayerBinding
        var min15 : Boolean = false
        var min30 : Boolean = false
        var min60 : Boolean = false
        var min90 : Boolean = false
        var min120 : Boolean = false
        var nowPlayingId: String = ""
        var isFavourite: Boolean = false
        var fIndex: Int = -1

        fun changeFavoriteIcon(){
            if (isFavourite) binding.favoriteBtnPA.setImageResource(R.drawable.baseline_favorite_24)
            else binding.favoriteBtnPA.setImageResource(R.drawable.baseline_favorite_border_24)
        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        enableEdgeToEdge()
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializar la interfaz de usuario
        initializeLayout()
        binding.backBtnPA.setOnClickListener {
            finish()
        }
        binding.songNamePA.isSelected = true
        binding.playPauseBtnPA.setOnClickListener {
            if (isPlaying) {
                pauseMusic()
            } else {
                playMusic()
            }
        }
        binding.previousBtnPA.setOnClickListener {
            prevNextSong(false)
        }
        binding.nextBtnPA.setOnClickListener {
            prevNextSong(true)
        }

        binding.seekBarPA.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    musicService!!.mediaPlayer!!.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit

            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
        })
        binding.timerBtnPA.setOnClickListener {
            val timer = min15 || min30 || min60 || min90 || min120
            if (!timer) showBottonSheetDialog()
            else {
                val builder = MaterialAlertDialogBuilder(this)
                builder.setTitle("Stop Timer")
                    .setMessage("Do you want to stop the timer?")
                    .setPositiveButton("Yes") { _, _ ->
                        min15 = false
                        min30 = false
                        min60 = false
                        min90 = false
                        min120 = false
                    }
                    .setNegativeButton("No") { dialog, _ ->
                        dialog.dismiss()
                    }
                val customDialog = builder.create()
                customDialog.show()
                customDialog.getButton(DialogInterface.BUTTON_POSITIVE)?.setTextColor(Color.WHITE)
                customDialog.getButton(DialogInterface.BUTTON_NEGATIVE)?.setTextColor(Color.WHITE)
                binding.timerBtnPA.setImageResource(R.drawable.baseline_query_builder_24)

            }
        }
        binding.repeatSongBtnPA.setOnClickListener {
            if (musicService!!.mediaPlayer!!.isLooping) {
                musicService!!.mediaPlayer!!.isLooping = false
                isRepeatEnabled = false
                binding.repeatSongBtnPA.setImageResource(R.drawable.baseline_repeat_24)
            } else {
                musicService!!.mediaPlayer!!.isLooping = true
                isRepeatEnabled = true
                binding.repeatSongBtnPA.setImageResource(R.drawable.baseline_repeat_one_24)
            }
        }
        binding.shuffleBtnPA.setOnClickListener {
            isShuffleEnabled = !isShuffleEnabled

            // Actualizar la apariencia del botón según el estado de la reproducción aleatoria
            val shuffleButtonIcon = if (isShuffleEnabled) {
                // Si la reproducción aleatoria está activada, establecer el icono correspondiente
                R.drawable.baseline_shuffle_24
            } else {
                // Si la reproducción aleatoria está desactivada, establecer el icono correspondiente
                R.drawable.baseline_reorder_24
            }
            binding.shuffleBtnPA.setImageResource(shuffleButtonIcon)
        }

        binding.favoriteBtnPA.setOnClickListener {
            fIndex = favoriteChecker(musicListPA[songPosition].id)
            if (isFavourite) {
                isFavourite = false
                binding.favoriteBtnPA.setImageResource(R.drawable.baseline_favorite_border_24)
                LocalFavorite.favoriteSongs.removeAt(fIndex)
            } else {
                isFavourite = true
                binding.favoriteBtnPA.setImageResource(R.drawable.baseline_favorite_24)
                LocalFavorite.favoriteSongs.add(musicListPA[songPosition])
            }
            NowPlaying.changeFavoriteIcon()
            LocalFavorite.favoritesChanged = true
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    // Actualizar la interfaz de usuario con la información de la canción actual
    private fun setLayout(){
        fIndex = favoriteChecker(musicListPA[songPosition].id)

        Glide.with(this)
            .load(musicListPA[songPosition].artUri)
            .apply (RequestOptions().placeholder(R.drawable.logob).centerCrop())
            .into(binding.songImgPA)
        binding.songNamePA.text = musicListPA[songPosition].title
        if (min15 || min30 || min60 || min90 || min120) binding.timerBtnPA.setImageResource(R.drawable.baseline_av_timer_24)
        else binding.timerBtnPA.setImageResource(R.drawable.baseline_query_builder_24)
        if (isShuffleEnabled)
            binding.shuffleBtnPA.setImageResource(R.drawable.baseline_shuffle_24)
        else
            binding.shuffleBtnPA.setImageResource(R.drawable.baseline_reorder_24)

        binding.repeatSongBtnPA.setImageResource(R.drawable.baseline_repeat_24)
        if (isFavourite) binding.favoriteBtnPA.setImageResource(R.drawable.baseline_favorite_24)
        else binding.favoriteBtnPA.setImageResource(R.drawable.baseline_favorite_border_24)
    }

    // Crear un reproductor de medios para la canción actual
    private fun createMediaPlayer() {
        try {
            if (musicService!!.mediaPlayer == null) {
                musicService!!.mediaPlayer = MediaPlayer()
            }
            musicService!!.mediaPlayer!!.reset()
            musicService!!.mediaPlayer!!.setDataSource(musicListPA[songPosition].path)
            musicService!!.mediaPlayer!!.prepare()
            musicService!!.mediaPlayer!!.setOnCompletionListener {
                // Reproducir automáticamente la siguiente canción al finalizar la actual
                prevNextSong(true)
            }
            musicService!!.mediaPlayer!!.start()
            isPlaying = true
            binding.playPauseBtnPA.setImageResource(R.drawable.baseline_pause_24)
            musicService!!.showNotification(R.drawable.baseline_pause_24)
            binding.songDurationPA.text = formatDuration(musicService!!.mediaPlayer!!.currentPosition.toLong())
            binding.seekBarPA.progress = musicService!!.mediaPlayer!!.currentPosition
            binding.seekBarPA.max = musicService!!.mediaPlayer!!.duration
            musicService!!.seekBarSetUp()
        } catch (e: Exception) {
            return
        }
    }

    // Inicializar la interfaz de usuario
    private fun initializeLayout(){
        songPosition = intent.getIntExtra("index", 0)
        when(intent.getStringExtra("class")){
            "FavoriteAdapter" -> {
                // Iniciar el servicio de música
                val intent = Intent(this, MusicService::class.java)
                bindService(intent, this, BIND_AUTO_CREATE)
                startService(intent)
                musicListPA = ArrayList()
                musicListPA.addAll(LocalFavorite.favoriteSongs) // Utilizar la lista de favoritos aquí
                setLayout()
            }
            "MusicAdapterSearch" -> {
                // Iniciar el servicio de música
                val intent = Intent(this, MusicService::class.java)
                bindService(intent, this, BIND_AUTO_CREATE)
                startService(intent)
                musicListPA = ArrayList()
                musicListPA.addAll(LocalAllSong.musicListSearch)
                setLayout()
            }
            "NowPlaying" -> {
                setLayout()
                binding.songDurationPA.text = formatDuration(musicService!!.mediaPlayer!!.currentPosition.toLong())
                binding.seekBarPA.progress = musicService!!.mediaPlayer!!.currentPosition
                binding.seekBarPA.max = musicService!!.mediaPlayer!!.duration
                if (isPlaying) {
                    binding.playPauseBtnPA.setImageResource(R.drawable.baseline_pause_24)
                } else {
                    binding.playPauseBtnPA.setImageResource(R.drawable.baseline_play_arrow_24)
                }
            }
            "MusicAdapter" -> {
                // Iniciar el servicio de música
                val intent = Intent(this, MusicService::class.java)
                bindService(intent, this, BIND_AUTO_CREATE)
                startService(intent)
                musicListPA = ArrayList()
                musicListPA.addAll(LocalAllSong.MusicListMA)
                setLayout()
            }
            "LocalAllSong" -> {
                // Iniciar el servicio de música
                val intent = Intent(this, MusicService::class.java)
                bindService(intent, this, BIND_AUTO_CREATE)
                startService(intent)
                musicListPA = ArrayList()
                musicListPA.addAll(LocalAllSong.MusicListMA)
                setLayout()
            }
            "PlaylistDetailsAdapter" -> {
                // Iniciar el servicio de música
                val intent = Intent(this, MusicService::class.java)
                bindService(intent, this, BIND_AUTO_CREATE)
                startService(intent)
                musicListPA = ArrayList()
                musicListPA.addAll(LocalPlayList.musicPlaylist.ref[PlayListDetails.currentPlaylistPos].playlist)
                setLayout()
            }
        }
    }
    private fun playMusic(){
        binding.playPauseBtnPA.setImageResource(R.drawable.baseline_pause_24)
        musicService!!.showNotification(R.drawable.baseline_pause_24)
        isPlaying = true
        musicService!!.mediaPlayer!!.start()

    }

    private fun pauseMusic(){
        binding.playPauseBtnPA.setImageResource(R.drawable.baseline_play_arrow_24)
        musicService!!.showNotification(R.drawable.baseline_play_arrow_24)
        isPlaying = false
        musicService!!.mediaPlayer!!.pause()
    }

    private fun prevNextSong(increment: Boolean) {
        if (isShuffleEnabled) {
            // Si la reproducción aleatoria está activada, mezclar la lista de reproducción
            musicListPA.shuffle()
            // Luego, establecer el diseño y crear el reproductor de medios
            setLayout()
            createMediaPlayer()
        } else {
            // Si no, avanzar o retroceder en la lista de canciones según corresponda
            if (increment) {
                setSongPosition(true)
            } else {
                setSongPosition(false)
            }
            // Luego, establecer el diseño y crear el reproductor de medios
            setLayout()
            createMediaPlayer()
        }
    }


    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        val binder = service as MusicService.MyBinder
        musicService = binder.currentService()
        createMediaPlayer()
        musicService!!.seekBarSetUp()

    }

    override fun onServiceDisconnected(name: ComponentName?) {
        musicService = null
    }

    private fun showBottonSheetDialog() {
        val dialog = BottomSheetDialog(this@PlayerActivity)
        dialog.setContentView(R.layout.bottom_sheet_dialog)
        dialog.show()
        dialog.findViewById<LinearLayout>(R.id.min_15)?.setOnClickListener(){
            Toast.makeText(baseContext, "Music will stop after 15 minutes", Toast.LENGTH_SHORT).show()
            binding.timerBtnPA.setImageResource(R.drawable.baseline_av_timer_24)
            min15 = true
            Thread{Thread.sleep((15 * 60000).toLong())
            if(min15) exitApplication()}.start()
            dialog.dismiss()
        }
        dialog.findViewById<LinearLayout>(R.id.min_30)?.setOnClickListener(){
            Toast.makeText(baseContext, "Music will stop after 30 minutes", Toast.LENGTH_SHORT).show()
            binding.timerBtnPA.setImageResource(R.drawable.baseline_av_timer_24)
            min30 = true
            Thread{Thread.sleep((30 * 60000).toLong())
                if(min30) exitApplication()}.start()
            dialog.dismiss()
        }
        dialog.findViewById<LinearLayout>(R.id.min_60)?.setOnClickListener(){
            Toast.makeText(baseContext, "Music will stop after 60 minutes", Toast.LENGTH_SHORT).show()
            binding.timerBtnPA.setImageResource(R.drawable.baseline_av_timer_24)
            min60 = true
            Thread{Thread.sleep((60 * 60000).toLong())
                if(min60) exitApplication()}.start()
            dialog.dismiss()
        }
        dialog.findViewById<LinearLayout>(R.id.min_90)?.setOnClickListener(){
            Toast.makeText(baseContext, "Music will stop after 90 minutes", Toast.LENGTH_SHORT).show()
            binding.timerBtnPA.setImageResource(R.drawable.baseline_av_timer_24)
            min90 = true
            Thread{Thread.sleep((90 * 60000).toLong())
                if(min90) exitApplication()}.start()
            dialog.dismiss()
        }
        dialog.findViewById<LinearLayout>(R.id.min_120)?.setOnClickListener(){
            Toast.makeText(baseContext, "Music will stop after 120 minutes", Toast.LENGTH_SHORT).show()
            binding.timerBtnPA.setImageResource(R.drawable.baseline_av_timer_24)
            min120 = true
            Thread{Thread.sleep((120 * 60000).toLong())
                if(min120) exitApplication()}.start()
            dialog.dismiss()
        }
    }

}