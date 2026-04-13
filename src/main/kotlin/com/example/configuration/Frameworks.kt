package com.example.configuration

import com.example.HelloService
import com.example.application.handler.BuyCardHandler
import com.example.application.handler.CreateGameHandler
import com.example.application.handler.EndGameHandler
import com.example.application.handler.FlagLastCardHandler
import com.example.application.handler.JoinPlayerInGameHandler
import com.example.application.handler.KickPlayerHandler
import com.example.application.handler.PassTurnHandler
import com.example.application.handler.QuitGameHandler
import com.example.application.handler.SkipPlayerHandler
import com.example.application.handler.StartGameHandler
import com.example.application.handler.ThrowCardHandler
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

            // Adapters
            single<GameRepository> { InMemoryGameRepository() }

            // Handlers
            single { CreateGameHandler(get()) }
            single { JoinPlayerInGameHandler(get()) }
            single { EndGameHandler(get()) }
            single { StartGameHandler(get()) }
            single { FlagLastCardHandler(get()) }
            single { ThrowCardHandler(get()) }
            single { SkipPlayerHandler(get()) }
            single { BuyCardHandler(get()) }
            single { KickPlayerHandler(get()) }
            single { QuitGameHandler(get()) }
            single { PassTurnHandler(get()) }
        })
    }
}
