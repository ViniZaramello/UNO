package com.example.application.handler

import com.example.application.command.QuitGame
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

class QuitGameHandlerTest {

    private val faker = Faker()
    private val repository = mockk<GameRepository>()
    private lateinit var handler: QuitGameHandler

    @BeforeEach
    fun setup() {
        handler = QuitGameHandler(repository)
    }

    private fun threePlayerGame(status: GameStatus = GameStatus.CREATED): Pair<com.example.application.model.Game, List<Player>> {
        val p1 = Domain.player(name = faker.name.firstName(), passphrase = faker.name.lastName(), owner = true, number = 1)
        val p2 = Domain.player(name = faker.name.firstName(), passphrase = faker.name.lastName(), owner = false, number = 2)
        val p3 = Domain.player(name = faker.name.firstName(), passphrase = faker.name.lastName(), owner = false, number = 3)
        val game = Domain.game(players = mutableListOf(p1, p2, p3), status = status)
        return game to listOf(p1, p2, p3)
    }

    @Test
    fun `should remove player from game and save`() = runBlocking {
        val (game, players) = threePlayerGame()
        val owner = players[0]

        every { repository.findById(game.id) } returns game
        every { repository.save(game) } returns game

        handler.handle(QuitGame(gameId = game.id, playerName = owner.name, passphrase = owner.passphrase))

        game.players.any { it.name == owner.name } shouldBe false
        verify(exactly = 1) { repository.save(game) }
    }

    @Test
    fun `should reorder player numbers after quit`() = runBlocking {
        val (game, players) = threePlayerGame()
        val owner = players[0]

        every { repository.findById(game.id) } returns game
        every { repository.save(game) } returns game

        handler.handle(QuitGame(gameId = game.id, playerName = owner.name, passphrase = owner.passphrase))

        game.players.size shouldBe 2
        game.players[0].number shouldBe 1
        game.players[1].number shouldBe 2
    }

    @Test
    fun `should assign owner to first remaining player when owner quits`() = runBlocking {
        val (game, players) = threePlayerGame()
        val owner = players[0]
        val nextPlayer = players[1]

        every { repository.findById(game.id) } returns game
        every { repository.save(game) } returns game

        handler.handle(QuitGame(gameId = game.id, playerName = owner.name, passphrase = owner.passphrase))

        game.findPlayer(nextPlayer.name).owner shouldBe true
    }

    @Test
    fun `should not reassign owner when non-owner quits`() = runBlocking {
        val (game, players) = threePlayerGame()
        val nonOwner = players[1]
        val owner = players[0]

        every { repository.findById(game.id) } returns game
        every { repository.save(game) } returns game

        handler.handle(QuitGame(gameId = game.id, playerName = nonOwner.name, passphrase = nonOwner.passphrase))

        game.findPlayer(owner.name).owner shouldBe true
    }

    @Test
    fun `should transfer cards back to deck when game is PLAYING`() = runBlocking {
        val (game, players) = threePlayerGame(status = GameStatus.PLAYING)
        game.initialCards()
        val owner = players[0]
        val deckSizeBeforeQuit = game.stacks.cardsInDeck.size + owner.cards.size

        every { repository.findById(game.id) } returns game
        every { repository.save(game) } returns game

        handler.handle(QuitGame(gameId = game.id, playerName = owner.name, passphrase = owner.passphrase))

        game.stacks.cardsInDeck.size shouldBe deckSizeBeforeQuit
    }

    @Test
    fun `should not transfer cards when game is not PLAYING`() = runBlocking {
        val (game, players) = threePlayerGame(status = GameStatus.CREATED)
        val owner = players[0]
        owner.cards.addAll(Domain.game().stacks.getRandomCards(3))
        val deckSizeBefore = game.stacks.cardsInDeck.size

        every { repository.findById(game.id) } returns game
        every { repository.save(game) } returns game

        handler.handle(QuitGame(gameId = game.id, playerName = owner.name, passphrase = owner.passphrase))

        game.stacks.cardsInDeck.size shouldBe deckSizeBefore
    }

    @Test
    fun `should throw NotFoundException when game does not exist`() = runBlocking<Unit> {
        val randomId = UUID.randomUUID()
        every { repository.findById(randomId) } throws NotFoundException("Game $randomId was not found.")

        assertThrows<NotFoundException> {
            runBlocking { handler.handle(QuitGame(gameId = randomId, playerName = faker.name.firstName(), passphrase = faker.name.lastName())) }
        }

        verify(exactly = 0) { repository.save(any()) }
    }

    @Test
    fun `should throw NotFoundException when player does not exist in game`() = runBlocking<Unit> {
        val (game, _) = threePlayerGame()
        every { repository.findById(game.id) } returns game

        assertThrows<NotFoundException> {
            runBlocking { handler.handle(QuitGame(gameId = game.id, playerName = "ghost_player", passphrase = "any")) }
        }

        verify(exactly = 0) { repository.save(any()) }
    }

    @Test
    fun `should throw IllegalArgumentException when passphrase is wrong`() = runBlocking<Unit> {
        val (game, players) = threePlayerGame()
        val owner = players[0]
        every { repository.findById(game.id) } returns game

        assertThrows<IllegalArgumentException> {
            runBlocking { handler.handle(QuitGame(gameId = game.id, playerName = owner.name, passphrase = "wrong_passphrase")) }
        }

        verify(exactly = 0) { repository.save(any()) }
    }

    @Test
    fun `should not save when an error occurs`() = runBlocking<Unit> {
        val (game, _) = threePlayerGame()
        every { repository.findById(game.id) } returns game

        assertThrows<NotFoundException> {
            runBlocking { handler.handle(QuitGame(gameId = game.id, playerName = "ghost_player", passphrase = "any")) }
        }

        verify(exactly = 0) { repository.save(any()) }
    }
}

