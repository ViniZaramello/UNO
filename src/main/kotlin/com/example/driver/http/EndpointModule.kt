package com.example.driver.http

import com.example.driver.http.createGame.createGameRoute
import io.ktor.server.application.*

fun Application.endpointConfig(){
    createGameRoute()
}