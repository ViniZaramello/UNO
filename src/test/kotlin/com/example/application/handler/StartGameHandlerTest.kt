package com.example.application.handler

import com.example.application.command.StartGame
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

class StartGameHandlerTest {

    private val faker = Faker()
    private val repository = mockk<GameRepository>()
    private lateinit var handler: StartGameHandler

    @BeforeEach
    fun setup() {
        handler = StartGameHandler(repository)
    }

    private fun twoPlayerGame(status: GameStatus = GameStatus.CREATED) = Domain.game(
        status = status,
        players = mutableListOf(
            Domain.player(name = faker.name.firstName(), passphrase = faker.name.lastName(), owner = true, number = 1),
            Domain.player(name = faker.name.firstName(), passphrase = faker.name.lastName(), owner = false, number = 2)
        )
    )

    @Test
    fun `should start game and set status to PLAYING`() = runBlocking {
        val game = twoPlayerGame()
        val owner = game.players.first()

        every { repository.findById(game.id) } returns game
        every { repository.save(game) } returns game

        handler.handle(StartGame(playerName = owner.name, gameId = game.id, passphrase = owner.passphrase))

        game.status shouldBe GameStatus.PLAYING
        verify(exactly = 1) { repository.save(game) }
    }

    @Test
    fun `should distribute 7 cards to each player on start`() = runBlocking {
        val game = twoPlayerGame()
        val owner = game.players.first()

        every { repository.findById(game.id) } returns game
        every { repository.save(game) } returns game

        handler.handle(StartGame(playerName = owner.name, gameId = game.id, passphrase = owner.passphrase))

        game.players.forEach { player -> player.cards.size shouldBe 7 }
    }

    @Test
    fun `should set all player statuses to PLAYING on start`() = runBlocking {
        val game = twoPlayerGame()
        val owner = game.players.first()

        every { repository.findById(game.id) } returns game
        every { repository.save(game) } returns game

        handler.handle(StartGame(playerName = owner.name, gameId = game.id, passphrase = owner.passphrase))

        game.players.forEach { player -> player.statusInGame shouldBe PlayerStatus.PLAYING }
    }

    @Test
    fun `should reset game and restart when status is FINISHED`() = runBlocking {
        val game = twoPlayerGame(status = GameStatus.FINISHED)
        val owner = game.players.first()

        every { repository.findById(game.id) } returns game
        every { repository.save(game) } returns game

        handler.handle(StartGame(playerName = owner.name, gameId = game.id, passphrase = owner.passphrase))

        game.status shouldBe GameStatus.PLAYING
        game.players.forEach { player -> player.cards.size shouldBe 7 }
        game.reverse shouldBe false
        game.blockPending shouldBe false
        game.buyCardQuantity shouldBe 0
    }

    @Test
    fun `should transition status from CREATED to WAITING then PLAYING`() = runBlocking {
        val game = twoPlayerGame(status = GameStatus.CREATED)
        val owner = game.players.first()

        every { repository.findById(game.id) } returns game
        every { repository.save(game) } returns game

        handler.handle(StartGame(playerName = owner.name, gameId = game.id, passphrase = owner.passphrase))

        game.status shouldBe GameStatus.PLAYING
    }

    @Test
    fun `should throw IllegalStateException when game is already PLAYING`() = runBlocking<Unit> {
        val game = twoPlayerGame(status = GameStatus.PLAYING)
        val owner = game.players.first()

        every { repository.findById(game.id) } returns game

        assertThrows<IllegalStateException> {
            runBlocking { handler.handle(StartGame(playerName = owner.name, gameId = game.id, passphrase = owner.passphrase)) }
        }

        verify(exactly = 0) { repository.save(any()) }
    }

    @Test
    fun `should throw IllegalArgumentException when fewer than 2 players`() = runBlocking<Unit> {
        val owner = Domain.player(name = faker.name.firstName(), passphrase = faker.name.lastName(), owner = true)
        val game = Domain.game(players = mutableListOf(owner), status = GameStatus.CREATED)

        every { repository.findById(game.id) } returns game

        assertThrows<IllegalArgumentException> {
            runBlocking { handler.handle(StartGame(playerName = owner.name, gameId = game.id, passphrase = owner.passphrase)) }
        }

        verify(exactly = 0) { repository.save(any()) }
    }

    @Test
    fun `should throw NotFoundException when game does not exist`() = runBlocking<Unit> {
        val randomId = UUID.randomUUID()

        every { repository.findById(randomId) } throws NotFoundException("Game $randomId was not found.")

        assertThrows<NotFoundException> {
            runBlocking { handler.handle(StartGame(playerName = faker.name.firstName(), gameId = randomId, passphrase = faker.name.lastName())) }
        }

        verify(exactly = 0) { repository.save(any()) }
    }

    @Test
    fun `should throw NotFoundException when player does not exist in game`() = runBlocking<Unit> {
        val game = twoPlayerGame()

        every { repository.findById(game.id) } returns game

        assertThrows<NotFoundException> {
            runBlocking { handler.handle(StartGame(playerName = "ghost_player", gameId = game.id, passphrase = "any")) }
        }

        verify(exactly = 0) { repository.save(any()) }
    }

    @Test
    fun `should throw IllegalArgumentException when passphrase is wrong`() = runBlocking<Unit> {
        val game = twoPlayerGame()
        val owner = game.players.first()

        every { repository.findById(game.id) } returns game

        assertThrows<IllegalArgumentException> {
            runBlocking { handler.handle(StartGame(playerName = owner.name, gameId = game.id, passphrase = "wrong_passphrase")) }
        }

        verify(exactly = 0) { repository.save(any()) }
    }

    @Test
    fun `should throw IllegalArgumentException when player is not owner`() = runBlocking<Unit> {
        val game = twoPlayerGame()
        val nonOwner = game.players[1] // second player is not owner

        every { repository.findById(game.id) } returns game

        assertThrows<IllegalArgumentException> {
            runBlocking { handler.handle(StartGame(playerName = nonOwner.name, gameId = game.id, passphrase = nonOwner.passphrase)) }
        }

        verify(exactly = 0) { repository.save(any()) }
    }

    @Test
    fun `should save game exactly once on success`() = runBlocking {
        val game = twoPlayerGame()
        val owner = game.players.first()

        every { repository.findById(game.id) } returns game
        every { repository.save(game) } returns game

        handler.handle(StartGame(playerName = owner.name, gameId = game.id, passphrase = owner.passphrase))

        verify(exactly = 1) { repository.save(game) }
    }

    @Test
    fun `started game should have exactly one card on the table`() = runBlocking {
        val game = twoPlayerGame()
        val owner = game.players.first()

        every { repository.findById(game.id) } returns game
        every { repository.save(game) } returns game

        handler.handle(StartGame(playerName = owner.name, gameId = game.id, passphrase = owner.passphrase))

        game.stacks.cardsInTable.size shouldBe 1
    }
}

