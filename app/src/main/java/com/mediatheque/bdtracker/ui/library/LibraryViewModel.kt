package com.mediatheque.bdtracker.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mediatheque.bdtracker.data.local.entity.SerieEntity
import com.mediatheque.bdtracker.data.repository.BdRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class LibraryUiState(
    val recherche: String = "",
    val series: List<SerieEntity> = emptyList()
)

class LibraryViewModel(private val repository: BdRepository) : ViewModel() {

    private val recherche = MutableStateFlow("")

    // On combine le texte de recherche avec le flux Room : la liste se recalcule
    // automatiquement à chaque changement de l'un ou de l'autre.
    private val series = recherche.flatMapLatest { texte ->
        if (texte.isBlank()) repository.observerMaBibliotheque()
        else repository.rechercherDansMaBibliotheque(texte)
    }

    val uiState: StateFlow<LibraryUiState> = combine(recherche, series) { texte, listeSeries ->
        LibraryUiState(recherche = texte, series = listeSeries)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LibraryUiState()
    )

    fun onRechercheChangee(texte: String) {
        recherche.value = texte
    }

    fun supprimerSerie(serie: SerieEntity) {
        viewModelScope.launch { repository.supprimerSerie(serie) }
    }
}
