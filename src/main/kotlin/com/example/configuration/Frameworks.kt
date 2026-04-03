package com.example.configuration

import com.example.HelloService
import com.example.application.ports.outbound.GameRepository
import com.example.driven.InMemoryGameRepository
import io.ktor.server.application.Application
import io.ktor.server.application.install
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun Application.configureFrameworks() {
    install(Koin) {
        slf4jLogger()
        modules(module {
            single<HelloService> {
                HelloService {
                    println(environment.log.info("Hello, World!"))
                }
            }
            single<GameRepository> { InMemoryGameRepository() }
        })
    }
}
