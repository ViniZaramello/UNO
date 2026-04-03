package com.example.application.handler

import com.example.application.command.JoinPlayerInGame
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

class JoinPlayerInGameHandlerTest {

    private val faker = Faker()
    private val repository = mockk<GameRepository>()
    private lateinit var handler: JoinPlayerInGameHandler

    @BeforeEach
    fun setup() {
        handler = JoinPlayerInGameHandler(repository)
    }

    @Test
    fun `should add player to game and save`() = runBlocking {
        val game = Domain.game(status = GameStatus.CREATED)
        val newPlayerName = faker.name.firstName()
        val newPassphrase = faker.name.lastName()
        val newPlayer = Player(name = newPlayerName, passphrase = newPassphrase)
        val command = JoinPlayerInGame(player = newPlayer, gameId = game.id)

        every { repository.findById(game.id) } returns game
        every { repository.save(game) } returns game

        handler.handle(command)

        game.players.any { it.name == newPlayerName } shouldBe true
        verify(exactly = 1) { repository.save(game) }
    }

    @Test
    fun `should assign correct player number when joining`(): Unit = runBlocking {
        val existingPlayer = Domain.player(name = faker.name.firstName(), number = 1)
        val game = Domain.game(
            players = mutableListOf(existingPlayer),
            status = GameStatus.CREATED
        )
        val newPlayer = Player(name = faker.name.firstName(), passphrase = faker.name.lastName())
        val command = JoinPlayerInGame(player = newPlayer, gameId = game.id)

        every { repository.findById(game.id) } returns game
        every { repository.save(game) } returns game

        handler.handle(command)

        game.findPlayer(newPlayer.name).number shouldBe 2
    }

    @Test
    fun `should allow joining when game status is WAITING`(): Unit = runBlocking {
        val game = Domain.game(status = GameStatus.WAITING)
        val newPlayer = Player(name = faker.name.firstName(), passphrase = faker.name.lastName())
        val command = JoinPlayerInGame(player = newPlayer, gameId = game.id)

        every { repository.findById(game.id) } returns game
        every { repository.save(game) } returns game

        handler.handle(command)

        game.players.any { it.name == newPlayer.name } shouldBe true
    }

    @Test
    fun `should throw NotFoundException when game does not exist`() = runBlocking<Unit> {
        val randomId = UUID.randomUUID()
        val newPlayer = Player(name = faker.name.firstName(), passphrase = faker.name.lastName())
        val command = JoinPlayerInGame(player = newPlayer, gameId = randomId)

        every { repository.findById(randomId) } throws NotFoundException("Game $randomId was not found.")

        assertThrows<NotFoundException> {
            runBlocking { handler.handle(command) }
        }

        verify(exactly = 0) { repository.save(any()) }
    }

    @Test
    fun `should throw IllegalArgumentException when game is in PLAYING status`() = runBlocking<Unit> {
        val game = Domain.game(status = GameStatus.PLAYING)
        val newPlayer = Player(name = faker.name.firstName(), passphrase = faker.name.lastName())
        val command = JoinPlayerInGame(player = newPlayer, gameId = game.id)

        every { repository.findById(game.id) } returns game

        assertThrows<IllegalArgumentException> {
            runBlocking { handler.handle(command) }
        }

        verify(exactly = 0) { repository.save(any()) }
    }

    @Test
    fun `should throw IllegalArgumentException when game is in FINISHED status`() = runBlocking<Unit> {
        val game = Domain.game(status = GameStatus.FINISHED)
        val newPlayer = Player(name = faker.name.firstName(), passphrase = faker.name.lastName())
        val command = JoinPlayerInGame(player = newPlayer, gameId = game.id)

        every { repository.findById(game.id) } returns game

        assertThrows<IllegalArgumentException> {
            runBlocking { handler.handle(command) }
        }

        verify(exactly = 0) { repository.save(any()) }
    }

    @Test
    fun `should throw IllegalArgumentException when player name already exists in game`() = runBlocking<Unit> {
        val existingName = "duplicatedPlayer"
        val existingPlayer = Domain.player(name = existingName)
        val game = Domain.game(
            players = mutableListOf(existingPlayer),
            status = GameStatus.CREATED
        )
        val duplicatePlayer = Player(name = existingName, passphrase = faker.name.lastName())
        val command = JoinPlayerInGame(player = duplicatePlayer, gameId = game.id)

        every { repository.findById(game.id) } returns game

        val exception = assertThrows<IllegalArgumentException> {
            runBlocking { handler.handle(command) }
        }

        exception.message shouldBe "The name $existingName is already in use."
        verify(exactly = 0) { repository.save(any()) }
    }

    @Test
    fun `should throw IllegalStateException when player limit is exceeded`() = runBlocking<Unit> {
        val players = mutableListOf(
            Domain.player(name = faker.name.firstName(), number = 1),
            Domain.player(name = faker.name.firstName(), number = 2),
            Domain.player(name = faker.name.firstName(), number = 3),
            Domain.player(name = faker.name.firstName(), number = 4)
        )
        val game = Domain.game(players = players, status = GameStatus.CREATED)
        val newPlayer = Player(name = faker.name.firstName(), passphrase = faker.name.lastName())
        val command = JoinPlayerInGame(player = newPlayer, gameId = game.id)

        every { repository.findById(game.id) } returns game

        assertThrows<IllegalStateException> {
            runBlocking { handler.handle(command) }
        }

        game.players.size shouldBe 4
        verify(exactly = 0) { repository.save(any()) }
    }

    @Test
    fun `joined player should have IN_LOBBY status`(): Unit = runBlocking {
        val game = Domain.game(status = GameStatus.CREATED)
        val newPlayer = Player(name = faker.name.firstName(), passphrase = faker.name.lastName())
        val command = JoinPlayerInGame(player = newPlayer, gameId = game.id)

        every { repository.findById(game.id) } returns game
        every { repository.save(game) } returns game

        handler.handle(command)

        game.findPlayer(newPlayer.name).statusInGame shouldBe PlayerStatus.IN_LOBBY
    }

    @Test
    fun `joined player should not be owner`(): Unit = runBlocking {
        val game = Domain.game(status = GameStatus.CREATED)
        val newPlayer = Player(name = faker.name.firstName(), passphrase = faker.name.lastName())
        val command = JoinPlayerInGame(player = newPlayer, gameId = game.id)

        every { repository.findById(game.id) } returns game
        every { repository.save(game) } returns game

        handler.handle(command)

        game.findPlayer(newPlayer.name).owner shouldBe false
    }

    @Test
    fun `joined player should start with empty cards`(): Unit = runBlocking {
        val game = Domain.game(status = GameStatus.CREATED)
        val newPlayer = Player(name = faker.name.firstName(), passphrase = faker.name.lastName())
        val command = JoinPlayerInGame(player = newPlayer, gameId = game.id)

        every { repository.findById(game.id) } returns game
        every { repository.save(game) } returns game

        handler.handle(command)

        game.findPlayer(newPlayer.name).cards shouldBe emptyList()
    }
}
