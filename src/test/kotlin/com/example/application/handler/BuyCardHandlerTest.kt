package com.example.application.handler

import com.example.application.command.BuyCard
import com.example.application.factories.Domain
import com.example.application.model.Card
import com.example.application.model.Colors
import com.example.application.model.GameStatus
import com.example.application.model.PlayerStatus
import com.example.application.model.SpecialType
import com.example.application.model.vo.StackCards
import com.example.application.ports.outbound.GameRepository
import com.example.configuration.NotFoundException
import io.github.serpro69.kfaker.Faker
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.UUID

class BuyCardHandlerTest {

    private val faker = Faker()
    private val repository = mockk<GameRepository>()
    private lateinit var handler: BuyCardHandler

    @BeforeEach
    fun setup() {
        handler = BuyCardHandler(repository)
    }

    private fun twoPlayerPlayingGame(playerTurn: Int = 1): Triple<com.example.application.model.Game, com.example.application.model.Player, StackCards> {
        val stacksSpy = spyk(StackCards())
        val player1 = Domain.player(name = faker.name.firstName(), passphrase = faker.name.lastName(), number = 1, status = PlayerStatus.PLAYING)
        val player2 = Domain.player(name = faker.name.firstName(), passphrase = faker.name.lastName(), number = 2, status = PlayerStatus.PLAYING)
        val game = Domain.game(players = mutableListOf(player1, player2), status = GameStatus.PLAYING, playerTurn = playerTurn, stacks = stacksSpy)
        game.initialCards()
        return Triple(game, player1, stacksSpy)
    }

    @Test
    fun `should add drawn card to player hand and return it`() = runBlocking {
        val (game, player, stacks) = twoPlayerPlayingGame()
        val drawnCard = Domain.card(name = "One red", number = "1", color = Colors.RED)
        val cardsBefore = player.cards.size

        every { repository.findById(game.id) } returns game
        every { repository.save(game) } returns game
        every { stacks.getRandomCard() } returns drawnCard

        val result = handler.handle(BuyCard(gameId = game.id, playerName = player.name, passphrase = player.passphrase))

        result shouldBe drawnCard
        player.cards.size shouldBe cardsBefore + 1
    }

    @Test
    fun `should pass turn when drawn card has no parity with table card`() = runBlocking {
        val (game, player, stacks) = twoPlayerPlayingGame()
        val nonMatchingCard = Domain.card(name = "Eight blue", number = "8", color = Colors.BLUE)
        game.stacks.cardsInTable.add(Domain.card(name = "One red", number = "1", color = Colors.RED))

        every { repository.findById(game.id) } returns game
        every { repository.save(game) } returns game
        every { stacks.getRandomCard() } returns nonMatchingCard

        handler.handle(BuyCard(gameId = game.id, playerName = player.name, passphrase = player.passphrase))

        game.playerTurn shouldBe 2
        player.buyParityCard shouldBe false
    }

    @Test
    fun `should set buyParityCard true and keep turn when drawn card matches table card`() = runBlocking {
        val (game, player, stacks) = twoPlayerPlayingGame()
        val matchingCard = Domain.card(name = "Three red", number = "3", color = Colors.RED)
        game.stacks.cardsInTable.add(Domain.card(name = "One red", number = "1", color = Colors.RED))

        every { repository.findById(game.id) } returns game
        every { repository.save(game) } returns game
        every { stacks.getRandomCard() } returns matchingCard

        handler.handle(BuyCard(gameId = game.id, playerName = player.name, passphrase = player.passphrase))

        game.playerTurn shouldBe 1
        player.buyParityCard shouldBe true
    }

    @Test
    fun `should throw IllegalStateException and pass turn when player has already bought a parity card`() = runBlocking<Unit> {
        val player = Domain.player(name = faker.name.firstName(), passphrase = faker.name.lastName(), number = 1, status = PlayerStatus.PLAYING, buyParityCard = true)
        val other = Domain.player(name = faker.name.firstName(), passphrase = faker.name.lastName(), number = 2, status = PlayerStatus.PLAYING)
        val game = Domain.game(players = mutableListOf(player, other), status = GameStatus.PLAYING, playerTurn = 1)
        game.initialCards()

        every { repository.findById(game.id) } returns game
        every { repository.save(game) } returns game

        assertThrows<IllegalStateException> {
            runBlocking { handler.handle(BuyCard(gameId = game.id, playerName = player.name, passphrase = player.passphrase)) }
        }

        game.playerTurn shouldBe 2
        player.buyParityCard shouldBe false
        verify(exactly = 1) { repository.save(game) }
    }

    @Test
    fun `should throw IllegalArgumentException when it is not the player's turn`() = runBlocking<Unit> {
        val (game, _, _) = twoPlayerPlayingGame(playerTurn = 2)
        val player = game.players.first()

        every { repository.findById(game.id) } returns game

        assertThrows<IllegalArgumentException> {
            runBlocking { handler.handle(BuyCard(gameId = game.id, playerName = player.name, passphrase = player.passphrase)) }
        }
        verify(exactly = 0) { repository.save(any()) }
    }

    @Test
    fun `should throw IllegalArgumentException when game is not PLAYING`() = runBlocking<Unit> {
        val player = Domain.player(name = faker.name.firstName(), passphrase = faker.name.lastName(), number = 1)
        val game = Domain.game(players = mutableListOf(player), status = GameStatus.WAITING, playerTurn = 1)

        every { repository.findById(game.id) } returns game

        assertThrows<IllegalArgumentException> {
            runBlocking { handler.handle(BuyCard(gameId = game.id, playerName = player.name, passphrase = player.passphrase)) }
        }
        verify(exactly = 0) { repository.save(any()) }
    }

    @Test
    fun `should throw IllegalArgumentException when passphrase is wrong`() = runBlocking<Unit> {
        val (game, player, _) = twoPlayerPlayingGame()

        every { repository.findById(game.id) } returns game

        assertThrows<IllegalArgumentException> {
            runBlocking { handler.handle(BuyCard(gameId = game.id, playerName = player.name, passphrase = "wrong")) }
        }
        verify(exactly = 0) { repository.save(any()) }
    }

    @Test
    fun `should throw NotFoundException when game does not exist`() = runBlocking<Unit> {
        val randomId = UUID.randomUUID()
        every { repository.findById(randomId) } throws NotFoundException("Game $randomId was not found.")

        assertThrows<NotFoundException> {
            runBlocking { handler.handle(BuyCard(gameId = randomId, playerName = faker.name.firstName(), passphrase = faker.name.lastName())) }
        }
        verify(exactly = 0) { repository.save(any()) }
    }

    @Test
    fun `should throw NotFoundException when player does not exist in game`() = runBlocking<Unit> {
        val (game, _, _) = twoPlayerPlayingGame()
        every { repository.findById(game.id) } returns game

        assertThrows<NotFoundException> {
            runBlocking { handler.handle(BuyCard(gameId = game.id, playerName = "ghost", passphrase = "any")) }
        }
        verify(exactly = 0) { repository.save(any()) }
    }

    @Test
    fun `should save game exactly once on success`() = runBlocking {
        val (game, player, stacks) = twoPlayerPlayingGame()
        every { repository.findById(game.id) } returns game
        every { repository.save(game) } returns game
        every { stacks.getRandomCard() } returns Domain.card()

        handler.handle(BuyCard(gameId = game.id, playerName = player.name, passphrase = player.passphrase))

        verify(exactly = 1) { repository.save(game) }
    }
}

