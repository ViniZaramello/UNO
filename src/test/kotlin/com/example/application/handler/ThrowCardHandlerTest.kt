package com.example.application.handler

import com.example.application.command.ThrowCard
import com.example.application.factories.Domain
import com.example.application.model.Colors
import com.example.application.model.GameStatus
import com.example.application.model.PlayerStatus
import com.example.application.model.SpecialType
import com.example.application.model.vo.StackCards
import com.example.application.ports.outbound.GameRepository
import com.example.application.shared.commonCardList
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

class ThrowCardHandlerTest {

    private val faker = Faker()
    private val repository = mockk<GameRepository>()
    private lateinit var handler: ThrowCardHandler

    @BeforeEach
    fun setup() {
        handler = ThrowCardHandler(repository)
    }

    private fun playingGame(playerTurn: Int = 1, stacksSpy: StackCards = spyk(StackCards())): com.example.application.model.Game {
        val p1 = Domain.player(name = faker.name.firstName(), passphrase = faker.name.lastName(), number = 1, status = PlayerStatus.PLAYING, cards = commonCardList())
        val p2 = Domain.player(name = faker.name.firstName(), passphrase = faker.name.lastName(), number = 2, status = PlayerStatus.PLAYING, cards = commonCardList())
        val game = Domain.game(players = mutableListOf(p1, p2), status = GameStatus.PLAYING, playerTurn = playerTurn, stacks = stacksSpy)
        game.stacks.cardsInTable.add(Domain.card(name = "One red", number = "1", color = Colors.RED, special = SpecialType.NONE))
        return game
    }

    @Test
    fun `should remove card from hand, add to table, and pass turn`() = runBlocking {
        val game = playingGame()
        val player = game.players.first()
        val cardToThrow = Domain.card(name = "Two red", number = "2", color = Colors.RED)
        player.cards.add(cardToThrow)

        every { repository.findById(game.id) } returns game
        every { repository.save(game) } returns game

        handler.handle(ThrowCard(gameId = game.id, playerName = player.name, passphrase = player.passphrase, cardId = UUID.fromString(cardToThrow.id)))

        game.playerTurn shouldBe 2
        player.cards.any { it.id == cardToThrow.id } shouldBe false
        game.stacks.cardsInTable.last().id shouldBe cardToThrow.id
        verify(exactly = 1) { repository.save(game) }
    }

    @Test
    fun `should set player status to FINISHED when last card is thrown with flag set`() = runBlocking {
        val p1 = Domain.player(name = faker.name.firstName(), passphrase = faker.name.lastName(), number = 1, status = PlayerStatus.PLAYING, lastCard = true)
        val p2 = Domain.player(name = faker.name.firstName(), passphrase = faker.name.lastName(), number = 2, status = PlayerStatus.PLAYING, cards = commonCardList())
        val game = Domain.game(players = mutableListOf(p1, p2), status = GameStatus.PLAYING, playerTurn = 1)
        val card = Domain.card(name = "One red", number = "1", color = Colors.RED)
        game.stacks.cardsInTable.add(Domain.card(name = "One red", number = "1", color = Colors.RED))
        p1.cards.add(card)

        every { repository.findById(game.id) } returns game
        every { repository.save(game) } returns game

        handler.handle(ThrowCard(gameId = game.id, playerName = p1.name, passphrase = p1.passphrase, cardId = UUID.fromString(card.id)))

        p1.cards.size shouldBe 0
        p1.statusInGame shouldBe PlayerStatus.FINISHED
    }

    @Test
    fun `should throw IllegalArgumentException when it is not the player's turn`() = runBlocking<Unit> {
        val game = playingGame(playerTurn = 2)
        val player = game.players.first()
        val card = player.cards.first()

        every { repository.findById(game.id) } returns game

        assertThrows<IllegalArgumentException> {
            runBlocking { handler.handle(ThrowCard(gameId = game.id, playerName = player.name, passphrase = player.passphrase, cardId = UUID.fromString(card.id))) }
        }
        verify(exactly = 0) { repository.save(any()) }
    }

