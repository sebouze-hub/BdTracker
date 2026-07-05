package com.mediatheque.bdtracker.ui.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.mediatheque.bdtracker.data.local.entity.SerieEntity

/**
 * Écran principal : liste des séries déjà ajoutées par le parent.
 * C'est l'écran consulté EN DIRECT dans la médiathèque.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel,
    onSerieCliquee: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(
            value = uiState.recherche,
            onValueChange = viewModel::onRechercheChangee,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Rechercher dans ma bibliothèque") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (uiState.series.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Aucune série pour l'instant.\nUtilisez l'onglet Recherche pour en ajouter.")
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(uiState.series, key = { it.id }) { serie ->
                    LigneSerie(
                        serie = serie,
                        onClick = { onSerieCliquee(serie.id) },
                        onSupprimer = { viewModel.supprimerSerie(serie) }
                    )
                }
            }
        }
    }
}

@Composable
private fun LigneSerie(serie: SerieEntity, onClick: () -> Unit, onSupprimer: () -> Unit) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = serie.couvertureUrl,
                contentDescription = "Couverture de ${serie.titre}",
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(width = 48.dp, height = 64.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = serie.titre, style = MaterialTheme.typography.titleMedium)
                if (serie.auteur.isNotBlank()) {
                    Text(text = serie.auteur, style = MaterialTheme.typography.bodySmall)
                }
            }
            IconButton(onClick = onSupprimer) {
                Icon(Icons.Default.Delete, contentDescription = "Supprimer la série")
            }
        }
    }
}
