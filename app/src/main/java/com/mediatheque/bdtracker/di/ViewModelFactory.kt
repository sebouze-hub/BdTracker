package com.mediatheque.bdtracker.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.mediatheque.bdtracker.data.repository.BdRepository
import com.mediatheque.bdtracker.ui.detail.SeriesDetailViewModel
import com.mediatheque.bdtracker.ui.library.LibraryViewModel
import com.mediatheque.bdtracker.ui.search.SearchViewModel

/**
 * Factory unique capable de construire n'importe quel ViewModel de l'application
 * en lui injectant le [BdRepository]. Évite d'avoir à ajouter une dépendance
 * comme Hilt pour un projet de cette taille.
 */
class ViewModelFactory(
    private val repository: BdRepository,
    private val serieId: Long = -1L
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        return when {
            modelClass.isAssignableFrom(SearchViewModel::class.java) ->
                SearchViewModel(repository) as T

            modelClass.isAssignableFrom(LibraryViewModel::class.java) ->
                LibraryViewModel(repository) as T

            modelClass.isAssignableFrom(SeriesDetailViewModel::class.java) ->
                SeriesDetailViewModel(repository, serieId) as T

            else -> throw IllegalArgumentException("ViewModel inconnu : ${modelClass.name}")
        }
    }
}
