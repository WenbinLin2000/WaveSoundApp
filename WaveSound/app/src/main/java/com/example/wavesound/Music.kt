package com.example.wavesound

import android.media.MediaMetadataRetriever
import com.example.wavesound.ui.local.LocalFavorite
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess

// Entidad que representa una cancion
data class Music(val id:String, val title:String, val album:String ,val artist:String, val duration: Long = 0, val path: String, val artUri:String){

}

// Entidad que representa una lista de reproduccion
class Playlist{
    lateinit var name: String
    lateinit var playlist: ArrayList<Music>
    lateinit var createdBy: String
    lateinit var createdOn: String
}

// Entidad que representa una lista de listas de reproduccion
class MusicPlaylist{
    var ref: ArrayList<Playlist> = ArrayList()
}

// Formatea la duracion de la cancion
fun formatDuration(duration: Long):String{
    val minutes = TimeUnit.MINUTES.convert(duration, TimeUnit.MILLISECONDS)
    val seconds = (TimeUnit.SECONDS.convert(duration, TimeUnit.MILLISECONDS) -
            minutes*TimeUnit.SECONDS.convert(1, TimeUnit.MINUTES))
    return String.format("%02d:%02d", minutes, seconds)
}

// Obtiene la imagen de la portada de la cancion
fun getImgArt(path: String): ByteArray? {
    val retriever = MediaMetadataRetriever()
    retriever.setDataSource(path)
    return retriever.embeddedPicture
}

// Obtiene la posicion de la cancion en la lista de reproduccion
fun setSongPosition(increment: Boolean){
        if(increment)
        {
            if(PlayerActivity.musicListPA.size - 1 == PlayerActivity.songPosition)
                PlayerActivity.songPosition = 0
            else ++PlayerActivity.songPosition
        }else{
            if(0 == PlayerActivity.songPosition)
                PlayerActivity.songPosition = PlayerActivity.musicListPA.size-1
            else --PlayerActivity.songPosition
        }
}

// Al salir de la aplicacion
fun exitApplication(){
    if(PlayerActivity.musicService != null){
        PlayerActivity.musicService!!.stopForeground(true)
        PlayerActivity.musicService!!.mediaPlayer!!.release()
        PlayerActivity.musicService = null}
    exitProcess(1)
}

// Verifica si la cancion es favorita
fun favoriteChecker(id: String): Int{
    PlayerActivity.isFavourite = false
    LocalFavorite.favoriteSongs.forEachIndexed { index, music ->
        if(id == music.id){
            PlayerActivity.isFavourite = true
            return index
        }
    }
    return -1
}

// Verifica si la cancion esta en la lista de reproduccion
fun checkPlaylist(playlist: ArrayList<Music>): ArrayList<Music>{
    playlist.forEachIndexed { index, music ->
        val file = File(music.path)
        if(!file.exists())
            playlist.removeAt(index)
    }
    return playlist
}