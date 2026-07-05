package com.mediatheque.bdtracker.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mediatheque.bdtracker.data.remote.model.OpenLibraryDoc
import com.mediatheque.bdtracker.data.repository.BdRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * État immuable de l'écran de recherche.
 * On regroupe tout dans un seul StateFlow : c'est plus simple à observer
 * depuis Compose et ça évite les incohérences entre plusieurs états séparés.
 */
data class SearchUiState(
    val requete: String = "",
    val enChargement: Boolean = false,
    val resultats: List<OpenLibraryDoc> = emptyList(),
    val messageErreur: String? = null,
    val serieAjouteeAvecSucces: Boolean = false
)

class SearchViewModel(private val repository: BdRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    fun onRequeteChangee(nouvelleRequete: String) {
        _uiState.update { it.copy(requete = nouvelleRequete) }
    }

    fun lancerRecherche() {
        val requete = _uiState.value.requete
        if (requete.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(enChargement = true, messageErreur = null) }
            try {
                val resultats = repository.rechercherSeriesEnLigne(requete)
                _uiState.update { it.copy(enChargement = false, resultats = resultats) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        enChargement = false,
                        messageErreur = "Recherche impossible. Vérifiez votre connexion internet."
                    )
                }
            }
        }
    }

    fun ajouterALaBibliotheque(doc: OpenLibraryDoc) {
        viewModelScope.launch {
            repository.ajouterSerieDepuisOpenLibrary(doc)
            _uiState.update { it.copy(serieAjouteeAvecSucces = true) }
        }
    }

    fun confirmationAffichee() {
        _uiState.update { it.copy(serieAjouteeAvecSucces = false) }
    }
}
