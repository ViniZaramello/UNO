package com.example.driver.http

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
import com.example.driver.http.buyCard.buyCardRoute
import com.example.driver.http.createGame.createGameRoute
import com.example.driver.http.endGame.endGameRoute
import com.example.driver.http.flagLastCard.flagLastCardRoute
import com.example.driver.http.joinGame.joinGameRoute
import com.example.driver.http.kickPlayer.kickPlayerRoute
import com.example.driver.http.passTurn.passTurnRoute
import com.example.driver.http.quitGame.quitGameRoute
import com.example.driver.http.skipPlayer.skipPlayerRoute
import com.example.driver.http.startGame.startGameRoute
import com.example.driver.http.throwCard.throwCardRoute
import io.ktor.server.application.Application
import org.koin.ktor.ext.get

fun Application.gameEndpointConfig() {
    createGameRoute(get<CreateGameHandler>())
    joinGameRoute(get<JoinPlayerInGameHandler>())
    endGameRoute(get<EndGameHandler>())
    startGameRoute(get<StartGameHandler>())
    skipPlayerRoute(get<SkipPlayerHandler>())
    kickPlayerRoute(get<KickPlayerHandler>())
}

fun Application.playerEndpointConfig() {
    flagLastCardRoute(get<FlagLastCardHandler>())
    throwCardRoute(get<ThrowCardHandler>())
    buyCardRoute(get<BuyCardHandler>())
    quitGameRoute(get<QuitGameHandler>())
    passTurnRoute(get<PassTurnHandler>())
}