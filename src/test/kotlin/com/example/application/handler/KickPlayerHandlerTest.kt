package com.example.application.handler

import com.example.application.command.KickPlayer
import com.example.application.factories.Domain
import com.example.application.model.GameStatus
import com.example.application.model.Player
import com.example.application.model.PlayerStatus
import com.example.application.ports.outbound.GameRepository
import com.example.configuration.NotFoundException
import io.github.serpro69.kfaker.Faker
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.UUID

class KickPlayerHandlerTest {

    private val faker = Faker()
    private val repository = mockk<GameRepository>()
    private lateinit var handler: KickPlayerHandler

    @BeforeEach
    fun setup() {
        handler = KickPlayerHandler(repository)
    }

    private fun threePlayerGame(status: GameStatus = GameStatus.PLAYING): Triple<com.example.application.model.Game, Player, Player> {
        val owner = Domain.player(name = faker.name.firstName(), passphrase = faker.name.lastName(), owner = true, number = 1, status = PlayerStatus.PLAYING)
        val target = Domain.player(name = faker.name.firstName(), passphrase = faker.name.lastName(), owner = false, number = 2, status = PlayerStatus.PLAYING)
        val third = Domain.player(name = faker.name.firstName(), passphrase = faker.name.lastName(), owner = false, number = 3, status = PlayerStatus.PLAYING)
        val game = Domain.game(players = mutableListOf(owner, target, third), status = status)
        return Triple(game, owner, target)
    }

    @Test
    fun `should remove target player from game and save`() = runBlocking {
        val (game, owner, target) = threePlayerGame()

        every { repository.findById(game.id) } returns game
        every { repository.save(game) } returns game

        handler.handle(KickPlayer(gameId = game.id, playerName = owner.name, passphrase = owner.passphrase, playerTarget = target.name))

        game.players.any { it.name == target.name } shouldBe false
        verify(exactly = 1) { repository.save(game) }
    }

    @Test
    fun `should reorder player numbers after kick`() = runBlocking {
        val (game, owner, target) = threePlayerGame()

        every { repository.findById(game.id) } returns game
        every { repository.save(game) } returns game

        handler.handle(KickPlayer(gameId = game.id, playerName = owner.name, passphrase = owner.passphrase, playerTarget = target.name))

        game.players.size shouldBe 2
        game.players[0].number shouldBe 1
        game.players[1].number shouldBe 2
    }

    @Test
    fun `should transfer target cards back to deck before removing`() = runBlocking {
        val (game, owner, target) = threePlayerGame()
        game.initialCards()
        val deckSizeBeforeKick = game.stacks.cardsInDeck.size + target.cards.size

        every { repository.findById(game.id) } returns game
        every { repository.save(game) } returns game

        handler.handle(KickPlayer(gameId = game.id, playerName = owner.name, passphrase = owner.passphrase, playerTarget = target.name))

        game.stacks.cardsInDeck.size shouldBe deckSizeBeforeKick
    }

    @Test
    fun `should pass turn when kicked player has the current turn`() = runBlocking {
        val owner = Domain.player(name = faker.name.firstName(), passphrase = faker.name.lastName(), owner = true, number = 1, status = PlayerStatus.PLAYING)
        val target = Domain.player(name = faker.name.firstName(), passphrase = faker.name.lastName(), owner = false, number = 2, status = PlayerStatus.PLAYING)
        val third = Domain.player(name = faker.name.firstName(), passphrase = faker.name.lastName(), owner = false, number = 3, status = PlayerStatus.PLAYING)
        val game = Domain.game(players = mutableListOf(owner, target, third), status = GameStatus.PLAYING, playerTurn = 2)

        every { repository.findById(game.id) } returns game
        every { repository.save(game) } returns game

        handler.handle(KickPlayer(gameId = game.id, playerName = owner.name, passphrase = owner.passphrase, playerTarget = target.name))

        // Turn should have moved away from the kicked player (number 2 → next)
        game.players.any { it.name == target.name } shouldBe false
    }

