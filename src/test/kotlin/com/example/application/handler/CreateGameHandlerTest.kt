package com.example.application.handler

import com.example.application.command.CreateGame
import com.example.application.model.Game
import com.example.application.model.GameStatus
import com.example.application.model.Player
import com.example.application.ports.outbound.GameRepository
import io.github.serpro69.kfaker.Faker
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

class CreateGameHandlerTest {

    private val faker = Faker()
    private val repository = mockk<GameRepository>()
    private lateinit var handler: CreateGameHandler

    @BeforeEach
    fun setup() {
        handler = CreateGameHandler(repository)
    }

    @Test
    fun `should create a game, save it and return its id`(): Unit = runBlocking {
        val playerName = faker.name.firstName()
        val passphrase = faker.name.lastName()
        val player = Player(name = playerName, passphrase = passphrase, owner = true)
        val command = CreateGame(player)
        val gameSlot = slot<Game>()

        every { repository.save(capture(gameSlot)) } answers { firstArg() }

        val result = handler.handle(command)

        verify(exactly = 1) { repository.save(any()) }
        result shouldNotBe null
        UUID.fromString(result) shouldBe gameSlot.captured.id
    }

    @Test
    fun `should create game with status CREATED`(): Unit = runBlocking {
        val player = Player(name = faker.name.firstName(), passphrase = faker.name.lastName(), owner = true)
        val gameSlot = slot<Game>()

        every { repository.save(capture(gameSlot)) } answers { firstArg() }

        handler.handle(CreateGame(player))

        gameSlot.captured.status shouldBe GameStatus.CREATED
    }

    @Test
    fun `should create game with the owner player in the player list`(): Unit = runBlocking {
        val playerName = faker.name.firstName()
        val passphrase = faker.name.lastName()
        val player = Player(name = playerName, passphrase = passphrase, owner = true)
        val gameSlot = slot<Game>()

        every { repository.save(capture(gameSlot)) } answers { firstArg() }

        handler.handle(CreateGame(player))

        val savedGame = gameSlot.captured
        savedGame.players shouldBe mutableListOf(player)
        savedGame.players.first().name shouldBe playerName
        savedGame.players.first().owner shouldBe true
    }

    @Test
    fun `should create game with a single player`(): Unit = runBlocking {
        val player = Player(name = faker.name.firstName(), passphrase = faker.name.lastName(), owner = true)
        val gameSlot = slot<Game>()

        every { repository.save(capture(gameSlot)) } answers { firstArg() }

        handler.handle(CreateGame(player))

        gameSlot.captured.players.size shouldBe 1
    }

    @Test
    fun `should return a valid UUID string`(): Unit = runBlocking {
        val player = Player(name = faker.name.firstName(), passphrase = faker.name.lastName(), owner = true)

        every { repository.save(any()) } answers { firstArg() }

        val result = handler.handle(CreateGame(player))

        var isValidUUID = true
        try { UUID.fromString(result) } catch (e: IllegalArgumentException) { isValidUUID = false }
        isValidUUID shouldBe true
    }

    @Test
    fun `each call should create a game with a unique id`() = runBlocking {
        val savedGames = mutableListOf<Game>()

        every { repository.save(any()) } answers {
            val game: Game = firstArg()
            savedGames.add(game)
            game
        }

        val player1 = Player(name = faker.name.firstName(), passphrase = faker.name.lastName(), owner = true)
        val player2 = Player(name = faker.name.firstName(), passphrase = faker.name.lastName(), owner = true)

        val id1 = handler.handle(CreateGame(player1))
        val id2 = handler.handle(CreateGame(player2))

        id1 shouldNotBe id2
        verify(exactly = 2) { repository.save(any()) }
    }
}
