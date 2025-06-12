package com.example.application.handler

import MyMessages.game_is_not_available
import MyMessages.name_in_use
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

        require(gameInfo.status == GameStatus.CREATED) { game_is_not_available(gameInfo) }
        require(gameInfo.verifyPlayerExists(playerInfo)) { name_in_use(playerInfo.name) }

        playerInfo.number = gameInfo.playerNumber()
        gameInfo.players.add(playerInfo)
    }
}
