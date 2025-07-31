package com.example.driver.http.skipPlayer

import MyMessages.game_info_id
import MyMessages.require_game_id
import MyMessages.require_player_name
import MyMessages.require_player_passphrase
import MyMessages.require_target_player_not_found
import com.example.application.command.SkipPlayer
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
    private val handler: CommandHandler<SkipPlayer, Unit>
) {
    suspend fun command(request: SkipPlayer) {
        handler.handle(request)
    }
}

fun Application.skipPlayerRoute(handler: CommandHandler<SkipPlayer, Unit>) {

    routing {
        route("/game") {
            post("/skipPlayer") {

                val request = call.receive<Request>()
                val command = request.toCommand()
                val errorList: List<String> = validateRequest(request)
                if (errorList.isNotEmpty())
                    return@post call.respondText(
                        errorList.joinToString(", "),
                        status = HttpStatusCode.BadRequest
                    )
                val gameId = Endpoint(handler).command(command)
                call.respond(HttpStatusCode.OK, game_info_id(gameId))
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


    if (request.targetPlayerName.isBlank())
        errorList.add(require_target_player_not_found.toString())

    return errorList
}
