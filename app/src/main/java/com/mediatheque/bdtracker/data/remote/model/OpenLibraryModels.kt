package com.mediatheque.bdtracker.data.remote.model

import com.google.gson.annotations.SerializedName

/**
 * Réponse brute de l'endpoint de recherche Open Library :
 * https://openlibrary.org/search.json?q=...
 */
data class OpenLibrarySearchResponse(
    @SerializedName("docs")
    val documents: List<OpenLibraryDoc> = emptyList()
)

data class OpenLibraryDoc(
    @SerializedName("key")
    val key: String? = null, // ex: "/works/OL123W"

    @SerializedName("title")
    val titre: String? = null,

    @SerializedName("author_name")
    val auteurs: List<String>? = null,

    @SerializedName("cover_i")
    val idCouverture: Int? = null,

    @SerializedName("first_publish_year")
    val anneePremierePublication: Int? = null
) {
    /**
     * Construit l'URL de l'image de couverture à taille moyenne.
     * Retourne null si aucune couverture n'est référencée.
     */
    fun urlCouverture(): String? {
        return idCouverture?.let { "https://covers.openlibrary.org/b/id/$it-M.jpg" }
    }
}
