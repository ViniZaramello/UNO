package com.example.application.handler

import com.example.application.command.JoinPlayerInGame
import com.example.application.model.GameStatus
import com.example.application.model.Games
import com.example.application.ports.inbound.CommandHandler

class JoinPlayerInGameHandler(
    private val game: Games
) : CommandHandler<JoinPlayerInGame, Unit> {
    override suspend fun handle(command: JoinPlayerInGame) {
        val (playerInfo, gameId) = command

        val gameInfo =
            game.games.find { it.id.toString() == gameId } ?: throw IllegalArgumentException("Game $gameId not found")

        if (gameInfo.status == GameStatus.CREATED)
            throw IllegalArgumentException("Game $gameId is not available")

        playerInfo.number = gameInfo.playerNumber()
        gameInfo.players.add(playerInfo)
    }
}
