package com.example.application.handler

import com.example.application.command.JoinPlayerInGame
import com.example.application.model.GameStatus
import com.example.application.model.Games
import com.example.application.ports.inbound.CommandHandler

class JoinPlayerInGameHandler(
    private val games: Games
) : CommandHandler<JoinPlayerInGame, Unit> {
    override suspend fun handle(command: JoinPlayerInGame) {
        val (playerInfo, gameId) = command
        val gameInfo = games.findGameById(gameId)

        require(gameInfo.status == GameStatus.CREATED) { "Game $gameId is not available" }

        playerInfo.number = gameInfo.playerNumber()
        gameInfo.players.add(playerInfo)
    }
}
