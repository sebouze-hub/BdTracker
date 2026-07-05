package com.mediatheque.bdtracker.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Représente un tome appartenant à une série.
 * La table "relation + statut lu/non lu" demandée dans le cahier des charges
 * est fusionnée ici avec la table des tomes : chaque tome porte directement
 * son statut "lu", ce qui simplifie grandement les requêtes tout en respectant
 * le besoin fonctionnel (un tome appartient à une seule série).
 */
@Entity(
    tableName = "tomes",
    foreignKeys = [
        ForeignKey(
            entity = SerieEntity::class,
            parentColumns = ["id"],
            childColumns = ["serieId"],
            onDelete = ForeignKey.CASCADE // Si la série est supprimée, ses tomes le sont aussi
        )
    ],
    indices = [Index("serieId")]
)
data class TomeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // Clé étrangère vers la série parente
    val serieId: Long,

    // Numéro du tome dans la série (1, 2, 3, ...)
    val numero: Int,

    // Titre du tome (peut être identique au numéro si inconnu, ex: "Tome 3")
    val titre: String,

    // URL de couverture optionnelle
    val couvertureUrl: String? = null,

    // Statut de lecture : true = déjà lu par l'enfant
    val lu: Boolean = false
)
