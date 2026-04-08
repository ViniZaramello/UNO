package com.example.application.handler

import com.example.application.command.SkipPlayer
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

class SkipPlayerHandlerTest {

    private val faker = Faker()
    private val repository = mockk<GameRepository>()
    private lateinit var handler: SkipPlayerHandler

    @BeforeEach
    fun setup() {
        handler = SkipPlayerHandler(repository)
    }

    private fun playingGame(): Triple<com.example.application.model.Game, Player, Player> {
        val owner = Domain.player(name = faker.name.firstName(), passphrase = faker.name.lastName(), owner = true, number = 1, status = PlayerStatus.PLAYING)
        val target = Domain.player(name = faker.name.firstName(), passphrase = faker.name.lastName(), owner = false, number = 2, status = PlayerStatus.PLAYING)
        val game = Domain.game(players = mutableListOf(owner, target), status = GameStatus.PLAYING, playerTurn = 2)
        return Triple(game, owner, target)
    }

    @Test
    fun `should add 2 penalty cards to target and pass turn`() = runBlocking {
        val (game, owner, target) = playingGame()
        val cardsBefore = target.cards.size

        every { repository.findById(game.id) } returns game
        every { repository.save(game) } returns game

        handler.handle(SkipPlayer(gameId = game.id, playerName = owner.name, passphrase = owner.passphrase, targetPlayerName = target.name))

        target.cards.size shouldBe cardsBefore + 2
        game.playerTurn shouldBe 1
        verify(exactly = 1) { repository.save(game) }
    }

    @Test
    fun `should throw IllegalArgumentException when game is not PLAYING`() = runBlocking<Unit> {
        val owner = Domain.player(name = faker.name.firstName(), passphrase = faker.name.lastName(), owner = true, number = 1)
        val target = Domain.player(name = faker.name.firstName(), passphrase = faker.name.lastName(), owner = false, number = 2)
        val game = Domain.game(players = mutableListOf(owner, target), status = GameStatus.CREATED, playerTurn = 2)

        every { repository.findById(game.id) } returns game

        assertThrows<IllegalArgumentException> {
            runBlocking { handler.handle(SkipPlayer(gameId = game.id, playerName = owner.name, passphrase = owner.passphrase, targetPlayerName = target.name)) }
        }
        verify(exactly = 0) { repository.save(any()) }
    }

    @Test
    fun `should throw IllegalArgumentException when target player is not on their turn`() = runBlocking<Unit> {
        val owner = Domain.player(name = faker.name.firstName(), passphrase = faker.name.lastName(), owner = true, number = 1, status = PlayerStatus.PLAYING)
        val target = Domain.player(name = faker.name.firstName(), passphrase = faker.name.lastName(), owner = false, number = 2, status = PlayerStatus.PLAYING)
        val game = Domain.game(players = mutableListOf(owner, target), status = GameStatus.PLAYING, playerTurn = 1)

        every { repository.findById(game.id) } returns game

        assertThrows<IllegalArgumentException> {
            runBlocking { handler.handle(SkipPlayer(gameId = game.id, playerName = owner.name, passphrase = owner.passphrase, targetPlayerName = target.name)) }
        }
        verify(exactly = 0) { repository.save(any()) }
    }

    @Test
    fun `should throw IllegalArgumentException when passphrase is wrong`() = runBlocking<Unit> {
        val (game, owner, target) = playingGame()
        every { repository.findById(game.id) } returns game

        assertThrows<IllegalArgumentException> {
            runBlocking { handler.handle(SkipPlayer(gameId = game.id, playerName = owner.name, passphrase = "wrong", targetPlayerName = target.name)) }
        }
        verify(exactly = 0) { repository.save(any()) }
    }

    @Test
    fun `should throw IllegalArgumentException when player is not owner`() = runBlocking<Unit> {
        val (game, _, target) = playingGame()
        every { repository.findById(game.id) } returns game

        assertThrows<IllegalArgumentException> {
            runBlocking { handler.handle(SkipPlayer(gameId = game.id, playerName = target.name, passphrase = target.passphrase, targetPlayerName = target.name)) }
        }
        verify(exactly = 0) { repository.save(any()) }
    }

    @Test
    fun `should throw NotFoundException when game does not exist`() = runBlocking<Unit> {
        val randomId = UUID.randomUUID()
        every { repository.findById(randomId) } throws NotFoundException("Game $randomId was not found.")

        assertThrows<NotFoundException> {
            runBlocking { handler.handle(SkipPlayer(gameId = randomId, playerName = faker.name.firstName(), passphrase = faker.name.lastName(), targetPlayerName = faker.name.firstName())) }
        }
        verify(exactly = 0) { repository.save(any()) }
    }

    @Test
    fun `should throw NotFoundException when kicker player not found`() = runBlocking<Unit> {
        val (game, _, target) = playingGame()
        every { repository.findById(game.id) } returns game

        assertThrows<NotFoundException> {
            runBlocking { handler.handle(SkipPlayer(gameId = game.id, playerName = "ghost", passphrase = "any", targetPlayerName = target.name)) }
        }
        verify(exactly = 0) { repository.save(any()) }
    }

    @Test
    fun `should throw NotFoundException when target player not found`() = runBlocking<Unit> {
        val (game, owner, _) = playingGame()
        every { repository.findById(game.id) } returns game

        assertThrows<NotFoundException> {
            runBlocking { handler.handle(SkipPlayer(gameId = game.id, playerName = owner.name, passphrase = owner.passphrase, targetPlayerName = "ghost_target")) }
        }
        verify(exactly = 0) { repository.save(any()) }
    }
}

