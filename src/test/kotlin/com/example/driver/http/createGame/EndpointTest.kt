package com.example.driver.http.createGame

import com.example.application.handler.CreateGameHandler
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
import java.util.UUID
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
    fun `Should return 201 CREATED when game is created`() = testApplication {
        application {
            createGameRoute(CreateGameHandler(repository))
            module()
        }
        /**@Dado que exista uma request valida*/
        val requestJson = Json.encodeToString(
            Request(
                name = "userName",
                passphrase = "abacaxi"
            )
        )

        /**@Quando for feito uma requisição para criar um jogo*/
        val response = client.post("/game/createGame") {
            contentType(ContentType.Application.Json)
            setBody(requestJson)
        }
        val responseBody = response.bodyAsText()
        val responseObject = Json.decodeFromString<Response>(responseBody)

        /**@Então deverá retornar o status 201 Created*/
        response.status shouldBe HttpStatusCode.Created

        /**@E o id do jogo criado deve ser igual ao id do jogo retornado*/
        val game = repository.findById(UUID.fromString(responseObject.gameId))
        responseObject.gameId shouldBe game.id.toString()

        /**@E deverá ter apenas um jogo na lista*/
        repository.size() shouldBe 1

        /**@E deverá conter apenas um unico jogador com o nome e a palavra-chave definida na request*/
        repository.findAll().first().players.size shouldBe 1
        repository.findAll().first().players.first().name shouldBe "userName"
        repository.findAll().first().players.first().passphrase shouldBe "abacaxi"

        /**@E deverá definir apenas esse jogador como owner */
        repository.findAll().first().players.first().owner shouldBe true
    }

    @Test
    fun `Should return 400 Bad Request when request is invalid`() = testApplication {
        application {
            createGameRoute(CreateGameHandler(repository))
            module()
        }
        /**@Dado que exista uma request com propriedades vazias*/
        val requestJson = Json.encodeToString(
            Request(name = "", passphrase = "")
        )

        /**@Quando for feito uma requisição com dados invalidos para criar um jogo*/
        val response = client.post("/game/createGame") {
            contentType(ContentType.Application.Json)
            setBody(requestJson)
        }
        val responseBody = response.bodyAsText()

        /**@Então deverá retornar o status 400 Bad Request */
        response.status shouldBe HttpStatusCode.BadRequest

        /**@E deve apontar quais campos estão vazios */
        responseBody shouldBe "Player name is required, Passphrase is required"
    }

    @Test
    fun `Should return 400 Bad Request and passphrase required when request is invalid`() = testApplication {
        application {
            createGameRoute(CreateGameHandler(repository))
            module()
        }
        /**@Dado que exista uma request com uma de suas propriedades vazias */
        val requestJson = Json.encodeToString(
            Request(name = "NameUser", passphrase = "")
        )

        /**@Quando for feito uma requisição com dados invalidos para criar um jogo */
        val response = client.post("/game/createGame") {
            contentType(ContentType.Application.Json)
            setBody(requestJson)
        }
        val responseBody = response.bodyAsText()

        /**@Então deverá retornar o status 400 Bad Request */
        response.status shouldBe HttpStatusCode.BadRequest

        /**@E deverá apontar uma lista de campos que estão vazios */
        responseBody shouldBe "Passphrase is required"
    }
}
