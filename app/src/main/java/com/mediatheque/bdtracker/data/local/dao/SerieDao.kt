package com.mediatheque.bdtracker.data.local.dao

import androidx.room.*
import com.mediatheque.bdtracker.data.local.entity.SerieEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SerieDao {

    // Flow = flux observable : l'UI se met à jour automatiquement quand les données changent
    @Query("SELECT * FROM series ORDER BY titre ASC")
    fun observerToutesLesSeries(): Flow<List<SerieEntity>>

    @Query("SELECT * FROM series WHERE id = :serieId")
    suspend fun getSerieParId(serieId: Long): SerieEntity?

    // Évite les doublons quand on recherche deux fois la même série
    @Query("SELECT * FROM series WHERE titre = :titre COLLATE NOCASE LIMIT 1")
    suspend fun trouverParTitre(titre: String): SerieEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun inserer(serie: SerieEntity): Long

    @Delete
    suspend fun supprimer(serie: SerieEntity)

    // Recherche rapide dans la bibliothèque personnelle (bonus demandé)
    @Query("SELECT * FROM series WHERE titre LIKE '%' || :requete || '%' ORDER BY titre ASC")
    fun rechercherDansMaBibliotheque(requete: String): Flow<List<SerieEntity>>
}
