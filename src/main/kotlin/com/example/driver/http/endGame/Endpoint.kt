package com.example.driver.http.endGame

import MyMessages.require_game_id
import MyMessages.require_player_name
import MyMessages.require_player_passphrase
import com.example.application.command.EndGame
import com.example.application.ports.inbound.CommandHandler
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing

class Endpoint(
    private val handler: CommandHandler<EndGame, Unit>
) {
    suspend fun command(request: EndGame) {
        handler.handle(request)
    }
}

fun Application.endGameRoute(handler: CommandHandler<EndGame, Unit>) {
    install(ContentNegotiation) {
        json()
    }
    routing {
        route("/game") {
            post("/endGame") {

                val request = call.receive<Request>()
                val command = request.toCommand()
                val errorList: List<String> = validateRequest(request)
                if (errorList.isNotEmpty())
                    return@post call.respondText(
                        errorList.joinToString(", "),
                        status = HttpStatusCode.BadRequest
                    )
                Endpoint(handler).command(command)
                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}

fun validateRequest(request: Request): MutableList<String> {
    val errorList: MutableList<String> = mutableListOf()
    if (request.gameId.isBlank())
        errorList.add(require_game_id.toString())

    if (request.playerName.isBlank())
        errorList.add(require_player_name.toString())

    if (request.passphrase.isBlank())
        errorList.add(require_player_passphrase.toString())
    return errorList
}
