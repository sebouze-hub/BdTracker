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

    // Évite les doublons quand on ajoute une série déjà connue depuis Open Library
    @Query("SELECT * FROM series WHERE openLibraryKey = :key LIMIT 1")
    suspend fun trouverParOpenLibraryKey(key: String): SerieEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun inserer(serie: SerieEntity): Long

    @Delete
    suspend fun supprimer(serie: SerieEntity)

    // Recherche rapide dans la bibliothèque personnelle (bonus demandé)
    @Query("SELECT * FROM series WHERE titre LIKE '%' || :requete || '%' ORDER BY titre ASC")
    fun rechercherDansMaBibliotheque(requete: String): Flow<List<SerieEntity>>
}
