package com.example.application.handler

import com.example.application.command.PassTurn
import com.example.application.factories.Domain
import com.example.application.model.GameStatus
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

class PassTurnHandlerTest {

    private val faker = Faker()
    private val repository = mockk<GameRepository>()
    private lateinit var handler: PassTurnHandler

    @BeforeEach
    fun setup() {
        handler = PassTurnHandler(repository)
    }

    @Test
    fun `should pass turn and reset buyParityCard when player has drawn a card`() = runBlocking {
        val player = Domain.player(name = faker.name.firstName(), passphrase = faker.name.lastName(), number = 1, status = PlayerStatus.PLAYING, buyParityCard = true)
        val other = Domain.player(name = faker.name.firstName(), passphrase = faker.name.lastName(), number = 2, status = PlayerStatus.PLAYING)
        val game = Domain.game(players = mutableListOf(player, other), status = GameStatus.PLAYING, playerTurn = 1)

        every { repository.findById(game.id) } returns game
        every { repository.save(game) } returns game

        handler.handle(PassTurn(gameId = game.id, playerName = player.name, passphrase = player.passphrase))

        game.playerTurn shouldBe 2
        player.buyParityCard shouldBe false
        verify(exactly = 1) { repository.save(game) }
    }

    @Test
    fun `should throw IllegalArgumentException when player has not drawn a card`() = runBlocking<Unit> {
        val player = Domain.player(name = faker.name.firstName(), passphrase = faker.name.lastName(), number = 1, status = PlayerStatus.PLAYING, buyParityCard = false)
        val other = Domain.player(name = faker.name.firstName(), passphrase = faker.name.lastName(), number = 2, status = PlayerStatus.PLAYING)
        val game = Domain.game(players = mutableListOf(player, other), status = GameStatus.PLAYING, playerTurn = 1)

        every { repository.findById(game.id) } returns game

        assertThrows<IllegalArgumentException> {
            runBlocking { handler.handle(PassTurn(gameId = game.id, playerName = player.name, passphrase = player.passphrase)) }
        }
        game.playerTurn shouldBe 1
        verify(exactly = 0) { repository.save(any()) }
    }

    @Test
    fun `should throw IllegalArgumentException when it is not the player's turn`() = runBlocking<Unit> {
        val player = Domain.player(name = faker.name.firstName(), passphrase = faker.name.lastName(), number = 1, status = PlayerStatus.PLAYING, buyParityCard = true)
        val other = Domain.player(name = faker.name.firstName(), passphrase = faker.name.lastName(), number = 2, status = PlayerStatus.PLAYING)
        val game = Domain.game(players = mutableListOf(player, other), status = GameStatus.PLAYING, playerTurn = 2)

        every { repository.findById(game.id) } returns game

        assertThrows<IllegalArgumentException> {
            runBlocking { handler.handle(PassTurn(gameId = game.id, playerName = player.name, passphrase = player.passphrase)) }
        }
        verify(exactly = 0) { repository.save(any()) }
    }

    @Test
    fun `should throw IllegalArgumentException when game is not PLAYING`() = runBlocking<Unit> {
        val player = Domain.player(name = faker.name.firstName(), passphrase = faker.name.lastName(), number = 1, buyParityCard = true)
        val game = Domain.game(players = mutableListOf(player), status = GameStatus.CREATED, playerTurn = 1)

        every { repository.findById(game.id) } returns game

        assertThrows<IllegalArgumentException> {
            runBlocking { handler.handle(PassTurn(gameId = game.id, playerName = player.name, passphrase = player.passphrase)) }
        }
        verify(exactly = 0) { repository.save(any()) }
    }

    @Test
    fun `should throw IllegalArgumentException when passphrase is wrong`() = runBlocking<Unit> {
        val player = Domain.player(name = faker.name.firstName(), passphrase = faker.name.lastName(), number = 1, status = PlayerStatus.PLAYING)
        val game = Domain.game(players = mutableListOf(player), status = GameStatus.PLAYING, playerTurn = 1)

        every { repository.findById(game.id) } returns game

        assertThrows<IllegalArgumentException> {
            runBlocking { handler.handle(PassTurn(gameId = game.id, playerName = player.name, passphrase = "wrong")) }
        }
        verify(exactly = 0) { repository.save(any()) }
    }

    @Test
    fun `should throw NotFoundException when game does not exist`() = runBlocking<Unit> {
        val randomId = UUID.randomUUID()
        every { repository.findById(randomId) } throws NotFoundException("Game $randomId was not found.")

        assertThrows<NotFoundException> {
            runBlocking { handler.handle(PassTurn(gameId = randomId, playerName = faker.name.firstName(), passphrase = faker.name.lastName())) }
        }
        verify(exactly = 0) { repository.save(any()) }
    }

    @Test
    fun `should throw NotFoundException when player does not exist in game`() = runBlocking<Unit> {
        val game = Domain.game(status = GameStatus.PLAYING)
        every { repository.findById(game.id) } returns game

        assertThrows<NotFoundException> {
            runBlocking { handler.handle(PassTurn(gameId = game.id, playerName = "ghost", passphrase = "any")) }
        }
        verify(exactly = 0) { repository.save(any()) }
    }
}

