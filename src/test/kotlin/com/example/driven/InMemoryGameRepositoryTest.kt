package com.example.driven

import com.example.application.factories.Domain
import com.example.configuration.NotFoundException
import io.github.serpro69.kfaker.Faker
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.UUID

class InMemoryGameRepositoryTest {

    private val faker = Faker()
    private val repository = InMemoryGameRepository()

    @BeforeEach
    fun setup() {
        repository.clear()
    }

    @Test
    fun `save should store game and return it`() {
        val game = Domain.game()

        val result = repository.save(game)

        result shouldBe game
        repository.size() shouldBe 1
    }

    @Test
    fun `save should overwrite game with same id`() {
        val game = Domain.game()
        repository.save(game)

        game.status = com.example.application.model.GameStatus.PLAYING
        repository.save(game)

        repository.size() shouldBe 1
        repository.findById(game.id).status shouldBe com.example.application.model.GameStatus.PLAYING
    }

    @Test
    fun `save should store multiple distinct games`() {
        repeat(3) { repository.save(Domain.game()) }

        repository.size() shouldBe 3
    }

    @Test
    fun `findById should return game when it exists`() {
        val game = Domain.game()
        repository.save(game)

        val found = repository.findById(game.id)

        found shouldBe game
        found.id shouldBe game.id
    }

    @Test
    fun `findById should throw NotFoundException when game does not exist`() {
        val missingId = UUID.randomUUID()

        val exception = assertThrows<NotFoundException> {
            repository.findById(missingId)
        }

        exception.message shouldBe "Game $missingId was not found."
    }

    @Test
    fun `findById should throw NotFoundException after game is deleted`() {
        val game = Domain.game()
        repository.save(game)
        repository.delete(game)

        assertThrows<NotFoundException> {
            repository.findById(game.id)
        }
    }

    @Test
    fun `delete should remove game from repository`() {
        val game = Domain.game()
        repository.save(game)

        repository.delete(game)

        repository.size() shouldBe 0
    }

    @Test
    fun `delete should not affect other games`() {
        val gameA = Domain.game()
        val gameB = Domain.game()
        repository.save(gameA)
        repository.save(gameB)

        repository.delete(gameA)

        repository.size() shouldBe 1
        repository.findById(gameB.id) shouldNotBe null
    }

    @Test
    fun `delete on non-existent game should not throw`() {
        val game = Domain.game()

        // should not throw
        repository.delete(game)

        repository.size() shouldBe 0
    }

    @Test
    fun `findAll should return all saved games`() {
        val games = (1..4).map { Domain.game() }
        games.forEach { repository.save(it) }

        val all = repository.findAll()

        all shouldHaveSize 4
        games.forEach { game -> all.any { it.id == game.id } shouldBe true }
    }

    @Test
    fun `findAll should return empty list when no games exist`() {
        repository.findAll() shouldHaveSize 0
    }

    @Test
    fun `size should reflect current number of games`() {
        repository.size() shouldBe 0

        val game = Domain.game()
        repository.save(game)
        repository.size() shouldBe 1

        repository.delete(game)
        repository.size() shouldBe 0
    }

    @Test
    fun `clear should remove all games`() {
        repeat(5) { repository.save(Domain.game()) }

        repository.clear()

        repository.size() shouldBe 0
        repository.findAll() shouldHaveSize 0
    }

    @Test
    fun `save should preserve all game fields`() {
        val playerName = faker.name.firstName()
        val passphrase = faker.name.lastName()
        val player = Domain.player(name = playerName, passphrase = passphrase)
        val game = Domain.game(players = mutableListOf(player))

        repository.save(game)

        val found = repository.findById(game.id)
        found.players.first().name shouldBe playerName
        found.players.first().passphrase shouldBe passphrase
    }
}


