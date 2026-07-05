package com.mediatheque.bdtracker.data.repository

import com.mediatheque.bdtracker.data.local.dao.SerieDao
import com.mediatheque.bdtracker.data.local.dao.TomeDao
import com.mediatheque.bdtracker.data.local.entity.SerieEntity
import com.mediatheque.bdtracker.data.local.entity.TomeEntity
import com.mediatheque.bdtracker.data.remote.OpenLibraryApi
import com.mediatheque.bdtracker.data.remote.model.OpenLibraryDoc
import kotlinx.coroutines.flow.Flow

/**
 * Le Repository est la SEULE source de vérité pour les ViewModels.
 * Il masque la complexité : les ViewModels n'ont pas besoin de savoir
 * si une donnée vient du réseau (Open Library) ou de la base locale (Room).
 */
class BdRepository(
    private val serieDao: SerieDao,
    private val tomeDao: TomeDao,
    private val api: OpenLibraryApi
) {

    // ---------- Recherche en ligne (Open Library) ----------

    suspend fun rechercherSeriesEnLigne(requete: String): List<OpenLibraryDoc> {
        if (requete.isBlank()) return emptyList()
        return api.rechercherLivres(requete).documents
    }

    // ---------- Bibliothèque personnelle (Room) ----------

    fun observerMaBibliotheque(): Flow<List<SerieEntity>> = serieDao.observerToutesLesSeries()

    fun rechercherDansMaBibliotheque(requete: String): Flow<List<SerieEntity>> =
        serieDao.rechercherDansMaBibliotheque(requete)

    suspend fun getSerie(serieId: Long): SerieEntity? = serieDao.getSerieParId(serieId)

    /**
     * Ajoute une série trouvée en ligne à la bibliothèque personnelle.
     * Retourne l'identifiant local de la série (nouvelle ou déjà existante).
     */
    suspend fun ajouterSerieDepuisOpenLibrary(doc: OpenLibraryDoc): Long {
        // On évite les doublons si la série a déjà été ajoutée précédemment
        doc.key?.let { key ->
            serieDao.trouverParOpenLibraryKey(key)?.let { return it.id }
        }
        val nouvelleSerie = SerieEntity(
            titre = doc.titre ?: "Titre inconnu",
            auteur = doc.auteurs?.joinToString(", ") ?: "",
            couvertureUrl = doc.urlCouverture(),
            openLibraryKey = doc.key
        )
        return serieDao.inserer(nouvelleSerie)
    }

    suspend fun supprimerSerie(serie: SerieEntity) = serieDao.supprimer(serie)

    // ---------- Tomes ----------

    fun observerTomes(serieId: Long, seulementNonLus: Boolean): Flow<List<TomeEntity>> {
        return if (seulementNonLus) {
            tomeDao.observerTomesNonLus(serieId)
        } else {
            tomeDao.observerTomesDeLaSerie(serieId)
        }
    }

    fun observerProgression(serieId: Long): Flow<Int> = tomeDao.observerNombreTomesLus(serieId)
    fun observerTotalTomes(serieId: Long): Flow<Int> = tomeDao.observerNombreTotalTomes(serieId)

    suspend fun ajouterTome(serieId: Long, numero: Int, titre: String, couvertureUrl: String? = null) {
        tomeDao.inserer(
            TomeEntity(serieId = serieId, numero = numero, titre = titre, couvertureUrl = couvertureUrl)
        )
    }

    suspend fun ajouterTomeDepuisOpenLibrary(serieId: Long, numero: Int, doc: OpenLibraryDoc) {
        tomeDao.inserer(
            TomeEntity(
                serieId = serieId,
                numero = numero,
                titre = doc.titre ?: "Tome $numero",
                couvertureUrl = doc.urlCouverture()
            )
        )
    }

    /** Bascule le statut lu / non lu : c'est LE geste principal de l'application. */
    suspend fun basculerStatutLu(tome: TomeEntity) {
        tomeDao.changerStatutLu(tome.id, !tome.lu)
    }

    suspend fun supprimerTome(tome: TomeEntity) = tomeDao.supprimer(tome)
}
