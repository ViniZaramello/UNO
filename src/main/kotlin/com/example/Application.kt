package com.example

import com.example.application.handler.BuyCardHandler
import com.example.application.handler.CreateGameHandler
import com.example.application.handler.EndGameHandler
import com.example.application.handler.FlagLastCardHandler
import com.example.application.handler.JoinPlayerInGameHandler
import com.example.application.handler.SkipPlayerHandler
import com.example.application.handler.StartGameHandler
import com.example.application.handler.ThrowCardHandler
import com.example.application.model.Games
import com.example.configuration.configureFrameworks
import com.example.configuration.configureHTTP
import com.example.driver.http.commandEndpointConfig
import com.example.query.queryEndpointConfig
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        configureFrameworks()
        configureHTTP()
        configureSerialization()
        configureRouting()

        val games = Games()

        val createGameHandler = CreateGameHandler(games)
        val joinPlayerInGameHandler = JoinPlayerInGameHandler(games)
        val endGameHandler = EndGameHandler(games)
        val startGameHandler = StartGameHandler(games)
        val flagLastCardHandler = FlagLastCardHandler(games)
        val throwCardHandler = ThrowCardHandler(games)
        val skipPlayerHandler = SkipPlayerHandler(games)
        val buyCardHandler = BuyCardHandler(games)

        commandEndpointConfig(
            createGame = createGameHandler,
            joinPlayerInGame = joinPlayerInGameHandler,
            endGame = endGameHandler,
            startGame = startGameHandler,
            flagLastCard = flagLastCardHandler,
            throwCard = throwCardHandler,
            skipPlayer = skipPlayerHandler,
            buyCard = buyCardHandler
        )
        queryEndpointConfig(games)
    }.start(wait = true)
}

fun Application.module() {
    install(ContentNegotiation) {
        json()
    }
}
