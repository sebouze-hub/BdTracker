package com.mediatheque.bdtracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.mediatheque.bdtracker.ui.navigation.BdTrackerNavGraph
import com.mediatheque.bdtracker.ui.theme.BdTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Récupère le repository unique créé dans BdTrackerApplication
        val repository = (application as BdTrackerApplication).container.repository

        setContent {
            BdTrackerTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    BdTrackerNavGraph(repository = repository)
                }
            }
        }
    }
}
