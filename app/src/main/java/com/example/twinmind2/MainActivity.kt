package com.example.twinmind2

import android.Manifest
import android.content.Context
import android.os.Bundle

import androidx.activity.ComponentActivity

import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat

import com.example.twinmind2.ui.theme.Twinmind2Theme



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModel=AuthViewModel(this)
        enableEdgeToEdge()
        requestPermissions()
        setContent {
            Twinmind2Theme {

                AppNavigation(viewModel)
            }
        }
    }
            private fun requestPermissions() {
                val permissions = arrayOf(
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                ActivityCompat.requestPermissions(this, permissions, 1001)
            }


}








