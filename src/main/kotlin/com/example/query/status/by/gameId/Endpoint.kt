package com.example.query.status.by.gameId

import MyMessages.require_game_id
import com.example.query.response.GameStats
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import java.util.UUID

class Endpoint(
    private val dao: Dao
) {
    fun query(gameId: UUID): GameStats {
        return dao.getGameStats(gameId)
    }
}

fun Application.getGameStats() {
    routing {
        get("/query/{gameId}/stats") {
            val gameId = UUID.fromString(call.parameters["gameId"]?.trim())

            require(gameId != null) { require_game_id }

            val gameStats = Endpoint(Dao()).query(gameId)

            call.respond(HttpStatusCode.OK, gameStats)
        }
    }
}