package com.example.application.handler


import com.example.application.command.EndGame
import com.example.application.model.GameStatus
import com.example.application.model.Games
import com.example.application.ports.inbound.CommandHandler

class EndGameHandler : CommandHandler<EndGame, Unit> {
    override suspend fun handle(command: EndGame) {
        //TODO: Foi recomendado pelo sonar de remover e adicionar uma nova instancia toda vez para evitar problema de performance
        command.gameId
        Games().games.find { it.id.toString() == command.gameId }?.let { game ->
            game.players.find { it.name == command.playerName }?.let { player ->
                if (player.isOwner())
                    game.status = GameStatus.FINISHED
                    Games().games.remove(game)
                    Games().games.add(game)

                throw IllegalArgumentException("Player ${command.playerName} is not the owner of game ${command.gameId}")
            } ?: throw IllegalArgumentException("Player ${command.playerName} not found in game ${command.gameId}")
        } ?: throw IllegalArgumentException("Game ${command.gameId} not found")
    }
}
//TODO:Melhorar chamada de exceptions, da para melhorar
