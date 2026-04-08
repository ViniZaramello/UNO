package com.example.application.handler

import com.example.application.command.FlagLastCard
import com.example.application.factories.Domain
import com.example.application.ports.outbound.GameRepository
import com.example.application.shared.commonCardList
import com.example.configuration.NotFoundException
import io.github.serpro69.kfaker.Faker
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.ktor.server.plugins.BadRequestException
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.UUID

class FlagLastCardHandlerTest {

    private val faker = Faker()
    private val repository = mockk<GameRepository>()
    private lateinit var handler: FlagLastCardHandler

    @BeforeEach
    fun setup() {
        handler = FlagLastCardHandler(repository)
    }

    @Test
    fun `should set lastCard to true when player has exactly one card`() = runBlocking {
        val card = Domain.card()
        val player = Domain.player(name = faker.name.firstName(), passphrase = faker.name.lastName(), cards = mutableListOf(card))
        val game = Domain.game(players = mutableListOf(player))

        every { repository.findById(game.id) } returns game
        every { repository.save(game) } returns game

        handler.handle(FlagLastCard(gameId = game.id, playerName = player.name, passphrase = player.passphrase))

        player.lastCard shouldBe true
        verify(exactly = 1) { repository.save(game) }
    }

    @Test
    fun `should throw BadRequestException and add penalty cards when player has more than one card`() = runBlocking<Unit> {
        val player = Domain.player(name = faker.name.firstName(), passphrase = faker.name.lastName(), cards = commonCardList())
        val game = Domain.game(players = mutableListOf(player))
        val cardsBefore = player.cards.size

        every { repository.findById(game.id) } returns game
        every { repository.save(game) } returns game

        assertThrows<BadRequestException> {
            runBlocking { handler.handle(FlagLastCard(gameId = game.id, playerName = player.name, passphrase = player.passphrase)) }
        }

        player.cards.size shouldBe cardsBefore + 2
        player.lastCard shouldBe false
        verify(exactly = 1) { repository.save(game) }
    }

    @Test
    fun `should throw IllegalArgumentException when passphrase is wrong`() = runBlocking<Unit> {
        val card = Domain.card()
        val player = Domain.player(name = faker.name.firstName(), passphrase = faker.name.lastName(), cards = mutableListOf(card))
        val game = Domain.game(players = mutableListOf(player))

        every { repository.findById(game.id) } returns game

        assertThrows<IllegalArgumentException> {
            runBlocking { handler.handle(FlagLastCard(gameId = game.id, playerName = player.name, passphrase = "wrong")) }
        }
        verify(exactly = 0) { repository.save(any()) }
    }

    @Test
    fun `should throw NotFoundException when game does not exist`() = runBlocking<Unit> {
        val randomId = UUID.randomUUID()
        every { repository.findById(randomId) } throws NotFoundException("Game $randomId was not found.")

        assertThrows<NotFoundException> {
            runBlocking { handler.handle(FlagLastCard(gameId = randomId, playerName = faker.name.firstName(), passphrase = faker.name.lastName())) }
        }
        verify(exactly = 0) { repository.save(any()) }
    }

    @Test
    fun `should throw NotFoundException when player does not exist in game`() = runBlocking<Unit> {
        val game = Domain.game()
        every { repository.findById(game.id) } returns game

        assertThrows<NotFoundException> {
            runBlocking { handler.handle(FlagLastCard(gameId = game.id, playerName = "ghost", passphrase = "any")) }
        }
        verify(exactly = 0) { repository.save(any()) }
    }
}