    @Test
    fun `should not change turn when kicked player does not have the current turn`() = runBlocking {
        val owner = Domain.player(name = faker.name.firstName(), passphrase = faker.name.lastName(), owner = true, number = 1, status = PlayerStatus.PLAYING)
        val target = Domain.player(name = faker.name.firstName(), passphrase = faker.name.lastName(), owner = false, number = 2, status = PlayerStatus.PLAYING)
        val third = Domain.player(name = faker.name.firstName(), passphrase = faker.name.lastName(), owner = false, number = 3, status = PlayerStatus.PLAYING)
        // Turn is player 1 (owner), not the target (player 2)
        val game = Domain.game(players = mutableListOf(owner, target, third), status = GameStatus.PLAYING, playerTurn = 1)

        every { repository.findById(game.id) } returns game
        every { repository.save(game) } returns game

        handler.handle(KickPlayer(gameId = game.id, playerName = owner.name, passphrase = owner.passphrase, playerTarget = target.name))

        // playerTurn stays at 1 (owner's turn, not modified)
        game.playerTurn shouldBe 1
    }

    @Test
    fun `should throw NotFoundException when game does not exist`() = runBlocking<Unit> {
        val randomId = UUID.randomUUID()
        every { repository.findById(randomId) } throws NotFoundException("Game $randomId was not found.")

        assertThrows<NotFoundException> {
            runBlocking {
                handler.handle(KickPlayer(gameId = randomId, playerName = faker.name.firstName(), passphrase = faker.name.lastName(), playerTarget = faker.name.firstName()))
            }
        }

        verify(exactly = 0) { repository.save(any()) }
    }

    @Test
    fun `should throw NotFoundException when kicker player does not exist in game`() = runBlocking<Unit> {
        val (game, _, target) = threePlayerGame()
        every { repository.findById(game.id) } returns game

        assertThrows<NotFoundException> {
            runBlocking { handler.handle(KickPlayer(gameId = game.id, playerName = "ghost_player", passphrase = "any", playerTarget = target.name)) }
        }

        verify(exactly = 0) { repository.save(any()) }
    }

    @Test
    fun `should throw NotFoundException when target player does not exist in game`() = runBlocking<Unit> {
        val (game, owner, _) = threePlayerGame()
        every { repository.findById(game.id) } returns game

        assertThrows<NotFoundException> {
            runBlocking { handler.handle(KickPlayer(gameId = game.id, playerName = owner.name, passphrase = owner.passphrase, playerTarget = "ghost_target")) }
        }

        verify(exactly = 0) { repository.save(any()) }
    }

    @Test
    fun `should throw IllegalArgumentException when passphrase is wrong`() = runBlocking<Unit> {
        val (game, owner, target) = threePlayerGame()
        every { repository.findById(game.id) } returns game

        assertThrows<IllegalArgumentException> {
            runBlocking { handler.handle(KickPlayer(gameId = game.id, playerName = owner.name, passphrase = "wrong_passphrase", playerTarget = target.name)) }
        }

        verify(exactly = 0) { repository.save(any()) }
    }

    @Test
    fun `should throw IllegalArgumentException when player is not owner`() = runBlocking<Unit> {
        val (game, _, target) = threePlayerGame()
        every { repository.findById(game.id) } returns game

        assertThrows<IllegalArgumentException> {
            runBlocking { handler.handle(KickPlayer(gameId = game.id, playerName = target.name, passphrase = target.passphrase, playerTarget = target.name)) }
        }

        verify(exactly = 0) { repository.save(any()) }
    }

    @Test
    fun `should not save when an error occurs`() = runBlocking<Unit> {
        val (game, _, _) = threePlayerGame()
        every { repository.findById(game.id) } returns game

        assertThrows<NotFoundException> {
            runBlocking { handler.handle(KickPlayer(gameId = game.id, playerName = "ghost_player", passphrase = "any", playerTarget = "ghost_target")) }
        }

        verify(exactly = 0) { repository.save(any()) }
    }
}