    @Test
    fun `should throw IllegalArgumentException when game status is not PLAYING`() = runBlocking<Unit> {
        val p1 = Domain.player(name = faker.name.firstName(), passphrase = faker.name.lastName(), number = 1, cards = commonCardList())
        val p2 = Domain.player(name = faker.name.firstName(), passphrase = faker.name.lastName(), number = 2, cards = commonCardList())
        val game = Domain.game(players = mutableListOf(p1, p2), status = GameStatus.FINISHED)
        game.stacks.cardsInTable.add(Domain.card())
        val card = p1.cards.first()

        every { repository.findById(game.id) } returns game

        assertThrows<IllegalArgumentException> {
            runBlocking { handler.handle(ThrowCard(gameId = game.id, playerName = p1.name, passphrase = p1.passphrase, cardId = UUID.fromString(card.id))) }
        }
        verify(exactly = 0) { repository.save(any()) }
    }

    @Test
    fun `should throw IllegalArgumentException when passphrase is wrong`() = runBlocking<Unit> {
        val game = playingGame()
        val player = game.players.first()
        val card = player.cards.first()

        every { repository.findById(game.id) } returns game

        assertThrows<IllegalArgumentException> {
            runBlocking { handler.handle(ThrowCard(gameId = game.id, playerName = player.name, passphrase = "wrong", cardId = UUID.fromString(card.id))) }
        }
        verify(exactly = 0) { repository.save(any()) }
    }

    @Test
    fun `should throw IllegalArgumentException when card has no parity with table card`() = runBlocking<Unit> {
        val game = playingGame()
        val player = game.players.first()
        val nonParityCard = Domain.card(name = "Eight blue", number = "8", color = Colors.BLUE, special = SpecialType.NONE)
        player.cards.add(0, nonParityCard)

        every { repository.findById(game.id) } returns game

        assertThrows<IllegalArgumentException> {
            runBlocking { handler.handle(ThrowCard(gameId = game.id, playerName = player.name, passphrase = player.passphrase, cardId = UUID.fromString(nonParityCard.id))) }
        }
        verify(exactly = 0) { repository.save(any()) }
    }

    @Test
    fun `should throw IllegalArgumentException when card not in player hand`() = runBlocking<Unit> {
        val game = playingGame()
        val player = game.players.first()

        every { repository.findById(game.id) } returns game

        assertThrows<IllegalArgumentException> {
            runBlocking { handler.handle(ThrowCard(gameId = game.id, playerName = player.name, passphrase = player.passphrase, cardId = UUID.randomUUID())) }
        }
        verify(exactly = 0) { repository.save(any()) }
    }

    @Test
    fun `should throw NotFoundException when game does not exist`() = runBlocking<Unit> {
        val randomId = UUID.randomUUID()
        every { repository.findById(randomId) } throws NotFoundException("Game $randomId was not found.")

        assertThrows<NotFoundException> {
            runBlocking { handler.handle(ThrowCard(gameId = randomId, playerName = faker.name.firstName(), passphrase = faker.name.lastName(), cardId = UUID.randomUUID())) }
        }
        verify(exactly = 0) { repository.save(any()) }
    }

    @Test
    fun `should throw NotFoundException when player does not exist in game`() = runBlocking<Unit> {
        val game = playingGame()
        every { repository.findById(game.id) } returns game

        assertThrows<NotFoundException> {
            runBlocking { handler.handle(ThrowCard(gameId = game.id, playerName = "ghost", passphrase = "any", cardId = UUID.randomUUID())) }
        }
        verify(exactly = 0) { repository.save(any()) }
    }

    @Test
    fun `should throw IllegalArgumentException when color is invalid for black cards`() = runBlocking<Unit> {
        val game = playingGame()
        val player = game.players.first()
        val changeColorCard = Domain.card(name = "change-color", number = "changeColor", color = Colors.BLACK, special = SpecialType.CHANGE_COLOR)
        player.cards.add(changeColorCard)

        every { repository.findById(game.id) } returns game

        assertThrows<IllegalArgumentException> {
            runBlocking { handler.handle(ThrowCard(gameId = game.id, playerName = player.name, passphrase = player.passphrase, cardId = UUID.fromString(changeColorCard.id), color = "purple")) }
        }
        verify(exactly = 0) { repository.save(any()) }
    }
}
