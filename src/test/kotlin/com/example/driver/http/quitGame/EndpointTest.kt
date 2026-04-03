package com.example.driver.http.quitGame

import com.example.application.factories.Domain
import com.example.application.handler.QuitGameHandler
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
    fun `Should return 204 when player quit the game`() = testApplication {
        application {
            quitGameRoute(QuitGameHandler(repository))
            configureExceptionHandling()
            module()
        }
        /**@Dado que exista um jogo já criado com 2 jogadores em partida*/
        val playerList = mutableListOf(
            Domain.player(),
            Domain.player(name = "joca", passphrase = "xpto", number = 2),
            Domain.player(name = "loren", passphrase = "xpto", number = 3)
        )
        val game = Domain.game(players = playerList)
        /**@E que o jogo está em andamento*/
        game.initialCards()
        repository.save(game)

        /**@E que exista uma request válida*/
        val requestJson = Json.encodeToString(
            Request(
                gameId = game.id.toString(),
                name = game.players.first().name,
                passphrase = game.players.first().passphrase
            )
        )

        /**@Quando for feito uma requisição para sair de um jogo*/
        val response = client.post("/game/quitGame") {
            contentType(ContentType.Application.Json)
            setBody(requestJson)
        }

        /**@Então deverá retornar o status 204*/
        response.status shouldBe HttpStatusCode.NoContent

        /**@E a contagem de jogadores deve ser de 2 */
        val gameUpdated = repository.findById(game.id)
        gameUpdated.players.size shouldBe 2

        /**@E o numero de cada jogador deve ser alterado */
        val firstPlayer = gameUpdated.findPlayer("joca")
        firstPlayer.number shouldBe 1

        /**@E o novo jogador numero deverá ser definido como owner*/
        firstPlayer.owner shouldBe true

        val secondPlayer = gameUpdated.findPlayer("loren")
        secondPlayer.number shouldBe 2
    }

    @Test
    fun `Should return 204 when player leave the game while it is in progress`() = testApplication {
        application {
            quitGameRoute(QuitGameHandler(repository))
            configureExceptionHandling()
            module()
        }
        /**@Dado que exista um jogo já criado com 3 jogadores em partida*/
        val playerList = mutableListOf(
            Domain.player(status = PlayerStatus.PLAYING),
            Domain.player(name = "joca", passphrase = "xpto", number = 2, status = PlayerStatus.PLAYING),
            Domain.player(name = "loren", passphrase = "xpto", number = 3, status = PlayerStatus.PLAYING)
        )
        val game = Domain.game(players = playerList, status = PLAYING)
        /**@E que o jogo está em andamento*/
        game.initialCards()
        repository.save(game)

        /**@E que exista uma request válida*/
        val requestJson = Json.encodeToString(
            Request(
                gameId = game.id.toString(),
                name = game.players.first().name,
                passphrase = game.players.first().passphrase
            )
        )

        /**@Quando for feito uma requisição para sair de um jogo*/
        val response = client.post("/game/quitGame") {
            contentType(ContentType.Application.Json)
            setBody(requestJson)
        }

        /**@Então deverá retornar o status 204*/
        response.status shouldBe HttpStatusCode.NoContent

        /**@E a contagem de jogadores deve ser de 2 */
        val gameUpdated = repository.findById(game.id)
        gameUpdated.players.size shouldBe 2

        /**@E as cartas do jogador que saiu deverá voltar para a pilha*/
        gameUpdated.stacks.cardsInDeck.size shouldBe 93

        /**@E o numero de cada jogador deve ser alterado */
        val firstPlayer = gameUpdated.findPlayer("joca")
        firstPlayer.number shouldBe 1

        val secondPlayer = gameUpdated.findPlayer("loren")
        secondPlayer.number shouldBe 2
    }

    @Test
    fun `Should return 400 Bad Request when gameId is invalid`() = testApplication {
        application {
            quitGameRoute(QuitGameHandler(repository))
            configureExceptionHandling()
            module()
        }
        /**@Dado que exista um jogo já criado com 2 jogadores em partida*/
        val playerList = mutableListOf(
            Domain.player(status = PlayerStatus.PLAYING, owner = false),
            Domain.player(name = "joca", passphrase = "xpto", status = PlayerStatus.PLAYING, number = 2)
        )
        val game = Domain.game(players = playerList, status = PLAYING, playerTurn = 2)
        game.initialCards()
        repository.save(game)

        /**@E que exista uma request com um gameId inexistente*/
        val invalidUuid = randomUUID().toString()
        val requestJson = Json.encodeToString(
            Request(
                gameId = invalidUuid,
                name = game.players.first().name,
                passphrase = game.players.first().passphrase
            )
        )

        /**@Quando for feito uma requisição para sair de um jogo*/
        val response = client.post("/game/quitGame") {
            contentType(ContentType.Application.Json)
            setBody(requestJson)
        }

        /**@Então deverá retornar o status 400*/
        response.status shouldBe HttpStatusCode.BadRequest

        /**@E deverá retornar uma mensagem informando que o jogo não foi encontrado*/
        val responseBody = response.bodyAsText()
        val json = Json.decodeFromString<Map<String, String>>(responseBody)
        json["message"] shouldBe "Game $invalidUuid was not found."

        /**@E o turno não deverá ser passado */
        val gameUpdated = repository.findById(game.id)
        gameUpdated.playerTurn shouldBe 2

        /**@E o jogador alvo não deverá sofrer penalidade */
        gameUpdated.findPlayer("joca").cards.size shouldBe 7
    }

    @Test
    fun `Should return 400 Bad Request when player name is invalid`() = testApplication {
        application {
            quitGameRoute(QuitGameHandler(repository))
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
                name = "lineuzinho",
                passphrase = game.players.first().passphrase
            )
        )

        val response = client.post("/game/quitGame") {
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
    fun `Should return 400 Bad Request and passphrase required when request is invalid`() = testApplication {
        application {
            quitGameRoute(QuitGameHandler(repository))
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
                name = game.players.first().name,
                passphrase = "xpto"
            )
        )

        val response = client.post("/game/quitGame") {
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
}
