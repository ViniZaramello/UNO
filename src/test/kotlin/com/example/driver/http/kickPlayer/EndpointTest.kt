package com.example.driver.http.kickPlayer

import com.example.application.factories.Domain
import com.example.application.handler.KickPlayerHandler
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
    fun after() {
        repository.clear()
    }

    @BeforeEach
    fun before() {
        repository.clear()
    }

    @Test
    fun `Should return 204 when kick the player`() = testApplication {
        application {
            kickPlayerRoute(KickPlayerHandler(repository))
            configureExceptionHandling()
            module()
        }
        /**@Dado que exista um jogo já criado com 3 jogadores*/
        val playerList = mutableListOf(
            Domain.player(),
            Domain.player(name = "joca", passphrase = "xpto", number = 2),
            Domain.player(name = "loren", passphrase = "xpto", number = 3)
        )
        val game = Domain.game(players = playerList)
        repository.save(game)

        val requestJson = Json.encodeToString(
            Request(
                gameId = game.id.toString(),
                playerName = game.players.first().name,
                passphrase = game.players.first().passphrase,
                targetPlayerName = "joca"
            )
        )

        /**@Quando for feito uma requisição para expulsar um jogador*/
        val response = client.post("/game/kickPlayer") {
            contentType(ContentType.Application.Json)
            setBody(requestJson)
        }

        /**@Então deverá retornar o status 204*/
        response.status shouldBe HttpStatusCode.NoContent

        /**@E deverá constar apenas 2 jogadores no jogo */
        val gameUpdated = repository.findById(game.id)
        gameUpdated.players.size shouldBe 2

        /**@E o numero do primeiro jogador da lista não deverá ser alterado */
        val firstPlayer = gameUpdated.findPlayer("Player")
        firstPlayer.number shouldBe 1

        /**@E o numero do ultimo jogador da lista deverá ser alterado */
        val secondPlayer = gameUpdated.findPlayer("loren")
        secondPlayer.number shouldBe 2
    }

    @Test
    fun `Should return 204 when kick the player while it is game in progress`() = testApplication {
        application {
            kickPlayerRoute(KickPlayerHandler(repository))
            configureExceptionHandling()
            module()
        }
        /**@Dado que exista um jogo já criado com 3 jogadores em partida*/
        val playerList = mutableListOf(
            Domain.player(status = PlayerStatus.PLAYING),
            Domain.player(name = "joca", passphrase = "xpto", status = PlayerStatus.PLAYING, number = 2),
            Domain.player(name = "loren", passphrase = "xpto", status = PlayerStatus.PLAYING, number = 3)
        )
        val game = Domain.game(players = playerList, status = PLAYING)
        game.initialCards()
        repository.save(game)

        val requestJson = Json.encodeToString(
            Request(
                gameId = game.id.toString(),
                playerName = game.players.first().name,
                passphrase = game.players.first().passphrase,
                targetPlayerName = "joca"
            )
        )

        val response = client.post("/game/kickPlayer") {
            contentType(ContentType.Application.Json)
            setBody(requestJson)
        }

        response.status shouldBe HttpStatusCode.NoContent

        val gameUpdated = repository.findById(game.id)
        gameUpdated.players.size shouldBe 2

        /**@E as cartas que estavam na mão do jogador expulsado deverá retornar a pilha de cartas*/
        gameUpdated.stacks.cardsInDeck.size shouldBe 93

        val firstPlayer = gameUpdated.findPlayer("Player")
        firstPlayer.number shouldBe 1

        val secondPlayer = gameUpdated.findPlayer("loren")
        secondPlayer.number shouldBe 2
    }

    @Test
    fun `Should return 400 Bad Request when gameId is invalid`() = testApplication {
        application {
            kickPlayerRoute(KickPlayerHandler(repository))
            configureExceptionHandling()
            module()
        }
        val playerList = mutableListOf(
            Domain.player(status = PlayerStatus.PLAYING, owner = false),
            Domain.player(name = "joca", passphrase = "xpto", status = PlayerStatus.PLAYING, number = 2)
        )
        val game = Domain.game(players = playerList, status = PLAYING, playerTurn = 2)
        game.initialCards()
        repository.save(game)

        val invalidUuid = randomUUID().toString()
        val requestJson = Json.encodeToString(
            Request(
                gameId = invalidUuid,
                playerName = game.players.first().name,
                passphrase = game.players.first().passphrase,
                targetPlayerName = "joca"
            )
        )

        val response = client.post("/game/kickPlayer") {
            contentType(ContentType.Application.Json)
            setBody(requestJson)
        }

        response.status shouldBe HttpStatusCode.BadRequest

        val responseBody = response.bodyAsText()
        val json = Json.decodeFromString<Map<String, String>>(responseBody)
        json["message"] shouldBe "Game $invalidUuid was not found."

        val gameUpdated = repository.findById(game.id)
        gameUpdated.playerTurn shouldBe 2
        gameUpdated.findPlayer("joca").cards.size shouldBe 7
    }

    @Test
    fun `Should return 400 Bad Request when player name is invalid`() = testApplication {
        application {
            kickPlayerRoute(KickPlayerHandler(repository))
            configureExceptionHandling()
            module()
        }
        val playerList = mutableListOf(
            Domain.player(status = PlayerStatus.PLAYING, owner = false),
            Domain.player(name = "joca", passphrase = "xpto", status = PlayerStatus.PLAYING, number = 2)
        )
        val game = Domain.game(players = playerList, status = PLAYING, playerTurn = 2)
        game.initialCards()
        repository.save(game)

        val requestJson = Json.encodeToString(
            Request(
                gameId = game.id.toString(),
                playerName = "lineuzinho",
                passphrase = game.players.first().passphrase,
                targetPlayerName = "joca"
            )
        )

        val response = client.post("/game/kickPlayer") {
            contentType(ContentType.Application.Json)
            setBody(requestJson)
        }

        response.status shouldBe HttpStatusCode.BadRequest

        val responseBody = response.bodyAsText()
        val json = Json.decodeFromString<Map<String, String>>(responseBody)
        json["message"] shouldBe "Player lineuzinho was not found in the game."

        val gameUpdated = repository.findById(game.id)
        gameUpdated.playerTurn shouldBe 2
        gameUpdated.findPlayer("joca").cards.size shouldBe 7
    }

    @Test
    fun `Should return 400 Bad Request when target player is invalid`() = testApplication {
        application {
            kickPlayerRoute(KickPlayerHandler(repository))
            configureExceptionHandling()
            module()
        }
        val playerList = mutableListOf(
            Domain.player(status = PlayerStatus.PLAYING, owner = true),
            Domain.player(name = "joca", passphrase = "xpto", status = PlayerStatus.PLAYING, number = 2)
        )
        val game = Domain.game(players = playerList, status = PLAYING, playerTurn = 2)
        game.initialCards()
        repository.save(game)

        val requestJson = Json.encodeToString(
            Request(
                gameId = game.id.toString(),
                playerName = game.players.first().name,
                passphrase = game.players.first().passphrase,
                targetPlayerName = "josefa"
            )
        )

        val response = client.post("/game/kickPlayer") {
            contentType(ContentType.Application.Json)
            setBody(requestJson)
        }

        response.status shouldBe HttpStatusCode.BadRequest

        val responseBody = response.bodyAsText()
        val json = Json.decodeFromString<Map<String, String>>(responseBody)
        json["message"] shouldBe "Player josefa was not found in the game."

        val gameUpdated = repository.findById(game.id)
        gameUpdated.playerTurn shouldBe 2
    }

    @Test
    fun `Should return 400 Bad Request and passphrase required when request is invalid`() = testApplication {
        application {
            kickPlayerRoute(KickPlayerHandler(repository))
            configureExceptionHandling()
            module()
        }
        val playerList = mutableListOf(
            Domain.player(status = PlayerStatus.PLAYING, owner = false),
            Domain.player(name = "joca", passphrase = "xpto", status = PlayerStatus.PLAYING, number = 2)
        )
        val game = Domain.game(players = playerList, status = PLAYING, playerTurn = 2)
        game.initialCards()
        repository.save(game)

        val requestJson = Json.encodeToString(
            Request(
                gameId = game.id.toString(),
                playerName = game.players.first().name,
                passphrase = "xpto",
                targetPlayerName = "joca"
            )
        )

        val response = client.post("/game/kickPlayer") {
            contentType(ContentType.Application.Json)
            setBody(requestJson)
        }

        response.status shouldBe HttpStatusCode.BadRequest

        val responseBody = response.bodyAsText()
        val json = Json.decodeFromString<Map<String, String>>(responseBody)
        json["message"] shouldBe "Invalid passphrase."

        val gameUpdated = repository.findById(game.id)
        gameUpdated.playerTurn shouldBe 2
        gameUpdated.findPlayer("joca").cards.size shouldBe 7
    }

    @Test
    fun `Should return 400 Bad Request when player is not owner`() = testApplication {
        application {
            kickPlayerRoute(KickPlayerHandler(repository))
            configureExceptionHandling()
            module()
        }
        val playerList = mutableListOf(
            Domain.player(status = PlayerStatus.PLAYING, owner = false),
            Domain.player(name = "joca", passphrase = "xpto", status = PlayerStatus.PLAYING, number = 2)
        )
        val game = Domain.game(players = playerList, status = PLAYING, playerTurn = 2)
        game.initialCards()
        repository.save(game)

        val requestJson = Json.encodeToString(
            Request(
                gameId = game.id.toString(),
                playerName = playerList.first().name,
                passphrase = playerList.first().passphrase,
                targetPlayerName = "joca"
            )
        )

        val response = client.post("/game/kickPlayer") {
            contentType(ContentType.Application.Json)
            setBody(requestJson)
        }

        response.status shouldBe HttpStatusCode.BadRequest

        val responseBody = response.bodyAsText()
        val json = Json.decodeFromString<Map<String, String>>(responseBody)
        json["message"] shouldBe "Player ${playerList.first().name} is not the owner of the game."

        val gameUpdated = repository.findById(game.id)
        gameUpdated.playerTurn shouldBe 2
        gameUpdated.findPlayer("joca").cards.size shouldBe 7
    }
}
