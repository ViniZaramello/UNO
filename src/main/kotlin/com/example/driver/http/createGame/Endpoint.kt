package com.example.driver.http.createGame

import com.example.application.command.CreateGame
import com.example.application.ports.inbound.CommandHandler
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

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
                call.respond(HttpStatusCode.Created,"Room identifier code: $gameId" )
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

    return errorList
}
