package com.example

import com.example.application.handler.CreateGameHandler
import com.example.application.handler.EndGameHandler
import com.example.application.handler.JoinPlayerInGameHandler
import com.example.application.handler.StartGameHandler
import com.example.application.model.Games
import com.example.configuration.configureFrameworks
import com.example.configuration.configureHTTP
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import com.example.driver.http.endpointConfig as gameEndpoint

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

        gameEndpoint(createGameHandler, joinPlayerInGameHandler, endGameHandler, startGameHandler)
    }.start(wait = true)
}

fun Application.module() {
    install(ContentNegotiation) {
        json()
    }
}
