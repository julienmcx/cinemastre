package fr.cinemastre

import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StatsTest {

    private val sample = listOf(
        FilmDto(title = "Dune", genre = "Sci-Fi", rating = 4.5, year = 2021),
        FilmDto(title = "Arrival", genre = "Sci-Fi", rating = 5.0, year = 2016),
        FilmDto(title = "Cats", genre = "Comédie", rating = 1.5, year = 2019),
    )

    @Test
    fun `summary compte les films et calcule la moyenne`() {
        val s = Stats.summary(sample)
        assertEquals(3, s.totalFilms)
        assertEquals(3.7, s.averageRating, 0.0001) // (4.5 + 5 + 1.5) / 3 = 3.67 -> 3.7
        assertEquals(2, s.highlyRatedCount)
    }

    @Test
    fun `summary renvoie des zeros sans aucun film`() {
        assertEquals(Summary(0, 0.0, 0), Stats.summary(emptyList()))
    }

    @Test
    fun `byGenre regroupe et trie par nombre decroissant`() {
        val r = Stats.byGenre(sample)
        assertEquals("Sci-Fi", r[0].genre)
        assertEquals(2, r[0].count)
        assertEquals(4.8, r[0].averageRating, 0.0001)
        assertEquals("Comédie", r[1].genre)
    }

    @Test
    fun `byGenre range les films sans genre dans Inconnu`() {
        val r = Stats.byGenre(listOf(FilmDto(title = "X", rating = 3.0)))
        assertEquals("Inconnu", r[0].genre)
    }

    @Test
    fun `top renvoie les mieux notes en premier`() {
        val r = Stats.top(sample, 2)
        assertEquals(2, r.size)
        assertEquals("Arrival", r[0].title)
        assertEquals("Dune", r[1].title)
    }
}

class FilmDtoTest {

    @Test
    fun `isHighlyRated a partir de 4 etoiles`() {
        assertTrue(FilmDto(title = "Dune", rating = 4.0).isHighlyRated())
        assertTrue(FilmDto(title = "Arrival", rating = 5.0).isHighlyRated())
        assertFalse(FilmDto(title = "Cats", rating = 3.5).isHighlyRated())
    }

    @Test
    fun `un film valide ne produit aucune erreur`() {
        assertTrue(FilmDto(title = "Dune", rating = 4.5, year = 2021).validate().isEmpty())
    }

    @Test
    fun `le titre est obligatoire`() {
        val errors = FilmDto(title = "  ", rating = 3.0).validate()
        assertTrue(errors.any { it.contains("titre") })
    }

    @Test
    fun `la note est bornee entre 0_5 et 5`() {
        assertTrue(FilmDto(title = "X", rating = 6.0).validate().any { it.contains("note") })
        assertTrue(FilmDto(title = "X", rating = 0.0).validate().any { it.contains("note") })
    }

    @Test
    fun `la date ne peut pas etre dans le futur`() {
        val demain = LocalDate.now().plusDays(1).toString()
        val errors = FilmDto(title = "X", rating = 3.0, watchedAt = demain).validate()
        assertTrue(errors.any { it.contains("futur") })
    }

    @Test
    fun `la date du jour est acceptee`() {
        val aujourdHui = LocalDate.now().toString()
        assertTrue(FilmDto(title = "X", rating = 3.0, watchedAt = aujourdHui).validate().isEmpty())
    }
}
