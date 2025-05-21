package com.example.driver.http.endGame

import com.example.application.command.EndGame
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
    private val handler: CommandHandler<EndGame, Unit>
) {
    suspend fun command(request: EndGame) {
        handler.handle(request)
    }
}

fun Application.endGameRoute(handler: CommandHandler<EndGame, Unit>) {

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
                val gameId = Endpoint(handler).command(command)
                call.respond(HttpStatusCode.Created,"Room identifier code: $gameId" )
            }
        }
    }
}

fun validateRequest(request: Request): MutableList<String> {
    val errorList: MutableList<String> = mutableListOf()
    if (request.gameId.isBlank())
        errorList.add("Game ID is required")

    if (request.playerName.isBlank())
        errorList.add("Name is required")

    return errorList
}
