package com.example.driver.http

import com.example.application.command.BuyCard
import com.example.application.command.CreateGame
import com.example.application.command.EndGame
import com.example.application.command.FlagLastCard
import com.example.application.command.JoinPlayerInGame
import com.example.application.command.KickPlayer
import com.example.application.command.PassTurn
import com.example.application.command.QuitGame
import com.example.application.command.SkipPlayer
import com.example.application.command.StartGame
import com.example.application.command.ThrowCard
import com.example.application.model.Card
import com.example.application.ports.inbound.CommandHandler
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

fun Application.gameEndpointConfig(
    createGame: CommandHandler<CreateGame, String>,
    joinPlayerInGame: CommandHandler<JoinPlayerInGame, Unit>,
    endGame: CommandHandler<EndGame, Unit>,
    startGame: CommandHandler<StartGame, Unit>,
    skipPlayer: CommandHandler<SkipPlayer, Unit>,
    kickPlayer: CommandHandler<KickPlayer, Unit>,
) {
    createGameRoute(createGame)
    joinGameRoute(joinPlayerInGame)
    endGameRoute(endGame)
    startGameRoute(startGame)
    skipPlayerRoute(skipPlayer)
    kickPlayerRoute(kickPlayer)
}

fun Application.playerEndpointConfig(
    flagLastCard: CommandHandler<FlagLastCard, Unit>,
    throwCard: CommandHandler<ThrowCard, Unit>,
    buyCard: CommandHandler<BuyCard, Card>,
    quitGame: CommandHandler<QuitGame, Unit>,
    passTurn: CommandHandler<PassTurn, Unit>
) {
    flagLastCardRoute(flagLastCard)
    throwCardRoute(throwCard)
    buyCardRoute(buyCard)
    quitGameRoute(quitGame)
    passTurnRoute(passTurn)
}