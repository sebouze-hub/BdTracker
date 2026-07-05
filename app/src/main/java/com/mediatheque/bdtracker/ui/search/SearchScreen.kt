package com.mediatheque.bdtracker.ui.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import com.mediatheque.bdtracker.data.remote.model.OpenLibraryDoc

/**
 * Écran de recherche : permet de trouver une série via Open Library
 * et de l'ajouter d'un clic à la bibliothèque personnelle.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(viewModel: SearchViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Affiche une confirmation quand une série vient d'être ajoutée
    LaunchedEffect(uiState.serieAjouteeAvecSucces) {
        if (uiState.serieAjouteeAvecSucces) {
            snackbarHostState.showSnackbar("Ajoutée à la bibliothèque ✅")
            viewModel.confirmationAffichee()
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {

            // Barre de recherche
            OutlinedTextField(
                value = uiState.requete,
                onValueChange = viewModel::onRequeteChangee,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Titre de la série ou de la BD") },
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

                uiState.resultats.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Recherchez une série pour commencer.")
                    }
                }

                else -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(uiState.resultats) { doc ->
                            ResultatRecherche(doc = doc, onAjouter = { viewModel.ajouterALaBibliotheque(doc) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ResultatRecherche(doc: OpenLibraryDoc, onAjouter: () -> Unit) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = doc.urlCouverture(),
                contentDescription = "Couverture de ${doc.titre}",
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(width = 48.dp, height = 64.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = doc.titre ?: "Titre inconnu",
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                doc.auteurs?.firstOrNull()?.let {
                    Text(text = it, style = MaterialTheme.typography.bodySmall)
                }
            }
            IconButton(onClick = onAjouter) {
                Icon(Icons.Default.Add, contentDescription = "Ajouter à ma bibliothèque")
            }
        }
    }
}
