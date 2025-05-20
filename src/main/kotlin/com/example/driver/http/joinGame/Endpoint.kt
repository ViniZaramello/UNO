package com.example.driver.http.joinGame

import com.example.application.command.JoinPlayerInGame
import com.example.application.ports.inbound.CommandHandler
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*


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
        errorList.add("Name is required")

    if (request.passphrase.isBlank())
        errorList.add("Passphrase is required")

    if (request.gameId.isBlank())
        errorList.add("gameId is required")

    return errorList
}
