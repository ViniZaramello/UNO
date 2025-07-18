package com.example.driver.http.createGame

import com.example.application.handler.CreateGameHandler
import com.example.application.model.Games
import com.example.application.shared.getFirstGame
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.testApplication
import kotlin.test.assertEquals
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class EndpointKtTest {

    @AfterEach
    fun after() {
        Games.games.clear()
    }

    @BeforeEach
    fun before() {
        Games.games.clear()
    }

    @Test
    fun `Should return 200 OK when game is created`() = testApplication {
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
        assertEquals(HttpStatusCode.Created, response.status)

        /**@E o id do jogo criado deve ser igual ao id do jogo retornado*/
        val game = getFirstGame()
        assertEquals(responseObject.gameId, game.id.toString())
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
        assertEquals(HttpStatusCode.BadRequest, response.status)

        /**@E deve apontar quais campos estão vazios*/
        val expectedMessages = "Player name is required, Passphrase is required"
        assertEquals(responseBody, expectedMessages)
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
        assertEquals(HttpStatusCode.BadRequest, response.status)

        /**@E deve apontar quais campos estão vazios*/
        val expectedMessages = "Passphrase is required"
        assertEquals(responseBody, expectedMessages)
    }
}
