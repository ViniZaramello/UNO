package com.example.driver.http.joinGame

import MyMessages.require_player_name
import MyMessages.require_game_id
import MyMessages.require_player_passphrase
import com.example.application.command.JoinPlayerInGame
import com.example.application.ports.inbound.CommandHandler
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing


class Endpoint(
    private val handler: CommandHandler<JoinPlayerInGame, Unit>
) {
    suspend fun command(request: JoinPlayerInGame) {
        handler.handle(request)
    }
}

fun Application.joinGameRoute(handler: CommandHandler<JoinPlayerInGame, Unit>) {

    routing {
        route("/game") {
            post("/joinGame") {
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
    if (request.name.isBlank())
        errorList.add(require_player_name.toString())

    if (request.passphrase.isBlank())
        errorList.add(require_player_passphrase.toString())

    if (request.gameId.isBlank())
        errorList.add(require_game_id.toString())

    return errorList
}
