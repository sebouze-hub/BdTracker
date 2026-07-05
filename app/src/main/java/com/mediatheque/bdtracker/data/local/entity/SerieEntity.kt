package com.mediatheque.bdtracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Représente une série de BD dans la bibliothèque personnelle de l'enfant.
 * Une série peut ensuite contenir plusieurs [TomeEntity].
 */
@Entity(tableName = "series")
data class SerieEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // Titre de la série (ex: "Les Légendaires")
    val titre: String,

    // Auteur principal, s'il est connu (peut être vide)
    val auteur: String = "",

    // URL de la couverture (peut être vide si non disponible)
    val couvertureUrl: String? = null,

    // Identifiant Open Library de l'œuvre d'origine, utile pour éviter les doublons
    val openLibraryKey: String? = null
)
