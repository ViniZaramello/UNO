package com.example.driver.http.createGame

import MyMessages.game_info_id
import MyMessages.require_player_name
import MyMessages.require_player_passphrase
import com.example.application.command.CreateGame
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
    private val handler: CommandHandler<CreateGame, String>
) {
    suspend fun command(request: CreateGame): String {
        return handler.handle(request)
    }
}

fun Application.createGameRoute(handler: CommandHandler<CreateGame, String>) {

    routing {
        route("/game") {
            post("/createGame") {

                val request = call.receive<Request>()
                val command = request.toCommand()
                val errorList: List<String> = validateRequest(request)
                if (errorList.isNotEmpty())
                    return@post call.respondText(
                        errorList.joinToString(", "),
                        status = HttpStatusCode.BadRequest
                    )
                val gameId = Endpoint(handler).command(command)
                call.respond(HttpStatusCode.Created, Response(gameId))
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

    return errorList
}
