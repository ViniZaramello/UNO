package com.example.query.status.by.gameId

import MyMessages.require_game_id
import com.example.application.model.Games
import com.example.query.status.by.gameId.response.Response
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

class Endpoint(
    private val dao: Dao
) {
    fun query(gameId: String): Response {
        return dao.getGameStats(gameId)
    }
}

fun Application.getGameStats(games: Games) {
    routing {
        get("/query/{gameId}/stats") {
            val gameId = call.parameters["gameId"]?.trim()

            require(gameId != null) { require_game_id }

            val gameStats = Endpoint(Dao(games)).query(gameId)

            call.respond(HttpStatusCode.OK, gameStats)
        }
    }
}