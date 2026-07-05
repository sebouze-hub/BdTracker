package com.mediatheque.bdtracker.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mediatheque.bdtracker.data.local.entity.SerieEntity
import com.mediatheque.bdtracker.data.local.entity.TomeEntity
import com.mediatheque.bdtracker.data.repository.BdRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class SeriesDetailUiState(
    val serie: SerieEntity? = null,
    val tomes: List<TomeEntity> = emptyList(),
    val nombreLus: Int = 0,
    val nombreTotal: Int = 0,
    val filtreNonLusUniquement: Boolean = false
)

class SeriesDetailViewModel(
    private val repository: BdRepository,
    private val serieId: Long
) : ViewModel() {

    private val filtreNonLus = MutableStateFlow(false)

    private val tomesFlow = filtreNonLus.flatMapLatest { filtre ->
        repository.observerTomes(serieId, seulementNonLus = filtre)
    }

    val uiState: StateFlow<SeriesDetailUiState> = combine(
        tomesFlow,
        repository.observerProgression(serieId),
        repository.observerTotalTomes(serieId),
        filtreNonLus
    ) { tomes, nombreLus, total, filtre ->
        SeriesDetailUiState(
            tomes = tomes,
            nombreLus = nombreLus,
            nombreTotal = total,
            filtreNonLusUniquement = filtre
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SeriesDetailUiState()
    )

    private val _serieState = MutableStateFlow<SerieEntity?>(null)
    val serie: StateFlow<SerieEntity?> = _serieState.asStateFlow()

    init {
        // Charge les infos de la série une seule fois (titre, auteur, couverture)
        viewModelScope.launch {
            _serieState.value = repository.getSerie(serieId)
        }
    }

    fun basculerFiltreNonLus() {
        filtreNonLus.value = !filtreNonLus.value
    }

    /** Action principale : un clic pour marquer un tome comme lu / non lu. */
    fun basculerLu(tome: TomeEntity) {
        viewModelScope.launch { repository.basculerStatutLu(tome) }
    }

    fun ajouterTomeManuel(numero: Int, titre: String) {
        viewModelScope.launch {
            repository.ajouterTomeManuel(serieId = serieId, numero = numero, titre = titre)
        }
    }

    fun supprimerTome(tome: TomeEntity) {
        viewModelScope.launch { repository.supprimerTome(tome) }
    }
}
