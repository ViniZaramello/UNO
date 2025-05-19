package com.example

import com.example.configuration.configureFrameworks
import com.example.configuration.configureHTTP
import com.example.driver.http.createGame.createGameRoute
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
//import org.jetbrains.exposed.sql.Database

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        configureFrameworks()
        configureHTTP()
        configureSerialization()
        configureRouting()
        endpointConfig()
    }.start(wait = true)
}

fun Application.module() {
    install(ContentNegotiation) {
        json()
    }
//    val database = Database.connect(
//        url = "jdbc:h2:mem:test",
//        user = "root",
//        driver = "org.h2.Driver",
//        password = ""
//    )
}

fun Application.endpointConfig(){
    createGameRoute()
}
