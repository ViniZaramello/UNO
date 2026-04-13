package com.example

import com.example.configuration.configureExceptionHandling
import com.example.configuration.configureFrameworks
import com.example.configuration.configureHTTP
import com.example.driver.http.gameEndpointConfig
import com.example.driver.http.playerEndpointConfig
import com.example.query.queryEndpointConfig
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation

fun main() {
    val port = System.getenv("PORT")?.toInt() ?: 8080
    embeddedServer(Netty, port = port, host = "0.0.0.0") {
        configureExceptionHandling()
        configureFrameworks()
        configureHTTP()
        configureSerialization()
        configureRouting()

        gameEndpointConfig()
        playerEndpointConfig()
        queryEndpointConfig()
    }.start(wait = true)
}

fun Application.module() {
    install(ContentNegotiation) {
        json()
    }
}
