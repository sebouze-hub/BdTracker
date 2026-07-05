package com.mediatheque.bdtracker

import android.app.Application
import com.mediatheque.bdtracker.di.AppContainer

class BdTrackerApplication : Application() {

    // Créé une seule fois pour toute la durée de vie de l'application
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
