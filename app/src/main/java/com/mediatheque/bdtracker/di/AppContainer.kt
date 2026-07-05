package com.mediatheque.bdtracker.di

import android.content.Context
import com.mediatheque.bdtracker.data.local.AppDatabase
import com.mediatheque.bdtracker.data.remote.OpenLibraryApi
import com.mediatheque.bdtracker.data.repository.BdRepository

/**
 * Conteneur de dépendances "fait main", volontairement simple pour rester
 * pédagogique. Dans un projet plus grand, on utiliserait Hilt ou Koin,
 * mais le principe (une seule instance du repository, partagée) resterait identique.
 */
class AppContainer(context: Context) {

    private val database = AppDatabase.getInstance(context)
    private val api = OpenLibraryApi.creer()

    val repository = BdRepository(
        serieDao = database.serieDao(),
        tomeDao = database.tomeDao(),
        api = api
    )
}
