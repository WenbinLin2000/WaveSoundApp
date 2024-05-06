package com.example.wavesound

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Menu
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.wavesound.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private val WRITE_EXTERNAL_STORAGE_PERMISSION_CODE = 1001
    private val READ_MEDIA_AUDIO_PERMISSION_CODE = 1002

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Verificar y solicitar permisos al iniciar la aplicación por primera vez
        checkAndRequestPermissions()

        setSupportActionBar(binding.appBarMain.toolbar)

        binding.appBarMain.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null)
                .setAnchorView(R.id.fab).show()
        }
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun checkAndRequestPermissions() {
        val writeStoragePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val readMediaAudioPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO)

        val permissionsToRequest = ArrayList<String>()

        if (writeStoragePermission != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if (readMediaAudioPermission != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.READ_MEDIA_AUDIO)
        }

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toTypedArray(), WRITE_EXTERNAL_STORAGE_PERMISSION_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == WRITE_EXTERNAL_STORAGE_PERMISSION_CODE || requestCode == READ_MEDIA_AUDIO_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                // Todos los permisos fueron concedidos
            } else {
                // Al menos un permiso fue denegado, manejar en consecuencia
            }
        }
    }
}