package dev.gmarques.compras.ui

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import dev.gmarques.compras.R
import dev.gmarques.compras.databinding.ActivityMainBinding
import dev.gmarques.compras.io.database.RoomDb
import dev.gmarques.compras.io.preferencias.Preferencias
import kotlinx.coroutines.runBlocking

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Preferencias().primeiroBoot()) runBlocking { RoomDb.criarListasItensEcategorias() }
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        navController = this.findNavController(R.id.navHostFrag)

        NavigationUI.setupActionBarWithNavController(this, navController)
        supportActionBar?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }


    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp()
    }
}