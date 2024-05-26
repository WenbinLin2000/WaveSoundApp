package com.example.wavesound

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.wavesound.databinding.FragmentNowPlayingBinding
import com.example.wavesound.ui.local.LocalFavorite

// Fragmento que muestra la cancion que se está reproduciendo actualmente
class NowPlaying : Fragment() {
    companion object{
        @SuppressLint("StaticFieldLeak")
        lateinit var binding: FragmentNowPlayingBinding

        // Revisa si la cancion es favorito y cambia el icono
        fun changeFavoriteIcon(){
            if(PlayerActivity.isFavourite) binding.favoriteBtnNP.setImageResource(R.drawable.baseline_favorite_24)
            else binding.favoriteBtnNP.setImageResource(R.drawable.baseline_favorite_border_24)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_now_playing, container, false)
        binding = FragmentNowPlayingBinding.bind(view)
        binding.root.visibility = View.INVISIBLE
        binding.playPauseBtnNP.setOnClickListener {
            if(PlayerActivity.isPlaying) pauseMusic() else playMusic()
        }

        binding.favoriteBtnNP.setOnClickListener {
            if(PlayerActivity.isFavourite){
                PlayerActivity.isFavourite = false
                LocalFavorite.favoriteSongs.remove(PlayerActivity.musicListPA[PlayerActivity.songPosition])
                binding.favoriteBtnNP.setImageResource(R.drawable.baseline_favorite_border_24)
            }else{
                PlayerActivity.isFavourite = true
                LocalFavorite.favoriteSongs.add(PlayerActivity.musicListPA[PlayerActivity.songPosition])
                binding.favoriteBtnNP.setImageResource(R.drawable.baseline_favorite_24)
            }
            LocalFavorite.favoritesChanged = true
            PlayerActivity.changeFavoriteIcon()

        }

        binding.root.setOnClickListener {
            val intent = Intent(requireContext(), PlayerActivity::class.java)
            intent.putExtra("index", PlayerActivity.songPosition)
            intent.putExtra("class", "NowPlaying")
            ContextCompat.startActivity(requireContext(), intent, null)
        }
        return view
    }

    // Actualiza la interfaz gráfica con la cancion actual
    override fun onResume() {
        super.onResume()
        if(PlayerActivity.musicService != null){
            binding.root.visibility = View.VISIBLE
            binding.songNameNP.isSelected = true
            binding.songNameNP.text = PlayerActivity.musicListPA[PlayerActivity.songPosition].title
            Glide.with(requireContext())
                .load(PlayerActivity.musicListPA[PlayerActivity.songPosition].artUri)
                .apply(RequestOptions().placeholder(R.drawable.logob).centerCrop())
                .into(binding.songImgNP)
            if(PlayerActivity.isPlaying) binding.playPauseBtnNP.setImageResource(R.drawable.baseline_pause_24)
            else binding.playPauseBtnNP.setImageResource(R.drawable.baseline_play_arrow_24)
            if (PlayerActivity.isFavourite) binding.favoriteBtnNP.setImageResource(R.drawable.baseline_favorite_24)
            else binding.favoriteBtnNP.setImageResource(R.drawable.baseline_favorite_border_24)

            // Establecer el listener para detectar cuando la cancion actual finaliza
            PlayerActivity.musicService!!.mediaPlayer!!.setOnCompletionListener {
                setSongPosition(increment = true)
                PlayerActivity.musicService!!.createMediaPlayer()
                Glide.with(requireContext())
                    .load(PlayerActivity.musicListPA[PlayerActivity.songPosition].artUri)
                    .apply(RequestOptions().placeholder(R.drawable.logob).centerCrop())
                    .into(binding.songImgNP)
                binding.songNameNP.text = PlayerActivity.musicListPA[PlayerActivity.songPosition].title
                PlayerActivity.musicService!!.showNotification(R.drawable.baseline_pause_24)
                playMusic()

            }
        }
    }

    // Fucion de play
    private fun playMusic(){
        PlayerActivity.isPlaying = true
        PlayerActivity.musicService!!.mediaPlayer!!.start()
        PlayerActivity.musicService!!.showNotification(R.drawable.baseline_pause_24)
        PlayerActivity.binding.playPauseBtnPA.setImageResource(R.drawable.baseline_pause_24)
        binding.playPauseBtnNP.setImageResource(R.drawable.baseline_pause_24)

    }

    // Funcion de pausa
    private fun pauseMusic(){
        PlayerActivity.isPlaying = false
        PlayerActivity.musicService!!.mediaPlayer!!.pause()
        PlayerActivity.musicService!!.showNotification(R.drawable.baseline_play_arrow_24)
        PlayerActivity.binding.playPauseBtnPA.setImageResource(R.drawable.baseline_play_arrow_24)
        binding.playPauseBtnNP.setImageResource(R.drawable.baseline_play_arrow_24)
    }
}