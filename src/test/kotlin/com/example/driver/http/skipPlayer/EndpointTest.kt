package com.example.driver.http.skipPlayer

import com.example.application.factories.Domain
import com.example.application.handler.SkipPlayerHandler
import com.example.application.model.GameStatus.FINISHED
import com.example.application.model.GameStatus.PLAYING
import com.example.application.model.PlayerStatus
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
    fun `Should return 204 when skipped player`() = testApplication {
        application { skipPlayerRoute(SkipPlayerHandler(repository)); module() }
        /**@Dado que exista um jogo já criado com 2 jogadores em partida*/
        val playerList = mutableListOf(
            Domain.player(status = PlayerStatus.PLAYING),
            Domain.player(name = "joca", passphrase = "xpto", status = PlayerStatus.PLAYING, number = 2)
        )
        val game = Domain.game(players = playerList, status = PLAYING, playerTurn = 2)
        game.initialCards()
        repository.save(game)

        val requestJson = Json.encodeToString(Request(
            gameId = game.id.toString(), playerName = game.players.first().name,
            passphrase = game.players.first().passphrase, targetPlayerName = "joca"
        ))

        val response = client.post("/game/skipPlayer") { contentType(ContentType.Application.Json); setBody(requestJson) }

        response.status shouldBe HttpStatusCode.OK
        val gameUpdated = repository.findById(game.id)
        gameUpdated.playerTurn shouldBe 1
        gameUpdated.findPlayer("Player").cards.size shouldBe 7
        gameUpdated.findPlayer("joca").cards.size shouldBe 9
    }

    @Test
    fun `Should return 400 Bad Request when gameStatus is different of PLAYING`() = testApplication {
        application { skipPlayerRoute(SkipPlayerHandler(repository)); configureExceptionHandling(); module() }
        val players = mutableListOf(Domain.player(), Domain.player(name = "joca", passphrase = "xpto"))
        val game = Domain.game(status = FINISHED, players = players)
        repository.save(game)

        val requestJson = Json.encodeToString(Request(
            gameId = game.id.toString(), playerName = game.players.first().name,
            passphrase = game.players.first().passphrase, targetPlayerName = "joca"
        ))

        val response = client.post("/game/skipPlayer") { contentType(ContentType.Application.Json); setBody(requestJson) }

        response.status shouldBe HttpStatusCode.BadRequest
        Json.decodeFromString<Map<String, String>>(response.bodyAsText())["message"] shouldBe "The game must be started before you can play."
        val gameUpdated = repository.findById(game.id)
        gameUpdated.playerTurn shouldBe 1
        gameUpdated.findPlayer("joca").cards.size shouldBe 0
    }

    @Test
    fun `Should return 400 Bad Request when gameId is invalid`() = testApplication {
        application { skipPlayerRoute(SkipPlayerHandler(repository)); configureExceptionHandling(); module() }
        val playerList = mutableListOf(
            Domain.player(status = PlayerStatus.PLAYING, owner = false),
            Domain.player(name = "joca", passphrase = "xpto", status = PlayerStatus.PLAYING, number = 2)
        )
        val game = Domain.game(players = playerList, status = PLAYING, playerTurn = 2)
        game.initialCards()
        repository.save(game)

        val invalidUuid = randomUUID().toString()
        val requestJson = Json.encodeToString(Request(
            gameId = invalidUuid, playerName = game.players.first().name,
            passphrase = game.players.first().passphrase, targetPlayerName = "joca"
        ))

        val response = client.post("/game/skipPlayer") { contentType(ContentType.Application.Json); setBody(requestJson) }

        response.status shouldBe HttpStatusCode.BadRequest
        Json.decodeFromString<Map<String, String>>(response.bodyAsText())["message"] shouldBe "Game $invalidUuid was not found."
        val gameUpdated = repository.findById(game.id)
        gameUpdated.playerTurn shouldBe 2
        gameUpdated.findPlayer("joca").cards.size shouldBe 7
    }

    @Test
    fun `Should return 400 Bad Request when player name is invalid`() = testApplication {
        application { skipPlayerRoute(SkipPlayerHandler(repository)); configureExceptionHandling(); module() }
        val playerList = mutableListOf(
            Domain.player(status = PlayerStatus.PLAYING, owner = false),
            Domain.player(name = "joca", passphrase = "xpto", status = PlayerStatus.PLAYING, number = 2)
        )
        val game = Domain.game(players = playerList, status = PLAYING, playerTurn = 2)
        game.initialCards()
        repository.save(game)

        val requestJson = Json.encodeToString(Request(
            gameId = game.id.toString(), playerName = "lineuzinho",
            passphrase = game.players.first().passphrase, targetPlayerName = "joca"
        ))

        val response = client.post("/game/skipPlayer") { contentType(ContentType.Application.Json); setBody(requestJson) }

        response.status shouldBe HttpStatusCode.BadRequest
        Json.decodeFromString<Map<String, String>>(response.bodyAsText())["message"] shouldBe "Player lineuzinho was not found in the game."
        val gameUpdated = repository.findById(game.id)
        gameUpdated.playerTurn shouldBe 2
        gameUpdated.findPlayer("joca").cards.size shouldBe 7
    }

    @Test
    fun `Should return 400 Bad Request when target player is invalid`() = testApplication {
        application { skipPlayerRoute(SkipPlayerHandler(repository)); configureExceptionHandling(); module() }
        val playerList = mutableListOf(
            Domain.player(status = PlayerStatus.PLAYING, owner = true),
            Domain.player(name = "joca", passphrase = "xpto", status = PlayerStatus.PLAYING, number = 2)
        )
        val game = Domain.game(players = playerList, status = PLAYING, playerTurn = 2)
        game.initialCards()
        repository.save(game)

        val requestJson = Json.encodeToString(Request(
            gameId = game.id.toString(), playerName = game.players.first().name,
            passphrase = game.players.first().passphrase, targetPlayerName = "josefa"
        ))

        val response = client.post("/game/skipPlayer") { contentType(ContentType.Application.Json); setBody(requestJson) }

        response.status shouldBe HttpStatusCode.BadRequest
        Json.decodeFromString<Map<String, String>>(response.bodyAsText())["message"] shouldBe "Player josefa was not found in the game."
        repository.findById(game.id).playerTurn shouldBe 2
    }

    @Test
    fun `Should return 400 Bad Request and passphrase required when request is invalid`() = testApplication {
        application { skipPlayerRoute(SkipPlayerHandler(repository)); configureExceptionHandling(); module() }
        val playerList = mutableListOf(
            Domain.player(status = PlayerStatus.PLAYING, owner = false),
            Domain.player(name = "joca", passphrase = "xpto", status = PlayerStatus.PLAYING, number = 2)
        )
        val game = Domain.game(players = playerList, status = PLAYING, playerTurn = 2)
        game.initialCards()
        repository.save(game)

        val requestJson = Json.encodeToString(Request(
            gameId = game.id.toString(), playerName = game.players.first().name,
            passphrase = "xpto", targetPlayerName = "joca"
        ))

        val response = client.post("/game/skipPlayer") { contentType(ContentType.Application.Json); setBody(requestJson) }

        response.status shouldBe HttpStatusCode.BadRequest
        Json.decodeFromString<Map<String, String>>(response.bodyAsText())["message"] shouldBe "Invalid passphrase."
        val gameUpdated = repository.findById(game.id)
        gameUpdated.playerTurn shouldBe 2
        gameUpdated.findPlayer("joca").cards.size shouldBe 7
    }

    @Test
    fun `Should return 400 Bad Request when player is not owner`() = testApplication {
        application { skipPlayerRoute(SkipPlayerHandler(repository)); configureExceptionHandling(); module() }
        val playerList = mutableListOf(
            Domain.player(status = PlayerStatus.PLAYING, owner = false),
            Domain.player(name = "joca", passphrase = "xpto", status = PlayerStatus.PLAYING, number = 2)
        )
        val game = Domain.game(players = playerList, status = PLAYING, playerTurn = 2)
        game.initialCards()
        repository.save(game)

        val requestJson = Json.encodeToString(Request(
            gameId = game.id.toString(), playerName = playerList.first().name,
            passphrase = playerList.first().passphrase, targetPlayerName = "joca"
        ))

        val response = client.post("/game/skipPlayer") { contentType(ContentType.Application.Json); setBody(requestJson) }

        response.status shouldBe HttpStatusCode.BadRequest
        Json.decodeFromString<Map<String, String>>(response.bodyAsText())["message"] shouldBe "Player ${playerList.first().name} is not the owner of the game."
        val gameUpdated = repository.findById(game.id)
        gameUpdated.playerTurn shouldBe 2
        gameUpdated.findPlayer("joca").cards.size shouldBe 7
    }

    @Test
    fun `Should return 400 Bad Request when it is not the target player turn`() = testApplication {
        application { skipPlayerRoute(SkipPlayerHandler(repository)); configureExceptionHandling(); module() }
        val playerList = mutableListOf(
            Domain.player(status = PlayerStatus.PLAYING, owner = false),
            Domain.player(name = "joca", passphrase = "xpto", status = PlayerStatus.PLAYING, number = 2)
        )
        val game = Domain.game(players = playerList, status = PLAYING, playerTurn = 1)
        game.initialCards()
        repository.save(game)

        val requestJson = Json.encodeToString(Request(
            gameId = game.id.toString(), playerName = game.players.first().name,
            passphrase = game.players.first().passphrase, targetPlayerName = "joca"
        ))

        val response = client.post("/game/skipPlayer") { contentType(ContentType.Application.Json); setBody(requestJson) }

        response.status shouldBe HttpStatusCode.BadRequest
        Json.decodeFromString<Map<String, String>>(response.bodyAsText())["message"] shouldBe "Target player is not in turn."
        repository.findById(game.id).findPlayer("joca").cards.size shouldBe 7
    }
}
