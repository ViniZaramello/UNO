package com.example.query.cards.by.gameIdplayer

import MyMessages.require_game_id
import MyMessages.require_player_name
import MyMessages.require_player_passphrase
import com.example.application.model.Card
import com.example.application.model.Games
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

class Endpoint(
    private val dao: Dao
) {
    fun query(gameId: String, playerName: String, passphrase: String): List<Card> {
        return dao.getPlayerHandCards(gameId, playerName, passphrase)
    }
}

fun Application.getPlayerHandCards(games: Games) {
    routing {
        get("/query/{gameId}/{playerName}/{passphrase}/cards") {
            val gameId = call.parameters["gameId"]?.trim()
            val playerName = call.parameters["playerName"]?.trim()
            val passphrase = call.parameters["passphrase"]?.trim()

            require(gameId != null) { require_game_id }
            require(playerName != null) { require_player_name }
            require(passphrase != null) { require_player_passphrase }

            val cards = Endpoint(Dao(games)).query(gameId, playerName, passphrase)

            call.respond(HttpStatusCode.OK, cards)
        }
    }
}
