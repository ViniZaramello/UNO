package com.example.application.handler

import com.example.application.command.JoinPlayerInGame
import com.example.application.ports.inbound.CommandHandler
import com.example.application.ports.outbound.Game
import com.example.application.ports.outbound.Player

class JoinPlayerInGameHandler(
    private val player: Player,
    private val game: Game
) : CommandHandler<JoinPlayerInGame, Unit> {
    override suspend fun handle(command: JoinPlayerInGame) {
        val(playerInfo, gameId) = command

        val gameInfo = game.find(gameId)
            ?: throw IllegalArgumentException("Game not found")

    }
}
