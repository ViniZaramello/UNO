package com.example.query.status.by.gameId
import com.example.application.factories.Domain
import com.example.application.model.GameStatus
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
    fun `should return game stats for a valid game`() {
        val player = Domain.player(name = faker.name.firstName(), passphrase = "pass", number = 1)
        val game = Domain.game(players = mutableListOf(player), status = GameStatus.PLAYING)
        every { repository.findById(game.id) } returns game
        val stats = dao.getGameStats(game.id)
        stats.gameId shouldBe game.id.toString()
        stats.status shouldBe GameStatus.PLAYING
        stats.playerTurn shouldBe player.name
        verify(exactly = 1) { repository.findById(game.id) }
    }
    @Test
    fun `should return correct player stats for multiple players`() {
        val player1 = Domain.player(name = faker.name.firstName(), passphrase = "p1", number = 1)
        val player2 = Domain.player(name = faker.name.firstName(), passphrase = "p2", number = 2, owner = false)
        val game = Domain.game(players = mutableListOf(player1, player2))
        every { repository.findById(game.id) } returns game
        val stats = dao.getGameStats(game.id)
        stats.playerStats.size shouldBe 2
        stats.playerStats[0].playerName shouldBe player1.name
        stats.playerStats[1].playerName shouldBe player2.name
    }
    @Test
    fun `should return correct card quantities in hand`() {
        val cards = mutableListOf(Domain.card(), Domain.card())
        val player = Domain.player(name = faker.name.firstName(), passphrase = "pass", number = 1, cards = cards)
        val game = Domain.game(players = mutableListOf(player))
        every { repository.findById(game.id) } returns game
        val stats = dao.getGameStats(game.id)
        stats.cardsInHandQuantity shouldBe 2
        stats.playerStats[0].cardsQuantity shouldBe 2
    }
    @Test
    fun `should return correct stack and table card quantities`() {
        val player = Domain.player(name = faker.name.firstName(), passphrase = "pass", number = 1)
        val game = Domain.game(players = mutableListOf(player))
        every { repository.findById(game.id) } returns game
        val stats = dao.getGameStats(game.id)
        stats.cardsInStackQuantity shouldBe game.stacks.cardsInDeck.size
        stats.cardsInTableQuantity shouldBe game.stacks.cardsInTable.size
    }
    @Test
    fun `should return last card in table`() {
        val player = Domain.player(name = faker.name.firstName(), passphrase = "pass", number = 1)
        val game = Domain.game(players = mutableListOf(player))
        every { repository.findById(game.id) } returns game
        val stats = dao.getGameStats(game.id)
        stats.lastCardInTable shouldBe game.stacks.cardsInTable.last()
    }
    @Test
    fun `should return reverse flag`() {
        val player = Domain.player(name = faker.name.firstName(), passphrase = "pass", number = 1)
        val game = Domain.game(players = mutableListOf(player), reverse = true)
        every { repository.findById(game.id) } returns game
        val stats = dao.getGameStats(game.id)
        stats.reverse shouldBe true
    }
    @Test
    fun `should throw NotFoundException when game does not exist`() {
        val missingId = UUID.randomUUID()
        every { repository.findById(missingId) } throws NotFoundException("Game $missingId was not found.")
        assertThrows<NotFoundException> {
            dao.getGameStats(missingId)
        }
    }
    @Test
    fun `should return lastCard flag per player in playerStats`() {
        val player = Domain.player(
            name = faker.name.firstName(),
            passphrase = "pass",
            number = 1,
            lastCard = true
        )
        val game = Domain.game(players = mutableListOf(player))
        every { repository.findById(game.id) } returns game
        val stats = dao.getGameStats(game.id)
        stats.playerStats[0].lastCard shouldBe true
    }
    @Test
    fun `should return correct playerTurn name`() {
        val player1 = Domain.player(name = "Alice", passphrase = "p1", number = 1)
        val player2 = Domain.player(name = "Bob", passphrase = "p2", number = 2, owner = false)
        val game = Domain.game(players = mutableListOf(player1, player2), playerTurn = 2)
        every { repository.findById(game.id) } returns game
        val stats = dao.getGameStats(game.id)
        stats.playerTurn shouldBe "Bob"
    }
    @Test
    fun `should aggregate cardsInHandQuantity across all players`() {
        val player1 = Domain.player(
            name = faker.name.firstName(),
            passphrase = "p1",
            number = 1,
            cards = mutableListOf(Domain.card(), Domain.card())
        )
        val player2 = Domain.player(
            name = faker.name.firstName(),
            passphrase = "p2",
            number = 2,
            owner = false,
            cards = mutableListOf(Domain.card(), Domain.card(), Domain.card())
        )
        val game = Domain.game(players = mutableListOf(player1, player2))
        every { repository.findById(game.id) } returns game
        val stats = dao.getGameStats(game.id)
        stats.cardsInHandQuantity shouldBe 5
    }
}
