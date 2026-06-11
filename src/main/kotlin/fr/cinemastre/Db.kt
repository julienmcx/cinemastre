package fr.cinemastre

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File


object Genres : IntIdTable("genre", "id_genre") {
    val libelle = varchar("libelle", 100).uniqueIndex()
}

object Films : IntIdTable("film", "id_film") {
    val titre = varchar("titre", 255)
    val annee = integer("annee").nullable()
    val note = double("note")
    val critique = text("critique").nullable()
    val dateVisionnage = date("date_visionnage")
    val genre = reference("id_genre", Genres).nullable()
}

fun initDatabase(path: String = System.getenv("DB_PATH") ?: "data/cinemastre.db") {
    File(path).parentFile?.mkdirs()
    Database.connect("jdbc:sqlite:$path", driver = "org.sqlite.JDBC")
    transaction {
        SchemaUtils.create(Genres, Films)
    }
}
