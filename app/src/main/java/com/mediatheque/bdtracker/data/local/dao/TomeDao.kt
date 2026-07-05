package com.mediatheque.bdtracker.data.local.dao

import androidx.room.*
import com.mediatheque.bdtracker.data.local.entity.TomeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TomeDao {

    // Tri par numéro croissant, comme demandé dans le cahier des charges
    @Query("SELECT * FROM tomes WHERE serieId = :serieId ORDER BY numero ASC")
    fun observerTomesDeLaSerie(serieId: Long): Flow<List<TomeEntity>>

    // Filtre "tomes non lus uniquement" (bonus demandé)
    @Query("SELECT * FROM tomes WHERE serieId = :serieId AND lu = 0 ORDER BY numero ASC")
    fun observerTomesNonLus(serieId: Long): Flow<List<TomeEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun inserer(tome: TomeEntity): Long

    @Update
    suspend fun mettreAJour(tome: TomeEntity)

    @Delete
    suspend fun supprimer(tome: TomeEntity)

    // Bascule rapide du statut lu / non lu (utilisé par le bouton "marquer comme lu")
    @Query("UPDATE tomes SET lu = :lu WHERE id = :tomeId")
    suspend fun changerStatutLu(tomeId: Long, lu: Boolean)

    @Query("SELECT COUNT(*) FROM tomes WHERE serieId = :serieId AND lu = 1")
    fun observerNombreTomesLus(serieId: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM tomes WHERE serieId = :serieId")
    fun observerNombreTotalTomes(serieId: Long): Flow<Int>
}
