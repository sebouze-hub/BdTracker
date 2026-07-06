package com.mediatheque.bdtracker.data.remote.model

import com.google.gson.annotations.SerializedName

/**
 * Réponse brute de l'endpoint de recherche Google Books :
 * https://www.googleapis.com/books/v1/volumes?q=...
 * Aucune clé API n'est nécessaire pour ce niveau d'usage (quota gratuit).
 */
data class GoogleBooksResponse(
    @SerializedName("totalItems")
    val totalItems: Int = 0,

    @SerializedName("items")
    val items: List<GoogleBookItem>? = null
)

data class GoogleBookItem(
    @SerializedName("id")
    val id: String? = null,

    @SerializedName("volumeInfo")
    val volumeInfo: GoogleBookVolumeInfo? = null
)

data class GoogleBookVolumeInfo(
    @SerializedName("title")
    val titre: String? = null,

    @SerializedName("subtitle")
    val sousTitre: String? = null,

    @SerializedName("authors")
    val auteurs: List<String>? = null,

    @SerializedName("imageLinks")
    val imageLinks: GoogleBookImageLinks? = null
) {
    /** Titre complet utilisé pour l'affichage et pour détecter le numéro de tome. */
    fun titreComplet(): String = listOfNotNull(titre, sousTitre).joinToString(" - ")
}

data class GoogleBookImageLinks(
    @SerializedName("thumbnail")
    val thumbnail: String? = null
) {
    /** Google Books renvoie parfois du http:// ; on force https pour éviter les soucis de sécurité réseau. */
    fun urlSecurisee(): String? = thumbnail?.replace("http://", "https://")
}
