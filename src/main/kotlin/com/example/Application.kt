package com.example

import com.example.application.handler.BuyCardHandler
import com.example.application.handler.CreateGameHandler
import com.example.application.handler.EndGameHandler
import com.example.application.handler.FlagLastCardHandler
import com.example.application.handler.JoinPlayerInGameHandler
import com.example.application.handler.KickPlayerHandle
import com.example.application.handler.QuitGameHandler
import com.example.application.handler.SkipPlayerHandler
import com.example.application.handler.StartGameHandler
import com.example.application.handler.ThrowCardHandler
import com.example.application.model.Games
import com.example.configuration.configureExceptionHandling
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
        configureExceptionHandling()
        configureFrameworks()
        configureHTTP()
        configureSerialization()
        configureRouting()

        val games = Games

        val createGameHandler = CreateGameHandler()
        val joinPlayerInGameHandler = JoinPlayerInGameHandler(games)
        val endGameHandler = EndGameHandler()
        val startGameHandler = StartGameHandler()
        val flagLastCardHandler = FlagLastCardHandler(games)
        val throwCardHandler = ThrowCardHandler(games)
        val skipPlayerHandler = SkipPlayerHandler(games)
        val buyCardHandler = BuyCardHandler(games)
        val kickPlayer = KickPlayerHandle(games)
        val quitGame = QuitGameHandler(games)

        commandEndpointConfig(
            createGame = createGameHandler,
            joinPlayerInGame = joinPlayerInGameHandler,
            endGame = endGameHandler,
            startGame = startGameHandler,
            flagLastCard = flagLastCardHandler,
            throwCard = throwCardHandler,
            skipPlayer = skipPlayerHandler,
            buyCard = buyCardHandler,
            kickPlayer = kickPlayer,
            quitGame = quitGame
        )
        queryEndpointConfig(games)
    }.start(wait = true)
}

fun Application.module() {
    install(ContentNegotiation) {
        json()
    }
}
