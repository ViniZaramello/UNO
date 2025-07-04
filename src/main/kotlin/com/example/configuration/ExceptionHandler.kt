package com.example.configuration

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond

fun Application.configureExceptionHandling() {
    install(StatusPages) {
        exception<IllegalArgumentException> { call, cause ->
            call.respond(HttpStatusCode.BadRequest, mapOf("message" to (cause.message ?: "Invalid request")))
        }
        exception<Throwable> { call, _ ->
            call.respond(HttpStatusCode.InternalServerError, mapOf("message" to "An unexpected error occurred"))
        }
    }
}