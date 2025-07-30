package com.example.application.handler

import MyMessages.game_is_not_available
import MyMessages.name_in_use
import com.example.application.command.JoinPlayerInGame
import com.example.application.model.GameStatus.CREATED
import com.example.application.model.GameStatus.WAITING
import com.example.application.model.Games
import com.example.application.ports.inbound.CommandHandler

class JoinPlayerInGameHandler : CommandHandler<JoinPlayerInGame, Unit> {
    override suspend fun handle(command: JoinPlayerInGame) {
        val (playerInfo, gameId) = command
        val gameInfo = Games.findGameById(gameId)

        require(gameInfo.status == CREATED || gameInfo.status == WAITING) { game_is_not_available(gameInfo.id) }
        require(gameInfo.verifyPlayerExists(playerInfo)) { name_in_use(playerInfo.name) }

        playerInfo.number = gameInfo.playerNumber()
        gameInfo.players.add(playerInfo)
    }
}
