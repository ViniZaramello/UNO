package com.example.application.handler


import com.example.application.command.EndGame
import com.example.application.model.GameStatus
import com.example.application.model.Games
import com.example.application.ports.inbound.CommandHandler

class EndGameHandler : CommandHandler<EndGame, Unit> {
    override suspend fun handle(command: EndGame) {
        val (gameId, playerName) = command
        val gamesInstance = Games()
        val game = gamesInstance.games.find { it.id.toString() == gameId }
            ?: throw IllegalArgumentException("Game $gameId not found")

        val player = game.players.find { it.name == playerName }
            ?: throw IllegalArgumentException("Player $playerName not found in game $gameId")

        if (!player.isOwner()) {
            game.status = GameStatus.FINISHED
            throw IllegalArgumentException("Player $playerName is not the owner of game $gameId")
        }
    }
}
//TODO:Melhorar chamada de exceptions, da para melhorar
