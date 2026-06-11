package fr.cinemastre

import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.leftJoin
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.time.LocalDate

private fun rowToDto(row: ResultRow) = FilmDto(
    id = row[Films.id].value,
    title = row[Films.titre],
    year = row[Films.annee],
    genre = row.getOrNull(Genres.libelle),
    rating = row[Films.note],
    review = row[Films.critique],
    watchedAt = row[Films.dateVisionnage].toString(),
)

/** Retrouve le genre par son libellé, ou le crée s'il n'existe pas encore. */
private fun genreIdFor(label: String?): EntityID<Int>? {
    if (label.isNullOrBlank()) return null
    val existing = Genres.selectAll().where { Genres.libelle eq label }.firstOrNull()
    return existing?.get(Genres.id) ?: Genres.insertAndGetId { it[libelle] = label }
}

private fun fetchAll(): List<FilmDto> = transaction {
    Films.leftJoin(Genres)
        .selectAll()
        .orderBy(Films.dateVisionnage, SortOrder.DESC)
        .map(::rowToDto)
}

private fun fetchOne(id: Int): FilmDto? = transaction {
    Films.leftJoin(Genres)
        .selectAll()
        .where { Films.id eq id }
        .map(::rowToDto)
        .firstOrNull()
}

fun Route.filmRoutes() = route("/api/logs") {

    get {
        call.respond(fetchAll())
    }

    get("{id}") {
        val id = call.parameters["id"]?.toIntOrNull()
            ?: return@get call.respond(HttpStatusCode.BadRequest)
        val film = fetchOne(id)
            ?: return@get call.respond(HttpStatusCode.NotFound)
        call.respond(film)
    }

    post {
        val dto = call.receive<FilmDto>()
        val errors = dto.validate()
        if (errors.isNotEmpty()) {
            return@post call.respond(HttpStatusCode.UnprocessableEntity, ErrorResponse(errors))
        }
        val newId = transaction {
            val gid = genreIdFor(dto.genre)
            Films.insertAndGetId {
                it[titre] = dto.title
                it[annee] = dto.year
                it[note] = dto.rating
                it[critique] = dto.review
                it[dateVisionnage] = dto.watchedAt?.let(LocalDate::parse) ?: LocalDate.now()
                it[genre] = gid
            }.value
        }
        call.respond(HttpStatusCode.Created, fetchOne(newId)!!)
    }

    put("{id}") {
        val id = call.parameters["id"]?.toIntOrNull()
            ?: return@put call.respond(HttpStatusCode.BadRequest)
        val dto = call.receive<FilmDto>()
        val errors = dto.validate()
        if (errors.isNotEmpty()) {
            return@put call.respond(HttpStatusCode.UnprocessableEntity, ErrorResponse(errors))
        }
        val count = transaction {
            val gid = genreIdFor(dto.genre)
            Films.update({ Films.id eq id }) {
                it[titre] = dto.title
                it[annee] = dto.year
                it[note] = dto.rating
                it[critique] = dto.review
                dto.watchedAt?.let { d -> it[dateVisionnage] = LocalDate.parse(d) }
                it[genre] = gid
            }
        }
        if (count == 0) return@put call.respond(HttpStatusCode.NotFound)
        call.respond(fetchOne(id)!!)
    }

    delete("{id}") {
        val id = call.parameters["id"]?.toIntOrNull()
            ?: return@delete call.respond(HttpStatusCode.BadRequest)
        val deleted = transaction { Films.deleteWhere { Films.id eq id } }
        if (deleted == 0) return@delete call.respond(HttpStatusCode.NotFound)
        call.respond(HttpStatusCode.NoContent)
    }
}

fun Route.statsRoutes() = route("/stats") {
    get("/summary") { call.respond(Stats.summary(fetchAll())) }
    get("/by-genre") { call.respond(Stats.byGenre(fetchAll())) }
    get("/top") { call.respond(Stats.top(fetchAll(), 5)) }
}
