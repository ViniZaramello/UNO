package com.example.driver.http.createGame

import com.example.application.handler.CreateGameHandler
import com.example.application.model.Games
import com.example.application.shared.getFirstGame
import io.kotest.matchers.shouldBe
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.testApplication
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
    fun `Should return 201 CREATED when game is created`() = testApplication {
        application {
            createGameRoute(CreateGameHandler())
        }
        /**@Dado que exista uma request para criar um jogo*/
        val requestJson = Json.encodeToString(
            Request(
                name = "userName",
                passphrase = "abacaxi"
            )
        )

        /**@Quando for feito uma requisição com dados validos para criar um jogo*/
        val response = client.post("/game/createGame") {
            contentType(ContentType.Application.Json)
            setBody(requestJson)
        }
        val responseBody = response.bodyAsText()
        val responseObject = Json.decodeFromString<Response>(responseBody)

        /**@Então deve retornar o status 201 Created*/
        response.status shouldBe HttpStatusCode.Created

        /**@E o id do jogo criado deve ser igual ao id do jogo retornado*/
        val game = getFirstGame()
        responseObject.gameId shouldBe game.id.toString()

        /**@E Deverá ter apenas um jogo na lista*/
        Games.games.size shouldBe 1

        /**@E deverá conter apenas um unico jogador com o nome e a palavra-chave definida na request*/
        Games.games.first().players.size shouldBe 1
        Games.games.first().players.first().name shouldBe "userName"
        Games.games.first().players.first().passphrase shouldBe "abacaxi"

        /**@E deverá definir apenas esse jogador como owner */
        Games.games.first().players.first().owner shouldBe true
    }

    @Test
    fun `Should return 400 Bad Request when request is invalid`() = testApplication {
        application {
            createGameRoute(CreateGameHandler())
        }
        /**@Dado que exista uma request com propriedades vazias*/
        val requestJson = Json.encodeToString(
            Request(
                name = "",
                passphrase = ""
            )
        )

        /**@Quando for feito uma requisição com dados invalidos para criar um jogo*/
        val response = client.post("/game/createGame") {
            contentType(ContentType.Application.Json)
            setBody(requestJson)
        }
        val responseBody = response.bodyAsText()

        /**@Então deve retornar o status 400 Bad Request*/
        response.status shouldBe HttpStatusCode.BadRequest

        /**@E deve apontar quais campos estão vazios*/
        val expectedMessages = "Player name is required, Passphrase is required"
        responseBody shouldBe expectedMessages
    }

    @Test
    fun `Should return 400 Bad Request and passphrase required when request is invalid`() = testApplication {
        application {
            createGameRoute(CreateGameHandler())
        }
        /**@Dado que exista uma request com propriedades uma de suas propriedades vazias para criar um jogo*/
        val requestJson = Json.encodeToString(
            Request(
                name = "NameUser",
                passphrase = ""
            )
        )

        /**@Quando for feito uma requisição com dados invalidos para criar um jogo*/
        val response = client.post("/game/createGame") {
            contentType(ContentType.Application.Json)
            setBody(requestJson)
        }
        val responseBody = response.bodyAsText()

        /**@Então deve retornar o status 400 Bad Request*/
        response.status shouldBe HttpStatusCode.BadRequest

        /**@E deve apontar quais campos estão vazios*/
        val expectedMessages = "Passphrase is required"
        responseBody shouldBe expectedMessages
    }
}
