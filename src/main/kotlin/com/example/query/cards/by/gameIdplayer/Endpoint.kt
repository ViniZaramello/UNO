package com.example.query.cards.by.gameIdplayer

import MyMessages.require_game_id
import MyMessages.require_player_name
import MyMessages.require_player_passphrase
import com.example.application.model.Games
import com.example.query.cards.by.gameIdplayer.response.Response
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import java.util.*

class Endpoint(
    private val dao: Dao
) {
    fun query(gameId: UUID, playerName: String, passphrase: String): Response {
        return dao.getPlayerHandCards(gameId, playerName, passphrase)
    }
}

fun Application.getPlayerHandCards() {
    routing {
        get("/query/{gameId}/{playerName}/{passphrase}/cards") {
            val gameId = UUID.fromString(call.parameters["gameId"]?.trim())
            val playerName = call.parameters["playerName"]?.trim()
            val passphrase = call.parameters["passphrase"]?.trim()

            require(gameId != null) { require_game_id }
            require(playerName != null) { require_player_name }
            require(passphrase != null) { require_player_passphrase }

            val response = Endpoint(Dao(Games)).query(gameId, playerName, passphrase)

            call.respond(HttpStatusCode.OK, response)
        }
    }
}
