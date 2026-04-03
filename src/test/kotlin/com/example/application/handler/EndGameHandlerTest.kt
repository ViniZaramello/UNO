package com.example.application.handler

import com.example.application.command.EndGame
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

class EndGameHandlerTest {

    private val faker = Faker()
    private val repository = mockk<GameRepository>()
    private lateinit var handler: EndGameHandler

    @BeforeEach
    fun setup() {
        handler = EndGameHandler(repository)
    }

    @Test
    fun `should set game status to FINISHED`() = runBlocking {
        val game = Domain.game(status = GameStatus.PLAYING)
        val owner = game.players.first()

        every { repository.findById(game.id) } returns game
        every { repository.save(game) } returns game

        handler.handle(EndGame(gameId = game.id, playerName = owner.name, passphrase = owner.passphrase))

        game.status shouldBe GameStatus.FINISHED
    }

    @Test
    fun `should set all player statuses to FINISHED`() = runBlocking {
        val players = mutableListOf(
            Domain.player(name = faker.name.firstName(), passphrase = faker.name.lastName(), owner = true, number = 1),
            Domain.player(name = faker.name.firstName(), passphrase = faker.name.lastName(), owner = false, number = 2)
        )
        val game = Domain.game(players = players, status = GameStatus.PLAYING)
        val owner = game.players.first()

        every { repository.findById(game.id) } returns game
        every { repository.save(game) } returns game

        handler.handle(EndGame(gameId = game.id, playerName = owner.name, passphrase = owner.passphrase))

        game.players.forEach { player -> player.statusInGame shouldBe PlayerStatus.FINISHED }
    }

    @Test
    fun `should save game exactly once on success`() = runBlocking {
        val game = Domain.game()
        val owner = game.players.first()

        every { repository.findById(game.id) } returns game
        every { repository.save(game) } returns game

        handler.handle(EndGame(gameId = game.id, playerName = owner.name, passphrase = owner.passphrase))

        verify(exactly = 1) { repository.save(game) }
    }

    @Test
    fun `should throw NotFoundException when game does not exist`() = runBlocking<Unit> {
        val randomId = UUID.randomUUID()

        every { repository.findById(randomId) } throws NotFoundException("Game $randomId was not found.")

        assertThrows<NotFoundException> {
            runBlocking { handler.handle(EndGame(gameId = randomId, playerName = faker.name.firstName(), passphrase = faker.name.lastName())) }
        }

        verify(exactly = 0) { repository.save(any()) }
    }

    @Test
    fun `should throw NotFoundException when player does not exist in game`() = runBlocking<Unit> {
        val game = Domain.game()

        every { repository.findById(game.id) } returns game

        assertThrows<NotFoundException> {
            runBlocking { handler.handle(EndGame(gameId = game.id, playerName = "ghost_player", passphrase = "any")) }
        }

        verify(exactly = 0) { repository.save(any()) }
    }

    @Test
    fun `should throw IllegalArgumentException when passphrase is wrong`() = runBlocking<Unit> {
        val game = Domain.game()
        val owner = game.players.first()

        every { repository.findById(game.id) } returns game

        assertThrows<IllegalArgumentException> {
            runBlocking { handler.handle(EndGame(gameId = game.id, playerName = owner.name, passphrase = "wrong_passphrase")) }
        }

        verify(exactly = 0) { repository.save(any()) }
    }

    @Test
    fun `should throw IllegalArgumentException when player is not owner`() = runBlocking<Unit> {
        val nonOwner = Domain.player(name = faker.name.firstName(), passphrase = faker.name.lastName(), owner = false, number = 2)
        val owner = Domain.player(name = faker.name.firstName(), passphrase = faker.name.lastName(), owner = true, number = 1)
        val game = Domain.game(players = mutableListOf(owner, nonOwner))

        every { repository.findById(game.id) } returns game

        assertThrows<IllegalArgumentException> {
            runBlocking { handler.handle(EndGame(gameId = game.id, playerName = nonOwner.name, passphrase = nonOwner.passphrase)) }
        }

        verify(exactly = 0) { repository.save(any()) }
    }

    @Test
    fun `should not save when an error occurs`() = runBlocking<Unit> {
        val game = Domain.game()

        every { repository.findById(game.id) } returns game

        assertThrows<NotFoundException> {
            runBlocking { handler.handle(EndGame(gameId = game.id, playerName = "ghost_player", passphrase = "any")) }
        }

        verify(exactly = 0) { repository.save(any()) }
    }

    @Test
    fun `should end game regardless of current game status`() = runBlocking {
        listOf(GameStatus.CREATED, GameStatus.WAITING, GameStatus.PLAYING, GameStatus.FINISHED).forEach { status ->
            val game = Domain.game(status = status)
            val owner = game.players.first()

            every { repository.findById(game.id) } returns game
            every { repository.save(game) } returns game

            handler.handle(EndGame(gameId = game.id, playerName = owner.name, passphrase = owner.passphrase))

            game.status shouldBe GameStatus.FINISHED
        }
    }
}

