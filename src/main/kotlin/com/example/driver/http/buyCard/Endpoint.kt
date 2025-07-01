package com.example.driver.http.buyCard

import MyMessages.require_game_id
import MyMessages.require_player_name
import MyMessages.require_player_passphrase
import com.example.application.command.BuyCard
import com.example.application.model.Card
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
    private val handler: CommandHandler<BuyCard, Card>
) {
    suspend fun command(request: BuyCard): Card {
        return handler.handle(request)
    }
}

fun Application.buyCardRoute(handler: CommandHandler<BuyCard, Card>) {

    routing {
        route("/player") {
            post("/buyCard") {

                val request = call.receive<Request>()
                val command = request.toCommand()
                val errorList: List<String> = validateRequest(request)
                if (errorList.isNotEmpty())
                    return@post call.respondText(
                        errorList.joinToString(", "),
                        status = HttpStatusCode.BadRequest
                    )
                val card = Endpoint(handler).command(command)
                call.respond(HttpStatusCode.Accepted, card)
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
