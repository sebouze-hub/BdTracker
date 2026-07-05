package com.mediatheque.bdtracker.ui.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.mediatheque.bdtracker.data.repository.TomeCandidat

/**
 * Écran de recherche : le parent tape juste le nom de la série, et TOUS les tomes
 * détectés apparaissent automatiquement avec leur jaquette. Un seul bouton
 * "Ajouter" les insère tous dans la bibliothèque, sans saisie manuelle de numéro.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(viewModel: SearchViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.ajoutReussi) {
        if (uiState.ajoutReussi) {
            snackbarHostState.showSnackbar("Série et tomes ajoutés à la bibliothèque ✅")
            viewModel.confirmationAffichee()
        }
    }

    val nombreSelectionnes = uiState.candidats.count { it.selectionne }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (uiState.candidats.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    text = { Text("Ajouter les $nombreSelectionnes tomes") },
                    icon = { Icon(Icons.Default.Search, contentDescription = null) },
                    onClick = viewModel::ajouterTomesSelectionnes
                )
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {

            OutlinedTextField(
                value = uiState.requete,
                onValueChange = viewModel::onRequeteChangee,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Nom de la série (ex : Les Légendaires)") },
                singleLine = true,
                trailingIcon = {
                    IconButton(onClick = viewModel::lancerRecherche) {
                        Icon(Icons.Default.Search, contentDescription = "Rechercher")
                    }
                },
                keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                    onSearch = { viewModel.lancerRecherche() }
                ),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    imeAction = androidx.compose.ui.text.input.ImeAction.Search
                )
            )

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Tous les tomes trouvés sont ajoutés automatiquement avec leur jaquette. Décochez ceux que vous ne voulez pas.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            when {
                uiState.enChargement -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                uiState.messageErreur != null -> {
                    Text(
                        text = uiState.messageErreur ?: "",
                        color = MaterialTheme.colorScheme.error
                    )
                }

                uiState.candidats.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Tapez le nom d'une série pour récupérer automatiquement tous ses tomes.")
                    }
                }

                else -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(uiState.candidats.size) { index ->
                            val candidat = uiState.candidats[index]
                            LigneCandidat(
                                candidat = candidat,
                                onClick = { viewModel.basculerSelection(index) }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(72.dp)) } // Place pour le FAB
                    }
                }
            }
        }
    }
}

@Composable
private fun LigneCandidat(candidat: TomeCandidat, onClick: () -> Unit) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = candidat.selectionne, onCheckedChange = { onClick() })

            AsyncImage(
                model = candidat.couvertureUrl,
                contentDescription = "Couverture de ${candidat.titre}",
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(width = 48.dp, height = 64.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                candidat.numero?.let {
                    Text(text = "Tome $it", style = MaterialTheme.typography.labelMedium)
                }
                Text(
                    text = candidat.titre,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
