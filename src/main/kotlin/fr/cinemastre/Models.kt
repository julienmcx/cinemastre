package fr.cinemastre

import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class FilmDto(
    val id: Int? = null,
    val title: String = "",
    val year: Int? = null,
    val genre: String? = null,
    val rating: Double = 0.0,
    val review: String? = null,
    val watchedAt: String? = null,
) {
    fun isHighlyRated(): Boolean = rating >= 4.0

    fun validate(): List<String> {
        val errors = mutableListOf<String>()
        if (title.isBlank()) errors += "Le titre est obligatoire"
        if (title.length > 255) errors += "Le titre ne peut pas dépasser 255 caractères"
        if (rating < 0.5 || rating > 5.0) errors += "La note doit être comprise entre 0,5 et 5"
        year?.let {
            if (it < 1888 || it > 2100) errors += "L'année doit être comprise entre 1888 et 2100"
        }
        watchedAt?.let {
            val parsed = runCatching { LocalDate.parse(it) }.getOrNull()
            if (parsed == null) {
                errors += "La date de visionnage est invalide (format attendu : AAAA-MM-JJ)"
            } else if (parsed.isAfter(LocalDate.now())) {
                errors += "La date ne peut pas être dans le futur"
            }
        }
        return errors
    }
}

@Serializable
data class ErrorResponse(val errors: List<String>)

@Serializable
data class Summary(val totalFilms: Int, val averageRating: Double, val highlyRatedCount: Int)

@Serializable
data class GenreStat(val genre: String, val count: Int, val averageRating: Double)

@Serializable
data class TopFilm(val title: String, val rating: Double, val year: Int? = null)

@Serializable
data class Health(val status: String)
