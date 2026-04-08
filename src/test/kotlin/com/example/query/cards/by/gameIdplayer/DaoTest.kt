package com.example.query.cards.by.gameIdplayer

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
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.UUID

class DaoTest {

    private val faker = Faker()
    private val repository = mockk<GameRepository>()
    private lateinit var dao: Dao

    @BeforeEach
    fun setup() {
        dao = Dao(repository)
    }

    @Test
    fun `should return player hand cards for a valid game, player and passphrase`() {
        val playerName = faker.name.firstName()
        val passphrase = faker.name.lastName()
        val player = Domain.player(name = playerName, passphrase = passphrase, number = 1)
        val game = Domain.game(players = mutableListOf(player), status = GameStatus.PLAYING)

        every { repository.findById(game.id) } returns game

        val response = dao.getPlayerHandCards(game.id, playerName, passphrase)

        response.player.name shouldBe playerName
        response.player.number shouldBe 1
        response.gameStats.gameId shouldBe game.id.toString()
        verify(exactly = 1) { repository.findById(game.id) }
    }

    @Test
    fun `should return correct game stats with multiple players`() {
        val player1 = Domain.player(name = faker.name.firstName(), passphrase = "pass1", number = 1)
        val player2 = Domain.player(name = faker.name.firstName(), passphrase = "pass2", number = 2, owner = false)
        val game = Domain.game(players = mutableListOf(player1, player2), status = GameStatus.PLAYING)

        every { repository.findById(game.id) } returns game

        val response = dao.getPlayerHandCards(game.id, player1.name, "pass1")

        response.gameStats.playerStats.size shouldBe 2
        response.gameStats.status shouldBe GameStatus.PLAYING
    }

    @Test
    fun `should indicate it is the player's turn when playerTurn matches`() {
        val player = Domain.player(name = faker.name.firstName(), passphrase = "secret", number = 1)
        val game = Domain.game(players = mutableListOf(player), playerTurn = 1)

        every { repository.findById(game.id) } returns game

        val response = dao.getPlayerHandCards(game.id, player.name, "secret")

        response.player.isYourTurn shouldBe true
    }

    @Test
    fun `should indicate it is NOT the player's turn when playerTurn does not match`() {
        val player1 = Domain.player(name = faker.name.firstName(), passphrase = "p1", number = 1)
        val player2 = Domain.player(name = faker.name.firstName(), passphrase = "p2", number = 2, owner = false)
        val game = Domain.game(players = mutableListOf(player1, player2), playerTurn = 2)

        every { repository.findById(game.id) } returns game

        val response = dao.getPlayerHandCards(game.id, player1.name, "p1")

        response.player.isYourTurn shouldBe false
    }

    @Test
    fun `should throw IllegalArgumentException when passphrase is invalid`() {
        val player = Domain.player(name = faker.name.firstName(), passphrase = "correct", number = 1)
        val game = Domain.game(players = mutableListOf(player))

        every { repository.findById(game.id) } returns game

        assertThrows<IllegalArgumentException> {
            dao.getPlayerHandCards(game.id, player.name, "wrong")
        }
    }

    @Test
    fun `should throw NotFoundException when game does not exist`() {
        val missingId = UUID.randomUUID()

        every { repository.findById(missingId) } throws NotFoundException("Game $missingId was not found.")

        assertThrows<NotFoundException> {
            dao.getPlayerHandCards(missingId, "any", "any")
        }
    }

    @Test
    fun `should throw NotFoundException when player does not exist in game`() {
        val player = Domain.player(name = faker.name.firstName(), passphrase = "pass", number = 1)
        val game = Domain.game(players = mutableListOf(player))

        every { repository.findById(game.id) } returns game

        assertThrows<NotFoundException> {
            dao.getPlayerHandCards(game.id, "nonExistentPlayer", "pass")
        }
    }

    @Test
    fun `should return correct card counts in game stats`() {
        val cards = mutableListOf(Domain.card(), Domain.card(), Domain.card())
        val player = Domain.player(name = faker.name.firstName(), passphrase = "pass", number = 1, cards = cards)
        val game = Domain.game(players = mutableListOf(player))

        every { repository.findById(game.id) } returns game

        val response = dao.getPlayerHandCards(game.id, player.name, "pass")

        response.player.totalCards shouldBe 3
        response.player.cards.size shouldBe 3
        response.gameStats.cardsInHandQuantity shouldBe 3
    }

    @Test
    fun `should return lastCard flag from player`() {
        val player = Domain.player(name = faker.name.firstName(), passphrase = "pass", number = 1, lastCard = true)
        val game = Domain.game(players = mutableListOf(player))

        every { repository.findById(game.id) } returns game

        val response = dao.getPlayerHandCards(game.id, player.name, "pass")

        response.player.lastCard shouldBe true
    }

    @Test
    fun `should return player status from player`() {
        val player = Domain.player(
            name = faker.name.firstName(),
            passphrase = "pass",
            number = 1,
            status = PlayerStatus.FINISHED
        )
        val game = Domain.game(players = mutableListOf(player))

        every { repository.findById(game.id) } returns game

        val response = dao.getPlayerHandCards(game.id, player.name, "pass")

        response.player.playerStatus shouldBe PlayerStatus.FINISHED
    }

    @Test
    fun `should return reverse flag from game`() {
        val player = Domain.player(name = faker.name.firstName(), passphrase = "pass", number = 1)
        val game = Domain.game(players = mutableListOf(player), reverse = true)

        every { repository.findById(game.id) } returns game

        val response = dao.getPlayerHandCards(game.id, player.name, "pass")

        response.gameStats.reverse shouldBe true
    }

    @Test
    fun `should include all players in playerStats`() {
        val player1 = Domain.player(name = "Alice", passphrase = "p1", number = 1)
        val player2 = Domain.player(name = "Bob", passphrase = "p2", number = 2, owner = false)
        val player3 = Domain.player(name = "Carol", passphrase = "p3", number = 3, owner = false)
        val game = Domain.game(players = mutableListOf(player1, player2, player3))

        every { repository.findById(game.id) } returns game

        val response = dao.getPlayerHandCards(game.id, "Alice", "p1")

        response.gameStats.playerStats.map { it.playerName } shouldBe listOf("Alice", "Bob", "Carol")
    }
}

