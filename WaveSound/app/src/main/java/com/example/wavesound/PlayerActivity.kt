package com.example.wavesound

import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.wavesound.databinding.ActivityPlayerBinding
import com.example.wavesound.ui.local.LocalAllSong

class PlayerActivity : AppCompatActivity() {

    companion object {
        lateinit var musicListPA : ArrayList<Music>
        var songPosition: Int = 0
        var mediaPlayer: MediaPlayer? = null
        var isPlaying:Boolean = false
        var isShuffleEnabled = false


    }

    private lateinit var binding: ActivityPlayerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        enableEdgeToEdge()
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initializeLayout()
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
        binding.repeatSongBtnPA.setOnClickListener {
            if (mediaPlayer!!.isLooping) {
                mediaPlayer!!.isLooping = false
                binding.repeatSongBtnPA.setImageResource(R.drawable.baseline_repeat_24)
            } else {
                mediaPlayer!!.isLooping = true
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

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setLayout(){
        Glide.with(this)
            .load(musicListPA[songPosition].artUri)
            .apply (RequestOptions().placeholder(R.drawable.logob).centerCrop())
            .into(binding.songImgPA)
        binding.songNamePA.text = musicListPA[songPosition].title

    }

    private fun createMediaPlayer() {
        try {
            if (mediaPlayer == null) {
                mediaPlayer = MediaPlayer()
            }
            mediaPlayer!!.reset()
            mediaPlayer!!.setDataSource(musicListPA[songPosition].path)
            mediaPlayer!!.prepare()
            mediaPlayer!!.setOnCompletionListener {
                // Reproducir automáticamente la siguiente canción al finalizar la actual
                prevNextSong(true)
            }
            mediaPlayer!!.start()
            isPlaying = true
            binding.playPauseBtnPA.setImageResource(R.drawable.baseline_pause_24)
        } catch (e: Exception) {
            return
        }
    }

    private fun initializeLayout(){
        songPosition = intent.getIntExtra("index", 0)
        when(intent.getStringExtra("class")){
            "MusicAdapter" -> {
                musicListPA = ArrayList()
                musicListPA.addAll(LocalAllSong.MusicListMA)
                setLayout()
                createMediaPlayer()
            }
        }
    }
    private fun playMusic(){
        binding.playPauseBtnPA.setImageResource(R.drawable.baseline_pause_24)
        isPlaying = true
        mediaPlayer!!.start()
    }

    private fun pauseMusic(){
        binding.playPauseBtnPA.setImageResource(R.drawable.baseline_play_arrow_24)
        isPlaying = false
        mediaPlayer!!.pause()
    }

    private fun prevNextSong(increment: Boolean) {
        if (isShuffleEnabled) {
            // Si la reproducción aleatoria está activada, elegir una canción aleatoria
            val randomSongPosition = (0 until musicListPA.size).random()
            songPosition = randomSongPosition
        } else {
            // Si no, avanzar o retroceder en la lista de canciones según corresponda
            if (increment) {
                setSongPosition(true)
            } else {
                setSongPosition(false)
            }
        }
        setLayout()
        createMediaPlayer()
    }

    private fun setSongPosition(increment: Boolean){
        if(increment) {
            if (musicListPA.size - 1 == songPosition)
                songPosition = 0
            else ++songPosition
        } else {
            if (0 == songPosition)
                songPosition = musicListPA.size - 1
            else --songPosition
        }
    }
}