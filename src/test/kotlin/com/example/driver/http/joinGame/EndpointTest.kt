package com.example.driver.http.joinGame

import com.example.application.factories.Domain
import com.example.application.handler.JoinPlayerInGameHandler
import com.example.application.model.GameStatus.PLAYING
import com.example.application.model.Games
import com.example.application.model.PlayerStatus
import com.example.application.shared.getFirstGame
import com.example.configuration.configureExceptionHandling
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
    @AfterEach
    fun after() {
        Games.games.clear()
    }

    @BeforeEach
    fun before() {
        Games.games.clear()
    }

    @Test
    fun `Should return 204 when player join in the game`() = testApplication {
        application {
            joinGameRoute(JoinPlayerInGameHandler())
            module()
        }
        /**@Dado que exista um jogo já criado*/
        val game = Domain.game()

        Games.addGame(game)

        /**@E que exista uma request valida*/
        val requestJson = Json.encodeToString(
            Request(
                gameId = game.id.toString(),
                name = "lineuzinho",
                passphrase = "palavraChave"
            )
        )

        /**@Quando for feito uma requisição para entrar em um jogo*/
        val response = client.post("/game/joinGame") {
            contentType(ContentType.Application.Json)
            setBody(requestJson)
        }

        /**@Então deverá retornar o status 204*/
        response.status shouldBe HttpStatusCode.NoContent

        /**@E o jogador deve constar na lista do jogo*/
        val gameUpdated = getFirstGame()
        val expectedPlayer = gameUpdated.findPlayer("lineuzinho")

        gameUpdated.players.size shouldBe 2

        expectedPlayer.passphrase shouldBe "palavraChave"
        expectedPlayer.statusInGame shouldBe PlayerStatus.IN_LOBBY
        /**@E o jogador deverá não deverá ter nada na lista de cartas */
        expectedPlayer.cards shouldBe emptyList()
        expectedPlayer.lastCard shouldBe false

        /**@E seu numero deverá ser o proximo da lista */
        expectedPlayer.number shouldBe 2

        /**@E o jogador deverá não deve ser registrado como owner */
        expectedPlayer.owner shouldBe false

        /**@E deverá ter apenas um jogo na lista*/
        Games.games.size shouldBe 1
    }

    @Test
    fun `Should return 400 Bad Request when gameId is invalid`() = testApplication {
        application {
            joinGameRoute(JoinPlayerInGameHandler())
            configureExceptionHandling()
            module()
        }
        /**@Dado que exista um jogo já criado*/
        val game = Domain.game()
        Games.addGame(game)

        /**@E que exista uma request com um gameId inexistente*/
        val invalidUuid = randomUUID().toString()
        val requestJson = Json.encodeToString(
            Request(
                gameId = invalidUuid,
                name = game.players.first().name,
                passphrase = game.players.first().passphrase
            )
        )

        /**@Quando for feito uma requisição para entrar em um jogo*/
        val response = client.post("/game/joinGame") {
            contentType(ContentType.Application.Json)
            setBody(requestJson)
        }

        /**@Então deverá retornar o status 400*/
        response.status shouldBe HttpStatusCode.BadRequest

        /**@E deverá retornar uma mensagem informando que o jogo não foi encontrado*/
        val responseBody = response.bodyAsText()
        val json = Json.decodeFromString<Map<String, String>>(responseBody)
        json["message"] shouldBe "Game $invalidUuid was not found."

        /**@E o jogador não deverá ser adicionado na sala */
        val gameUpdated = getFirstGame()
        gameUpdated.players.size shouldBe 1

        /**@E deverá ter apenas um jogo na lista*/
        Games.games.size shouldBe 1
    }

    @Test
    fun `Should return 400 Bad Request when player name exists in game`() = testApplication {
        application {
            joinGameRoute(JoinPlayerInGameHandler())
            configureExceptionHandling()
            module()
        }

        /**@Dado que exista um jogo já criado*/
        val game = Domain.game()
        Games.addGame(game)

        /**@E que exista uma request com usuario que já está na partida*/
        val requestJson = Json.encodeToString(
            Request(
                gameId = game.id.toString(),
                name = game.players.first().name,
                passphrase = game.players.first().passphrase
            )
        )

        /**@Quando for feito uma requisição para entrar em um jogo*/
        val response = client.post("/game/joinGame") {
            contentType(ContentType.Application.Json)
            setBody(requestJson)
        }

        /**@Então deve retornar o status 400*/
        response.status shouldBe HttpStatusCode.BadRequest

        /**@E deverá retornar uma mensagem informando que já existe um jogador com esse nome*/
        val responseBody = response.bodyAsText()
        val json = Json.decodeFromString<Map<String, String>>(responseBody)
        json["message"] shouldBe "The name Player is already in use."

        /**@E o jogador não deverá ser adicionado na sala*/
        val gameUpdated = getFirstGame()
        gameUpdated.players.size shouldBe 1
    }

    @Test
    fun `Should return 400 Bad Request when the game has status PLAYING`() = testApplication {
        application {
            joinGameRoute(JoinPlayerInGameHandler())
            configureExceptionHandling()
            module()
        }
        /**@Dado que exista um jogo já criado e que esteja com o status de PLAYING*/
        val game = Domain.game(status = PLAYING)
        Games.addGame(game)

        /**@E que exista uma request valida*/
        val requestJson = Json.encodeToString(
            Request(
                gameId = game.id.toString(),
                name = "lineuzinho",
                passphrase = "palavraChave"
            )
        )

        /**@Quando for feito uma requisição entrar em um jogo*/
        val response = client.post("/game/joinGame") {
            contentType(ContentType.Application.Json)
            setBody(requestJson)
        }

        /**@Então deverá retornar o status 400*/
        response.status shouldBe HttpStatusCode.BadRequest

        /**@E deverá retornar uma mensagem informando que o jogo já está em partida*/
        val responseBody = response.bodyAsText()
        val json = Json.decodeFromString<Map<String, String>>(responseBody)
        json["message"] shouldBe "Game ${game.id} is not available."

        /**@E o jogador não deverá constar na lista de jogadores da sala */
        val gameUpdated = getFirstGame()
        gameUpdated.players.size shouldBe 1
    }

    @Test
    fun `Should return 400 Bad Request and passphrase is empty`() = testApplication {
        application {
            joinGameRoute(JoinPlayerInGameHandler())
            configureExceptionHandling()
            module()
        }
        /**@Dado que exista um jogo já criado*/
        val game = Domain.game()
        Games.addGame(game)

        /**@E que exista uma request com o campo de passphrase vazio*/
        val requestJson = Json.encodeToString(
            Request(
                gameId = game.id.toString(),
                name = game.players.first().name,
                passphrase = ""
            )
        )

        /**@Quando for feito uma requisição para entrar no jogo*/
        val response = client.post("/game/joinGame") {
            contentType(ContentType.Application.Json)
            setBody(requestJson)
        }
        val responseBody = response.bodyAsText()

        /**@Então deverá retornar o status 400*/
        response.status shouldBe HttpStatusCode.BadRequest

        /**@E deverá retornar uma mensagem informando que é necessario informar um passphrase*/
        val expectedMessages = "Passphrase is required"
        responseBody shouldBe expectedMessages
    }

    @Test
    fun `Should return 400 when player limit exceeded`() = testApplication {
        application {
            joinGameRoute(JoinPlayerInGameHandler())
            configureExceptionHandling()
            module()
        }
        /**@Dado que exista um jogo já criado com varios jogadores*/
        val players = mutableListOf(
            Domain.player(name = "lineu", number = 1),
            Domain.player(name = "abreu", number = 2),
            Domain.player(name = "caio", number = 3),
            Domain.player(name = "joca", number = 4)
        )
        val game = Domain.game(players = players)

        Games.addGame(game)

        /**@E que exista uma request valida*/
        val requestJson = Json.encodeToString(
            Request(
                gameId = game.id.toString(),
                name = "lineuzinho",
                passphrase = "palavraChave"
            )
        )

        /**@Quando for feito uma requisição para entrar em um jogo*/
        val response = client.post("/game/joinGame") {
            contentType(ContentType.Application.Json)
            setBody(requestJson)
        }

        /**@Então deverá retornar o status 400*/
        response.status shouldBe HttpStatusCode.BadRequest

        /**@E deverá retornar uma mensagem informando que a sala está cheia*/
        val responseBody = response.bodyAsText()
        val json = Json.decodeFromString<Map<String, String>>(responseBody)
        json["message"] shouldBe "The maximum number of players has been exceeded, limit 4."

        /**@E a lista não pode se exceder*/
        val gameUpdated = getFirstGame()
        gameUpdated.players.size shouldBe 4
    }
}