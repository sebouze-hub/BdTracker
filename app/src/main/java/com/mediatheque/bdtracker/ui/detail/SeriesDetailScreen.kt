package com.mediatheque.bdtracker.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.mediatheque.bdtracker.data.local.entity.TomeEntity
import com.mediatheque.bdtracker.ui.theme.RougeNonLu
import com.mediatheque.bdtracker.ui.theme.VertLu

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeriesDetailScreen(
    viewModel: SeriesDetailViewModel,
    onRetour: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val serie by viewModel.serie.collectAsStateWithLifecycle()
    var afficherDialogueAjout by remember { mutableStateOf(false) }
    var vignetteAgrandie by remember { mutableStateOf<TomeEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(serie?.titre ?: "Série") },
                navigationIcon = {
                    IconButton(onClick = onRetour) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { afficherDialogueAjout = true }) {
                Icon(Icons.Default.Add, contentDescription = "Ajouter un tome")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(horizontal = 16.dp)) {

            // Barre de progression : X / Y tomes lus
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${uiState.nombreLus} / ${uiState.nombreTotal} tomes lus",
                style = MaterialTheme.typography.titleMedium
            )
            LinearProgressIndicator(
                progress = if (uiState.nombreTotal > 0) uiState.nombreLus / uiState.nombreTotal.toFloat() else 0f,
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            )

            // Filtre "tomes non lus uniquement" (bonus demandé)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                    checked = uiState.filtreNonLusUniquement,
                    onCheckedChange = { viewModel.basculerFiltreNonLus() }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Afficher uniquement les tomes non lus")
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (uiState.tomes.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Aucun tome pour l'instant. Appuyez sur + pour en ajouter.")
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(uiState.tomes, key = { it.id }) { tome ->
                        LigneTome(
                            tome = tome,
                            onToggleLu = { viewModel.basculerLu(tome) },
                            onSupprimer = { viewModel.supprimerTome(tome) },
                            onClicVignette = { vignetteAgrandie = tome }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(72.dp)) } // Place pour le FAB
                }
            }
        }
    }

    if (afficherDialogueAjout) {
        DialogueAjoutTome(
            onValider = { numero, titre ->
                viewModel.ajouterTomeManuel(numero, titre)
                afficherDialogueAjout = false
            },
            onAnnuler = { afficherDialogueAjout = false }
        )
    }

    vignetteAgrandie?.let { tome ->
        DialogueVignetteAgrandie(tome = tome, onFermer = { vignetteAgrandie = null })
    }
}

/**
 * Ligne représentant un tome. C'est ICI que se trouve le geste principal
 * demandé par le parent en médiathèque : un clic pour marquer "déjà lu".
 */
@Composable
private fun LigneTome(
    tome: TomeEntity,
    onToggleLu: () -> Unit,
    onSupprimer: () -> Unit,
    onClicVignette: () -> Unit
) {
    val couleurStatut = if (tome.lu) VertLu else RougeNonLu

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggleLu) // Toute la carte est cliquable : rapide en situation réelle
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Pastille de couleur : indicateur visuel demandé (vert = lu)
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(couleurStatut)
            )
            Spacer(modifier = Modifier.width(8.dp))

            // Vignette de couverture : un clic dessus l'agrandit (sans marquer lu/non lu)
            AsyncImage(
                model = tome.couvertureUrl,
                contentDescription = "Couverture du tome ${tome.numero}, appuyer pour agrandir",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(width = 44.dp, height = 60.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .clickable(onClick = onClicVignette)
            )
            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Tome ${tome.numero}", style = MaterialTheme.typography.labelMedium)
                Text(text = tome.titre, style = MaterialTheme.typography.titleMedium)
            }

            // Bouton toggle explicite en plus du clic sur la carte
            IconButton(onClick = onToggleLu) {
                Icon(
                    imageVector = if (tome.lu) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = if (tome.lu) "Marquer comme non lu" else "Marquer comme lu",
                    tint = couleurStatut
                )
            }

            IconButton(onClick = onSupprimer) {
                Icon(Icons.Default.Delete, contentDescription = "Supprimer le tome")
            }
        }
    }
}

/** Affiche la jaquette du tome en grand, par-dessus l'écran, jusqu'à ce que l'utilisateur la ferme. */
@Composable
private fun DialogueVignetteAgrandie(tome: TomeEntity, onFermer: () -> Unit) {
    Dialog(onDismissRequest = onFermer, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .clickable(onClick = onFermer), // Cliquer n'importe où sur la vignette la referme
            contentAlignment = Alignment.TopEnd
        ) {
            AsyncImage(
                model = tome.couvertureUrl,
                contentDescription = "Couverture agrandie du tome ${tome.numero}",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
            )
            IconButton(onClick = onFermer) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Fermer",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun DialogueAjoutTome(onValider: (Int, String) -> Unit, onAnnuler: () -> Unit) {
    var numero by remember { mutableStateOf("") }
    var titre by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onAnnuler,
        title = { Text("Ajouter un tome") },
        text = {
            Column {
                OutlinedTextField(
                    value = numero,
                    onValueChange = { numero = it.filter { c -> c.isDigit() } },
                    label = { Text("Numéro du tome") },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = titre,
                    onValueChange = { titre = it },
                    label = { Text("Titre (optionnel)") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val n = numero.toIntOrNull() ?: return@TextButton
                    val t = titre.ifBlank { "Tome $n" }
                    onValider(n, t)
                },
                enabled = numero.isNotBlank()
            ) { Text("Ajouter") }
        },
        dismissButton = {
            TextButton(onClick = onAnnuler) { Text("Annuler") }
        }
    )
}
