package com.mediatheque.bdtracker.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mediatheque.bdtracker.data.repository.BdRepository
import com.mediatheque.bdtracker.data.repository.TomeCandidat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * État immuable de l'écran de recherche.
 * `candidats` contient tous les tomes trouvés automatiquement en ligne,
 * chacun pouvant être décoché si l'utilisateur ne le veut pas dans sa bibliothèque.
 */
data class SearchUiState(
    val requete: String = "",
    val enChargement: Boolean = false,
    val candidats: List<TomeCandidat> = emptyList(),
    val messageErreur: String? = null,
    val ajoutReussi: Boolean = false,
    val nomSerieRecherchee: String = ""
)

class SearchViewModel(private val repository: BdRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    fun onRequeteChangee(nouvelleRequete: String) {
        _uiState.update { it.copy(requete = nouvelleRequete) }
    }

    /**
     * Lance la recherche : récupère automatiquement TOUS les tomes détectés
     * pour cette série, avec leur jaquette. Aucune saisie manuelle de numéro
     * n'est demandée à l'utilisateur.
     */
    fun lancerRecherche() {
        val requete = _uiState.value.requete.trim()
        if (requete.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(enChargement = true, messageErreur = null, candidats = emptyList()) }
            try {
                val resultats = repository.rechercherTomesEnLigne(requete)
                if (resultats.isEmpty()) {
                    _uiState.update {
                        it.copy(
                            enChargement = false,
                            messageErreur = "Aucun tome trouvé pour \"$requete\". Vérifiez l'orthographe ou essayez un autre titre."
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(enChargement = false, candidats = resultats, nomSerieRecherchee = requete)
                    }
                }
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

    /** Coche/décoche un tome candidat avant l'ajout définitif. */
    fun basculerSelection(index: Int) {
        _uiState.update { etat ->
            val nouvelleListe = etat.candidats.toMutableList()
            val candidat = nouvelleListe[index]
            nouvelleListe[index] = candidat.copy(selectionne = !candidat.selectionne)
            etat.copy(candidats = nouvelleListe)
        }
    }

    /** Ajoute la série et tous les tomes cochés en une seule fois, avec leur jaquette. */
    fun ajouterTomesSelectionnes() {
        val etat = _uiState.value
        val tomesRetenus = etat.candidats.filter { it.selectionne }
        if (tomesRetenus.isEmpty()) return

        viewModelScope.launch {
            repository.ajouterSerieAvecTomes(etat.nomSerieRecherchee, tomesRetenus)
            _uiState.update {
                it.copy(ajoutReussi = true, candidats = emptyList(), requete = "")
            }
        }
    }

    fun confirmationAffichee() {
        _uiState.update { it.copy(ajoutReussi = false) }
    }
}
