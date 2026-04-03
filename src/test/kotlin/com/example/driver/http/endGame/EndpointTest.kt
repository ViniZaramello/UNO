package com.example.driver.http.endGame

import com.example.application.factories.Domain
import com.example.application.handler.EndGameHandler
import com.example.application.model.GameStatus.CREATED
import com.example.application.model.GameStatus.FINISHED
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
    fun `Should return 204 when game is end`() = testApplication {
        application {
            endGameRoute(EndGameHandler(repository))
            module()
        }
        /**@Dado que exista um jogo já criado*/
        val game = Domain.game()
        repository.save(game)

        /**@E que exista uma request valida*/
        val requestJson = Json.encodeToString(
            Request(
                gameId = game.id.toString(),
                playerName = game.players.first().name,
                passphrase = game.players.first().passphrase
            )
        )

        /**@Quando for feito uma requisição para encerrar um jogo*/
        val response = client.post("/game/endGame") {
            contentType(ContentType.Application.Json)
            setBody(requestJson)
        }

        /**@Então deverá retornar o status 204*/
        response.status shouldBe HttpStatusCode.NoContent

        /**@E o status do jogo deverá ser alterado para FINISHED*/
        val gameUpdated = repository.findById(game.id)
        gameUpdated.status shouldBe FINISHED

        /**@E o status dos jogadores deverá ser alterado ser FINISHED*/
        gameUpdated.players.first().statusInGame shouldBe PlayerStatus.FINISHED

        /**@E deverá ter apenas um jogo na lista*/
        repository.size() shouldBe 1
    }

    @Test
    fun `Should return 400 Bad Request when gameId is invalid`() = testApplication {
        application {
            endGameRoute(EndGameHandler(repository))
            configureExceptionHandling()
            module()
        }
        /**@Dado que exista um jogo já criado*/
        val game = Domain.game()
        repository.save(game)

        /**@E que exista uma request para encerrar um jogo com um gameId inexistente*/
        val invalidUuid = randomUUID().toString()
        val requestJson = Json.encodeToString(
            Request(
                gameId = invalidUuid,
                playerName = game.players.first().name,
                passphrase = game.players.first().passphrase
            )
        )

        /**@Quando for feito uma requisição para encerrar um jogo*/
        val response = client.post("/game/endGame") {
            contentType(ContentType.Application.Json)
            setBody(requestJson)
        }

        /**@Então deverá retornar o status 400*/
        response.status shouldBe HttpStatusCode.BadRequest

        /**@E deverá retornar uma mensagem informando que o jogo não foi encontrado*/
        val responseBody = response.bodyAsText()
        val json = Json.decodeFromString<Map<String, String>>(responseBody)
        json["message"] shouldBe "Game $invalidUuid was not found."

        /**@E o status tanto da sala quanto as dos players não deverá sofrer alterações */
        val gameUpdated = repository.findById(game.id)
        gameUpdated.status shouldBe CREATED
        gameUpdated.players.first().statusInGame shouldBe PlayerStatus.IN_LOBBY

        /**@E deverá ter apenas um jogo na lista*/
        repository.size() shouldBe 1
    }

    @Test
    fun `Should return 400 Bad Request when player name is invalid`() = testApplication {
        application {
            endGameRoute(EndGameHandler(repository))
            configureExceptionHandling()
            module()
        }
        /**@Dado que exista um jogo já criado*/
        val game = Domain.game()
        repository.save(game)

        /**@E que exista uma request com jogador inexistente na partida*/
        val requestJson = Json.encodeToString(
            Request(
                gameId = game.id.toString(),
                playerName = "lineuzinho",
                passphrase = game.players.first().passphrase
            )
        )

        /**@Quando for feito uma requisição para encerrar um jogo*/
        val response = client.post("/game/endGame") {
            contentType(ContentType.Application.Json)
            setBody(requestJson)
        }

        /**@Então deve retornar o status 400*/
        response.status shouldBe HttpStatusCode.BadRequest

        /**@E deverá retornar uma mensagem informando que o jogo não foi encontrado*/
        val responseBody = response.bodyAsText()
        val json = Json.decodeFromString<Map<String, String>>(responseBody)
        json["message"] shouldBe "Player lineuzinho was not found in the game."

        /**@E o status tanto da sala quanto as dos jogadores não deverá sofrer alterações */
        val gameUpdated = repository.findById(game.id)
        gameUpdated.status shouldBe CREATED
        gameUpdated.players.first().statusInGame shouldBe PlayerStatus.IN_LOBBY

        /**@E deverá ter apenas um jogo na lista*/
        repository.size() shouldBe 1
    }

    @Test
    fun `Should return 400 Bad Request and passphrase required when request is invalid`() = testApplication {
        application {
            endGameRoute(EndGameHandler(repository))
            configureExceptionHandling()
            module()
        }
        /**@Dado que exista um jogo já criado*/
        val game = Domain.game()
        repository.save(game)

        /**@E que exista uma request com passphrase invalido*/
        val requestJson = Json.encodeToString(
            Request(
                gameId = game.id.toString(),
                playerName = game.players.first().name,
                passphrase = "xpto"
            )
        )

        /**@Quando for feito uma requisição encerrar um jogo*/
        val response = client.post("/game/endGame") {
            contentType(ContentType.Application.Json)
            setBody(requestJson)
        }

        /**@Então deverá retornar o status 400*/
        response.status shouldBe HttpStatusCode.BadRequest

        /**@E deverá retornar uma mensagem informando que o passphrase é invalido*/
        val responseBody = response.bodyAsText()
        val json = Json.decodeFromString<Map<String, String>>(responseBody)
        json["message"] shouldBe "Invalid passphrase."

        /**@E o status tanto da sala quanto as dos players não deverá sofrer alterações */
        val gameUpdated = repository.findById(game.id)
        gameUpdated.status shouldBe CREATED
        gameUpdated.players.first().statusInGame shouldBe PlayerStatus.IN_LOBBY
    }

    @Test
    fun `Should return 400 Bad Request when player is not owner`() = testApplication {
        application {
            endGameRoute(EndGameHandler(repository))
            configureExceptionHandling()
            module()
        }
        /**@Dado que exista um jogo já criado*/
        val player = Domain.player(owner = false)
        val game = Domain.game(players = mutableListOf(player))
        repository.save(game)

        /**@E que exista uma request com dados de um jogador não setado como owner*/
        val requestJson = Json.encodeToString(
            Request(
                gameId = game.id.toString(),
                playerName = player.name,
                passphrase = player.passphrase
            )
        )

        /**@Quando for feito uma requisição para encerrar um jogo*/
        val response = client.post("/game/endGame") {
            contentType(ContentType.Application.Json)
            setBody(requestJson)
        }

        /**@Então deverá retornar o status 400*/
        response.status shouldBe HttpStatusCode.BadRequest

        /**@E deverá retornar uma mensagem informando que o jogador não é owner*/
        val responseBody = response.bodyAsText()
        val json = Json.decodeFromString<Map<String, String>>(responseBody)
        json["message"] shouldBe "Player ${player.name} is not the owner of the game."

        /**@E o status tanto da sala quanto as dos jogadores não deverá sofrer alterações */
        val gameUpdated = repository.findById(game.id)
        gameUpdated.status shouldBe CREATED
        gameUpdated.players.first().statusInGame shouldBe PlayerStatus.IN_LOBBY

        /**@E deverá ter apenas um jogo na lista*/
        repository.size() shouldBe 1
    }

    @Test
    fun `Should return 400 Bad Request and passphrase is empty`() = testApplication {
        application {
            endGameRoute(EndGameHandler(repository))
            configureExceptionHandling()
            module()
        }
        /**@Dado que exista um jogo já criado*/
        val game = Domain.game()
        repository.save(game)

        /**@E que exista uma request com o campo de passphrase vazio */
        val requestJson = Json.encodeToString(
            Request(
                gameId = game.id.toString(),
                playerName = game.players.first().name,
                passphrase = ""
            )
        )

        /**@Quando for feito uma requisição para encerrar um jogo*/
        val response = client.post("/game/endGame") {
            contentType(ContentType.Application.Json)
            setBody(requestJson)
        }
        val responseBody = response.bodyAsText()

        /**@Então deverá retornar o status 400*/
        response.status shouldBe HttpStatusCode.BadRequest

        /**@E deverá retornar uma mensagem informando que é necessario informar um passphrase*/
        responseBody shouldBe "Passphrase is required"

        /**@E o status tanto da sala quanto a dos jogadores não deverá sofrer alterações*/
        val gameUpdated = repository.findById(game.id)
        gameUpdated.status shouldBe CREATED
        gameUpdated.players.first().statusInGame shouldBe PlayerStatus.IN_LOBBY
    }
}
