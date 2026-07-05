package com.mediatheque.bdtracker.util

/**
 * Détecte automatiquement un numéro de tome à partir d'un titre de livre.
 * Gère les formats les plus courants utilisés par les éditeurs de BD francophones :
 *   "Les Légendaires - Tome 12 - Le crépuscule des dieux"  → 12
 *   "Les Légendaires T12"                                  → 12
 *   "One Piece Vol. 3"                                     → 3
 *   "Astérix #5"                                           → 5
 */
object TomeNumeroExtractor {

    private val motifs = listOf(
        Regex("""(?i)tome\s*n?°?\s*(\d{1,3})"""),
        Regex("""(?i)\bt\.?\s?(\d{1,3})\b"""),
        Regex("""(?i)vol(?:ume)?\.?\s*(\d{1,3})"""),
        Regex("""#(\d{1,3})""")
    )

    /** Retourne le numéro détecté, ou null si aucun motif ne correspond. */
    fun extraire(titre: String): Int? {
        for (motif in motifs) {
            val resultat = motif.find(titre)
            if (resultat != null) {
                return resultat.groupValues[1].toIntOrNull()
            }
        }
        return null
    }
}
