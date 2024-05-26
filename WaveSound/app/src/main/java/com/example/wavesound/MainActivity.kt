package com.example.wavesound

import android.Manifest
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.wavesound.databinding.ActivityMainBinding
import com.example.wavesound.ui.local.LocalFavorite
import com.example.wavesound.ui.local.LocalPlayList
import com.google.android.material.navigation.NavigationView
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private val WRITE_EXTERNAL_STORAGE_PERMISSION_CODE = 1001
    private val READ_MEDIA_AUDIO_PERMISSION_CODE = 1002

    override fun onCreate(savedInstanceState: Bundle?) {
        // Verificar y solicitar permisos al iniciar la aplicacion por primera vez
        checkAndRequestPermissions()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)

        // Cargar la lista de canciones favoritas y la lista de reproduccion
        LocalFavorite.favoriteSongs = ArrayList()
        val editor = getSharedPreferences("FAVORITES", MODE_PRIVATE)
        val jsonString = editor.getString("FavoriteSongs", null)
        val typeToken = object : TypeToken<ArrayList<Music>>() {}.type
        if (jsonString != null) {
            val data : ArrayList<Music> = GsonBuilder().create().fromJson(jsonString, typeToken)
            LocalFavorite.favoriteSongs.addAll(data)
        }

        // Cargar la lista de reproduccion
        LocalPlayList.musicPlaylist = MusicPlaylist()
        val jsonStringPlaylist = editor.getString("MusicPlaylist", null)
        val typeTokenPlaylist = object : TypeToken<MusicPlaylist>() {}.type
        if (jsonStringPlaylist != null) {
            val dataPlaylist : MusicPlaylist = GsonBuilder().create().fromJson(jsonStringPlaylist, typeTokenPlaylist)
            LocalPlayList.musicPlaylist = dataPlaylist
        }
        setSupportActionBar(binding.appBarMain.toolbar)

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_local, R.id.nav_online, R.id.nav_user
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    // Menu de la barra de accion
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    // Navegacion de la barra de accion
    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    // Verificar y solicitar permisos al iniciar la aplicacion por primera vez
    private fun checkAndRequestPermissions() {
        //val writeStoragePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val readMediaAudioPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO)
        val permissionsToRequest = ArrayList<String>()

        /*Denegado por seguridad de google
        if (writeStoragePermission != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        */

        if (readMediaAudioPermission != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.READ_MEDIA_AUDIO)
        }

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toTypedArray(), WRITE_EXTERNAL_STORAGE_PERMISSION_CODE)
        }
    }

    // Manejar la respuesta de la solicitud de permisos
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == WRITE_EXTERNAL_STORAGE_PERMISSION_CODE || requestCode == READ_MEDIA_AUDIO_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                // Todos los permisos solicitados fueron concedidos, puedes realizar las acciones necesarias aqui
                restartApplication()
            } else {
                // Al menos un permiso fue denegado, manejar en consecuencia
                // Por ejemplo, mostrar un mensaje al usuario o tomar otra accion
            }
        }
    }

    // Reinicia la aplicacion
    private fun restartApplication() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finish()
    }

    // Popup para confirmar la salida de la aplicacion
    override fun onBackPressed() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Are you sure you want to exit??")
        builder.setPositiveButton("Yes") { _, _ ->
            exitApplication()
            super.onBackPressed()
        }
        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
        dialog.getButton(DialogInterface.BUTTON_POSITIVE)?.setTextColor(Color.WHITE)
        dialog.getButton(DialogInterface.BUTTON_NEGATIVE)?.setTextColor(Color.WHITE)
    }

    // Al salir de la aplicacion detener la musica y liberar los recursos
    override fun onDestroy() {
        super.onDestroy()
        if (!PlayerActivity.isPlaying && PlayerActivity.musicService != null) {
            PlayerActivity.musicService!!.stopForeground(true)
            PlayerActivity.musicService!!.mediaPlayer!!.release()
            PlayerActivity.musicService = null
            exitProcess(1)
        }

    }

    // Al salir de la aplicacion guardar la lista de canciones favoritas y la lista de reproduccion
    override fun onResume(){
        super.onResume()
        val editor = getSharedPreferences("FAVORITES", MODE_PRIVATE).edit()
        val jsonString = GsonBuilder().create().toJson(LocalFavorite.favoriteSongs)
        editor.putString("FavoriteSongs", jsonString)
        val jsonStringPlaylist = GsonBuilder().create().toJson(LocalPlayList.musicPlaylist)
        editor.putString("MusicPlaylist", jsonStringPlaylist)
        editor.apply()
    }


}