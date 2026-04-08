package com.example.application.handler

import MyMessages.passphrase_invalid
import MyMessages.punished_for_not_having_just_one_card
import com.example.application.command.FlagLastCard
import com.example.application.ports.inbound.CommandHandler
import com.example.application.ports.outbound.GameRepository
import io.ktor.server.plugins.BadRequestException

class FlagLastCardHandler(
    private val repository: GameRepository
) : CommandHandler<FlagLastCard, Unit> {
    override suspend fun handle(command: FlagLastCard) {
        val game = repository.findById(command.gameId)
        val player = game.findPlayer(command.playerName)

        require(player.passphrase == command.passphrase) { passphrase_invalid }
        if (player.cards.size == 1) {
            player.lastCard = true
        } else {
            val cards = game.stacks.getRandomCards(2)
            player.cards.addAll(cards)
            repository.save(game)
            throw BadRequestException(punished_for_not_having_just_one_card.toString())
        }
        repository.save(game)
    }
}
