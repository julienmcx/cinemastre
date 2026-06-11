package fr.cinemastre

import kotlin.math.roundToInt

object Stats {

    private fun round1(x: Double): Double = (x * 10).roundToInt() / 10.0

    fun summary(films: List<FilmDto>): Summary = Summary(
        totalFilms = films.size,
        averageRating = if (films.isEmpty()) 0.0 else round1(films.sumOf { it.rating } / films.size),
        highlyRatedCount = films.count { it.isHighlyRated() },
    )

    fun byGenre(films: List<FilmDto>): List<GenreStat> =
        films
            .groupBy { it.genre ?: "Inconnu" }
            .map { (genre, list) ->
                GenreStat(
                    genre = genre,
                    count = list.size,
                    averageRating = round1(list.sumOf { it.rating } / list.size),
                )
            }
            .sortedByDescending { it.count }

    fun top(films: List<FilmDto>, limit: Int = 5): List<TopFilm> =
        films
            .sortedByDescending { it.rating }
            .take(limit)
            .map { TopFilm(title = it.title, rating = it.rating, year = it.year) }
}
