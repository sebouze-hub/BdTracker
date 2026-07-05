package com.mediatheque.bdtracker.data.repository

import com.mediatheque.bdtracker.data.local.dao.SerieDao
import com.mediatheque.bdtracker.data.local.dao.TomeDao
import com.mediatheque.bdtracker.data.local.entity.SerieEntity
import com.mediatheque.bdtracker.data.local.entity.TomeEntity
import com.mediatheque.bdtracker.data.remote.GoogleBooksApi
import com.mediatheque.bdtracker.util.TomeNumeroExtractor
import kotlinx.coroutines.flow.Flow

/**
 * Un tome trouvé en ligne, avant confirmation par l'utilisateur.
 * `numero` peut être null si aucun numéro n'a pu être détecté dans le titre :
 * dans ce cas, un numéro de secours lui sera attribué au moment de l'ajout.
 */
data class TomeCandidat(
    val titre: String,
    val numero: Int?,
    val couvertureUrl: String?,
    val selectionne: Boolean = true
)

/**
 * Le Repository est la SEULE source de vérité pour les ViewModels.
 * Il masque la complexité : les ViewModels n'ont pas besoin de savoir
 * si une donnée vient du réseau (Google Books) ou de la base locale (Room).
 */
class BdRepository(
    private val serieDao: SerieDao,
    private val tomeDao: TomeDao,
    private val api: GoogleBooksApi
) {

    // ---------- Recherche en ligne (Google Books) ----------

    /**
     * Recherche tous les tomes correspondant au nom de série donné, détecte
     * automatiquement leur numéro à partir du titre, et les trie.
     * Les résultats sans numéro détecté sont placés à la fin.
     */
    suspend fun rechercherTomesEnLigne(nomSerie: String): List<TomeCandidat> {
        if (nomSerie.isBlank()) return emptyList()

        val reponse = api.rechercherVolumes(requete = "intitle:$nomSerie")
        val items = reponse.items.orEmpty()

        val candidats = items.mapNotNull { item ->
            val info = item.volumeInfo ?: return@mapNotNull null
            val titre = info.titreComplet().ifBlank { return@mapNotNull null }
            TomeCandidat(
                titre = titre,
                numero = TomeNumeroExtractor.extraire(titre),
                couvertureUrl = info.imageLinks?.urlSecurisee()
            )
        }

        // Dédoublonnage : deux éditions différentes du même tome ne doivent apparaître qu'une fois
        val dedupliques = candidats
            .distinctBy { it.numero ?: it.titre.lowercase() }
            .sortedWith(compareBy(nullsLast()) { it.numero })

        return dedupliques
    }

    // ---------- Bibliothèque personnelle (Room) ----------

    fun observerMaBibliotheque(): Flow<List<SerieEntity>> = serieDao.observerToutesLesSeries()

    fun rechercherDansMaBibliotheque(requete: String): Flow<List<SerieEntity>> =
        serieDao.rechercherDansMaBibliotheque(requete)

    suspend fun getSerie(serieId: Long): SerieEntity? = serieDao.getSerieParId(serieId)

    /**
     * Crée (ou récupère) la série, puis ajoute automatiquement tous les tomes
     * sélectionnés avec leur jaquette. C'est l'action principale de l'écran de recherche.
     * Retourne l'identifiant local de la série.
     */
    suspend fun ajouterSerieAvecTomes(nomSerie: String, tomes: List<TomeCandidat>): Long {
        val serieExistante = serieDao.trouverParTitre(nomSerie)
        val serieId = serieExistante?.id ?: serieDao.inserer(
            SerieEntity(
                titre = nomSerie,
                couvertureUrl = tomes.firstOrNull()?.couvertureUrl
            )
        )

        // Numéro de secours pour les tomes dont on n'a pas pu détecter le numéro dans le titre
        var prochainNumeroDeSecours = (tomes.mapNotNull { it.numero }.maxOrNull() ?: 0) + 1

        for (candidat in tomes) {
            val numero = candidat.numero ?: prochainNumeroDeSecours++
            // On évite d'insérer deux fois le même numéro de tome pour cette série
            val dejaPresent = tomeDao.trouverParSerieEtNumero(serieId, numero)
            if (dejaPresent == null) {
                tomeDao.inserer(
                    TomeEntity(
                        serieId = serieId,
                        numero = numero,
                        titre = candidat.titre,
                        couvertureUrl = candidat.couvertureUrl
                    )
                )
            }
        }

        return serieId
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

    /** Ajout manuel d'un tome, conservé comme solution de secours si un tome n'est pas trouvé en ligne. */
    suspend fun ajouterTomeManuel(serieId: Long, numero: Int, titre: String, couvertureUrl: String? = null) {
        tomeDao.inserer(
            TomeEntity(serieId = serieId, numero = numero, titre = titre, couvertureUrl = couvertureUrl)
        )
    }

    /** Bascule le statut lu / non lu : c'est LE geste principal de l'application. */
    suspend fun basculerStatutLu(tome: TomeEntity) {
        tomeDao.changerStatutLu(tome.id, !tome.lu)
    }

    suspend fun supprimerTome(tome: TomeEntity) = tomeDao.supprimer(tome)
}
