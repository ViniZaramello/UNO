package com.example.driver.http.createGame

import com.example.application.handler.CreateGameHandler
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*


fun Application.createGameRoute() {
    routing {
        route("/game") {
            post("/createGame") {
                val request = call.receive<Request>()
                val command = request.toCommand()
                val errorList = validateRequest(request)
                if (errorList.isNotEmpty())
                    return@post call.respondText(
                        errorList.joinToString(", "),
                        status = io.ktor.http.HttpStatusCode.BadRequest
                    )
                val gameId = CreateGameHandler().handle(command)
                call.respondText("Room identifier code: $gameId")
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