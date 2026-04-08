package com.example.driver.http.passTurn

import com.example.application.factories.Domain
import com.example.application.handler.PassTurnHandler
import com.example.application.model.GameStatus.PLAYING
import com.example.application.model.PlayerStatus
import com.example.application.shared.commonCardList
import com.example.configuration.configureExceptionHandling
import com.example.driven.InMemoryGameRepository
import com.example.module
import io.kotest.matchers.shouldBe
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.testApplication
import java.util.UUID.randomUUID
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class EndpointTest {

    private val repository = InMemoryGameRepository()

    @AfterEach
    fun after() { repository.clear() }

    @BeforeEach
    fun before() { repository.clear() }

    @Test
    fun `Should return 204 when player pass turn`() = testApplication {
        application { passTurnRoute(PassTurnHandler(repository)); module() }
        val playerList = mutableListOf(
            Domain.player(status = PlayerStatus.PLAYING, owner = false, buyParityCard = true),
            Domain.player(name = "joca", passphrase = "xpto", status = PlayerStatus.PLAYING, number = 2, cards = commonCardList())
        )
        val cardInHand = Domain.card()
        val game = Domain.game(players = playerList, status = PLAYING)
        game.players.first().cards.add(cardInHand)
        repository.save(game)

        val requestJson = Json.encodeToString(Request(
            gameId = game.id.toString(), playerName = game.players.first().name, passphrase = game.players.first().passphrase
        ))

        val response = client.post("/player/passTurn") { contentType(ContentType.Application.Json); setBody(requestJson) }

        response.status shouldBe HttpStatusCode.NoContent
        val gameUpdated = repository.findById(game.id)
        gameUpdated.playerTurn shouldBe 2
        gameUpdated.findPlayer("Player").buyParityCard shouldBe false
    }

    @Test
    fun `Should return 400 when player try skip with not buy a card`() = testApplication {
        application { passTurnRoute(PassTurnHandler(repository)); configureExceptionHandling(); module() }
        val playerList = mutableListOf(
            Domain.player(status = PlayerStatus.PLAYING, owner = false, cards = commonCardList()),
            Domain.player(name = "joca", passphrase = "xpto", status = PlayerStatus.PLAYING, number = 2, cards = commonCardList())
        )
        val game = Domain.game(players = playerList, status = PLAYING)
        repository.save(game)

        val requestJson = Json.encodeToString(Request(
            gameId = game.id.toString(), playerName = game.players.first().name, passphrase = game.players.first().passphrase
        ))

        val response = client.post("/player/passTurn") { contentType(ContentType.Application.Json); setBody(requestJson) }

        response.status shouldBe HttpStatusCode.BadRequest
        Json.decodeFromString<Map<String, String>>(response.bodyAsText())["message"] shouldBe "You must draw a card before skipping your turn."
        repository.findById(game.id).playerTurn shouldBe 1
    }

    @Test
    fun `Should return 400 Bad Request when gameId is invalid`() = testApplication {
        application { passTurnRoute(PassTurnHandler(repository)); configureExceptionHandling(); module() }
        val playerList = mutableListOf(
            Domain.player(status = PlayerStatus.PLAYING, owner = false),
            Domain.player(name = "joca", passphrase = "xpto", status = PlayerStatus.PLAYING, number = 2)
        )
        val game = Domain.game(players = playerList, status = PLAYING, playerTurn = 2)
        game.initialCards()
        repository.save(game)

        val invalidUuid = randomUUID().toString()
        val requestJson = Json.encodeToString(Request(
            gameId = invalidUuid, playerName = game.players.first().name, passphrase = game.players.first().passphrase
        ))

        val response = client.post("/player/passTurn") { contentType(ContentType.Application.Json); setBody(requestJson) }

        response.status shouldBe HttpStatusCode.BadRequest
        Json.decodeFromString<Map<String, String>>(response.bodyAsText())["message"] shouldBe "Game $invalidUuid was not found."
        val gameUpdated = repository.findById(game.id)
        gameUpdated.playerTurn shouldBe 2
        gameUpdated.findPlayer("joca").cards.size shouldBe 7
    }

    @Test
    fun `Should return 400 Bad Request when player name is invalid`() = testApplication {
        application { passTurnRoute(PassTurnHandler(repository)); configureExceptionHandling(); module() }
        val playerList = mutableListOf(
            Domain.player(status = PlayerStatus.PLAYING, owner = false),
            Domain.player(name = "joca", passphrase = "xpto", status = PlayerStatus.PLAYING, number = 2)
        )
        val game = Domain.game(players = playerList, status = PLAYING, playerTurn = 2)
        game.initialCards()
        repository.save(game)

        val requestJson = Json.encodeToString(Request(
            gameId = game.id.toString(), playerName = "lineuzinho", passphrase = game.players.first().passphrase
        ))

        val response = client.post("/player/passTurn") { contentType(ContentType.Application.Json); setBody(requestJson) }

        response.status shouldBe HttpStatusCode.BadRequest
        Json.decodeFromString<Map<String, String>>(response.bodyAsText())["message"] shouldBe "Player lineuzinho was not found in the game."
        val gameUpdated = repository.findById(game.id)
        gameUpdated.playerTurn shouldBe 2
        gameUpdated.findPlayer("joca").cards.size shouldBe 7
    }

    @Test
    fun `Should return 400 Bad Request and passphrase required when request is invalid`() = testApplication {
        application { passTurnRoute(PassTurnHandler(repository)); configureExceptionHandling(); module() }
        val playerList = mutableListOf(
            Domain.player(status = PlayerStatus.PLAYING, owner = false),
            Domain.player(name = "joca", passphrase = "xpto", status = PlayerStatus.PLAYING, number = 2)
        )
        val game = Domain.game(players = playerList, status = PLAYING, playerTurn = 2)
        game.initialCards()
        repository.save(game)

        val requestJson = Json.encodeToString(Request(
            gameId = game.id.toString(), playerName = game.players.first().name, passphrase = "xpto"
        ))

        val response = client.post("/player/passTurn") { contentType(ContentType.Application.Json); setBody(requestJson) }

        response.status shouldBe HttpStatusCode.BadRequest
        Json.decodeFromString<Map<String, String>>(response.bodyAsText())["message"] shouldBe "Invalid passphrase."
        val gameUpdated = repository.findById(game.id)
        gameUpdated.playerTurn shouldBe 2
        gameUpdated.findPlayer("joca").cards.size shouldBe 7
    }
}
