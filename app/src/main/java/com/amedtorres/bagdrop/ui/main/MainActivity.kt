package com.amedtorres.bagdrop.ui.main

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.amedtorres.bagdrop.R
import com.amedtorres.bagdrop.databinding.ActivityMainBinding
import com.amedtorres.bagdrop.ui.menu.HomeFragment
import com.amedtorres.bagdrop.ui.menu.MisReservasFragment
import com.amedtorres.bagdrop.ui.menu.PerfilFragment

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //cargamos el fragment de incioo por defecto
        if (savedInstanceState == null) {
            cambiarFragment(HomeFragment())
        }

        // configuracion para la barra de navegacion
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_inicio -> {
                    cambiarFragment(HomeFragment())
                    true
                }

                R.id.nav_reservas -> {
                    cambiarFragment(MisReservasFragment())
                    true
                }

                R.id.nav_perfil -> {
                    cambiarFragment(PerfilFragment())
                    true
                }

                else -> false
            }
        }
    }

    // funcion para cambiar el fragment
    private fun cambiarFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment, fragment)
            .commit()
        }
    }