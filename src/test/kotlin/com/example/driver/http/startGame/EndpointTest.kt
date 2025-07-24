package com.example.driver.http.startGame

import com.example.application.factories.Domain
import com.example.application.handler.StartGameHandler
import com.example.application.model.GameStatus.CREATED
import com.example.application.model.GameStatus.FINISHED
import com.example.application.model.GameStatus.PLAYING
import com.example.application.model.GameStatus.WAITING
import com.example.application.model.Games
import com.example.application.model.PlayerStatus
import com.example.application.model.SpecialType
import com.example.application.shared.getFirstGame
import com.example.configuration.configureExceptionHandling
import com.example.driver.http.endGame.Request
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
    fun `Should return 204 when game is started`() = testApplication {
        application {
            startGameRoute(StartGameHandler())
        }
        /**@Dado que exista um jogo já criado com 2 players*/
        val playerList = mutableListOf(Domain.player(), Domain.player(name = "joca", passphrase = "xpto"))
        val game = Domain.game(players = playerList)
        Games.addGame(game)

        /**@E que exista uma request para iniciar um jogo*/
        val requestJson = Json.encodeToString(
            Request(
                gameId = game.id.toString(),
                playerName = game.players.first().name,
                passphrase = game.players.first().passphrase
            )
        )

        /**@Quando for feito uma requisição com dados validos para iniciar um jogo*/
        val response = client.post("/game/startGame") {
            contentType(ContentType.Application.Json)
            setBody(requestJson)
        }

        /**@Então deve retornar o status 204*/
        response.status shouldBe HttpStatusCode.NoContent

        /**@E o status tanto da sala deverá mudar para PLAYING*/
        val gameUpdated = getFirstGame()
        gameUpdated.status shouldBe PLAYING
        gameUpdated.players.size shouldBe 2
        gameUpdated.reverse shouldBe false
        gameUpdated.stacks.cardsInTable.size shouldBe 1
        gameUpdated.buyCardQuantity shouldBe 0
        gameUpdated.blockPending shouldBe false
        gameUpdated.stacks.cardsInDeck.size shouldBe 93

        /**@E o primeiro player deverá ter seu status alterado para PLAYING e deverá ter 7 cartas na mão*/
        val firstPlayer = gameUpdated.findPlayer("Player")
        firstPlayer.statusInGame shouldBe PlayerStatus.PLAYING
        firstPlayer.cards.size shouldBe 7

        /**@E o segundo player deverá ter seu status alterado para PLAYING e deverá ter 7 cartas na mão*/
        val secondPlayer = gameUpdated.findPlayer("joca")
        secondPlayer.statusInGame shouldBe PlayerStatus.PLAYING
        secondPlayer.cards.size shouldBe 7

        /**@E a primeira carta da mesa deverá ser do tipo NONE*/
        gameUpdated.firstCard.especial shouldBe SpecialType.NONE

        /**@E Deverá ter apenas um jogo na lista*/
        Games.games.size shouldBe 1
    }

    @Test
    fun `Should return 400 Bad Request when minus of 2 players to start`() = testApplication {
        application {
            startGameRoute(StartGameHandler())
            configureExceptionHandling()
        }
        /**@Dado que exista um jogo já criado*/
        val game = Domain.game()
        Games.addGame(game)

        /**@E que exista uma request para iniciar um jogo*/
        val requestJson = Json.encodeToString(
            Request(
                gameId = game.id.toString(),
                playerName = game.players.first().name,
                passphrase = game.players.first().passphrase
            )
        )

        /**@Quando for feito uma requisição com dados validos para iniciar um jogo*/
        val response = client.post("/game/startGame") {
            contentType(ContentType.Application.Json)
            setBody(requestJson)
        }

        /**@Então deve retornar o status 400*/
        response.status shouldBe HttpStatusCode.BadRequest

        /**@E deverá retornar uma mensagem infornado que o jogo não foi encontrado*/
        val responseBody = response.bodyAsText()
        val json = Json.decodeFromString<Map<String, String>>(responseBody)
        json["message"] shouldBe "The game requires at least 2 players to start"

        /**@E o status tanto da sala deverá mudar para WAITING*/
        val gameUpdated = getFirstGame()
        gameUpdated.status shouldBe WAITING

        /**@E os players dentro da sala não deverá sofrer alterações*/
        Games.games.first().players.first().statusInGame shouldBe PlayerStatus.IN_LOBBY

        /**@E Deverá ter apenas um jogo na lista*/
        Games.games.size shouldBe 1
    }

    @Test
    fun `Should return 400 Bad Request when gameStatus is PLAYING`() = testApplication {
        application {
            startGameRoute(StartGameHandler())
            configureExceptionHandling()
        }
        /**@Dado que exista um jogo já criado e em jogo*/
        val players = mutableListOf(Domain.player(), Domain.player(name = "joca", passphrase = "xpto"))
        val game = Domain.game(status = PLAYING, players = players)
        Games.addGame(game)

        /**@E que exista uma request para iniciar um jogo*/
        val requestJson = Json.encodeToString(
            Request(
                gameId = game.id.toString(),
                playerName = game.players.first().name,
                passphrase = game.players.first().passphrase
            )
        )

        /**@Quando for feito uma requisição com dados validos para iniciar um jogo*/
        val response = client.post("/game/startGame") {
            contentType(ContentType.Application.Json)
            setBody(requestJson)
        }

        /**@Então deve retornar o status 400*/
        response.status shouldBe HttpStatusCode.BadRequest

        /**@E deverá retornar uma mensagem infornado que o jogo não foi encontrado*/
        val responseBody = response.bodyAsText()
        val json = Json.decodeFromString<Map<String, String>>(responseBody)
        json["message"] shouldBe "The game is already in progress."

        /**@E o status tanto da sala deverá mudar para WAITING*/
        val gameUpdated = getFirstGame()
        gameUpdated.status shouldBe PLAYING

        /**@E os players dentro da sala não deverá sofrer alterações*/
        Games.games.first().players.first().statusInGame shouldBe PlayerStatus.IN_LOBBY

        /**@E Deverá ter apenas um jogo na lista*/
        Games.games.size shouldBe 1
    }

    @Test
    fun `Should 204 and reset the game when gameStatus is FINISHED`() = testApplication {
        application {
            startGameRoute(StartGameHandler())
            configureExceptionHandling()
        }
        /**@Dado que exista um jogo já criado e em jogo*/
        val players = mutableListOf(Domain.player(), Domain.player(name = "joca", passphrase = "xpto"))
        val game = Domain.game(status = FINISHED, players = players)
        Games.addGame(game)

        /**@E que exista uma request para iniciar um jogo*/
        val requestJson = Json.encodeToString(
            Request(
                gameId = game.id.toString(),
                playerName = game.players.first().name,
                passphrase = game.players.first().passphrase
            )
        )

        /**@Quando for feito uma requisição com dados validos para iniciar um jogo*/
        val response = client.post("/game/startGame") {
            contentType(ContentType.Application.Json)
            setBody(requestJson)
        }

        /**@Então deve retornar o status 400*/
        response.status shouldBe HttpStatusCode.NoContent

        /**@E o status tanto da sala deverá mudar para PLAYING*/
        val gameUpdated = getFirstGame()
        gameUpdated.status shouldBe PLAYING
        gameUpdated.players.size shouldBe 2
        gameUpdated.reverse shouldBe false
        gameUpdated.stacks.cardsInTable.size shouldBe 1
        gameUpdated.buyCardQuantity shouldBe 0
        gameUpdated.blockPending shouldBe false
        gameUpdated.stacks.cardsInDeck.size shouldBe 93

        /**@E o primeiro player deverá ter seu status alterado para PLAYING e deverá ter 7 cartas na mão*/
        val firstPlayer = gameUpdated.findPlayer("Player")
        firstPlayer.statusInGame shouldBe PlayerStatus.PLAYING
        firstPlayer.cards.size shouldBe 7

        /**@E o segundo player deverá ter seu status alterado para PLAYING e deverá ter 7 cartas na mão*/
        val secondPlayer = gameUpdated.findPlayer("joca")
        secondPlayer.statusInGame shouldBe PlayerStatus.PLAYING
        secondPlayer.cards.size shouldBe 7

        /**@E a primeira carta da mesa deverá ser do tipo NONE*/
        gameUpdated.firstCard.especial shouldBe SpecialType.NONE

        /**@E Deverá ter apenas um jogo na lista*/
        Games.games.size shouldBe 1
    }

    @Test
    fun `Should return 400 Bad Request when gameId is invalid`() = testApplication {
        application {
            startGameRoute(StartGameHandler())
            configureExceptionHandling()
        }
        /**@Dado que exista um jogo já criado*/
        val game = Domain.game()
        Games.addGame(game)

        /**@E que exista uma request para iniciar um jogo*/
        val invalidUuid = randomUUID().toString()
        val requestJson = Json.encodeToString(
            Request(
                gameId = invalidUuid,
                playerName = game.players.first().name,
                passphrase = game.players.first().passphrase
            )
        )

        /**@Quando for feito uma requisição com dados validos para iniciar um jogo*/
        val response = client.post("/game/startGame") {
            contentType(ContentType.Application.Json)
            setBody(requestJson)
        }

        /**@Então deve retornar o status 400*/
        response.status shouldBe HttpStatusCode.BadRequest

        /**@E deverá retornar uma mensagem infornado que o jogo não foi encontrado*/
        val responseBody = response.bodyAsText()
        val json = Json.decodeFromString<Map<String, String>>(responseBody)
        json["message"] shouldBe "Game $invalidUuid was not found."

        /**@E o status tanto da sala quanto as dos players não deveram sofrer alterações */
        val gameUpdated = getFirstGame()
        gameUpdated.status shouldBe CREATED
        Games.games.first().players.first().statusInGame shouldBe PlayerStatus.IN_LOBBY

        /**@E Deverá ter apenas um jogo na lista*/
        Games.games.size shouldBe 1
    }

    @Test
    fun `Should return 400 Bad Request when player name is invalid`() = testApplication {
        application {
            startGameRoute(StartGameHandler())
            configureExceptionHandling()
        }
        /**@Dado que exista um jogo já criado*/
        val game = Domain.game()
        Games.addGame(game)

        /**@E que exista uma request para encerrar um jogo com usuario invalido*/
        val requestJson = Json.encodeToString(
            Request(
                gameId = game.id.toString(),
                playerName = "lineuzinho",
                passphrase = game.players.first().passphrase
            )
        )

        /**@Quando for feito uma requisição com dados validos para encerrar um jogo*/
        val response = client.post("/game/startGame") {
            contentType(ContentType.Application.Json)
            setBody(requestJson)
        }

        /**@Então deve retornar o status 400*/
        response.status shouldBe HttpStatusCode.BadRequest

        /**@E deverá retornar uma mensagem infornado que o jogo não foi encontrado*/
        val responseBody = response.bodyAsText()
        val json = Json.decodeFromString<Map<String, String>>(responseBody)
        json["message"] shouldBe "Player lineuzinho was not found in the game."

        /**@E o status tanto da sala quanto as dos players não deveram sofrer alterações */
        val gameUpdated = getFirstGame()
        gameUpdated.status shouldBe CREATED
        Games.games.first().players.first().statusInGame shouldBe PlayerStatus.IN_LOBBY

        /**@E Deverá ter apenas um jogo na lista*/
        Games.games.size shouldBe 1
    }

    @Test
    fun `Should return 400 Bad Request and passphrase required when request is invalid`() = testApplication {
        application {
            startGameRoute(StartGameHandler())
            configureExceptionHandling()
        }
        /**@Dado que exista um jogo já criado*/
        val game = Domain.game()
        Games.addGame(game)

        /**@E que exista uma request para iniciar um jogo com passphrase incorreto*/
        val requestJson = Json.encodeToString(
            Request(
                gameId = game.id.toString(),
                playerName = game.players.first().name,
                passphrase = "xpto"
            )
        )

        /**@Quando for feito uma requisição com dados invalidos para iniciar um jogo*/
        val response = client.post("/game/startGame") {
            contentType(ContentType.Application.Json)
            setBody(requestJson)
        }

        /**@Então deve retornar o status 400*/
        response.status shouldBe HttpStatusCode.BadRequest

        /**@E deverá retornar uma mensagem informando que o passphrase é invalido*/
        val responseBody = response.bodyAsText()
        val json = Json.decodeFromString<Map<String, String>>(responseBody)
        json["message"] shouldBe "Invalid passphrase."

        /**@E o status tanto da sala quanto as dos players não deveram sofrer alterações */
        val gameUpdated = getFirstGame()
        gameUpdated.status shouldBe CREATED
        Games.games.first().players.first().statusInGame shouldBe PlayerStatus.IN_LOBBY
    }

    @Test
    fun `Should return 400 Bad Request when player is not owner`() = testApplication {
        application {
            startGameRoute(StartGameHandler())
            configureExceptionHandling()
        }
        /**@Dado que exista um jogo já criado*/
        val player = Domain.player(isOwner = false)
        val game = Domain.game(players = mutableListOf(player))
        Games.addGame(game)

        /**@E que exista uma request para iniciar um jogo*/
        val requestJson = Json.encodeToString(
            Request(
                gameId = game.id.toString(),
                playerName = player.name,
                passphrase = player.passphrase
            )
        )

        /**@Quando for feito uma requisição com dados validos para iniciar um jogo*/
        val response = client.post("/game/startGame") {
            contentType(ContentType.Application.Json)
            setBody(requestJson)
        }

        /**@Então deve retornar o status 400*/
        response.status shouldBe HttpStatusCode.BadRequest

        /**@E deverá retornar uma mensagem infornado que o jogador não é owner*/
        val responseBody = response.bodyAsText()
        val json = Json.decodeFromString<Map<String, String>>(responseBody)
        json["message"] shouldBe "Player ${player.name} is not the owner of the game."

        /**@E o status tanto da sala quanto as dos players não deveram sofrer alterações */
        val gameUpdated = getFirstGame()
        gameUpdated.status shouldBe CREATED
        Games.games.first().players.first().statusInGame shouldBe PlayerStatus.IN_LOBBY

        /**@E Deverá ter apenas um jogo na lista*/
        Games.games.size shouldBe 1
    }

    @Test
    fun `Should return 400 Bad Request and passphrase is empty`() = testApplication {
        application {
            startGameRoute(StartGameHandler())
            configureExceptionHandling()
        }
        /**@Dado que exista um jogo já criado*/
        val game = Domain.game()
        Games.addGame(game)

        /**@E que exista uma request com invalido para iniciar um jogo*/
        val requestJson = Json.encodeToString(
            Request(
                gameId = game.id.toString(),
                playerName = game.players.first().name,
                passphrase = ""
            )
        )

        /**@Quando for feito uma requisição com dados invalidos para iniciar um jogo*/
        val response = client.post("/game/startGame") {
            contentType(ContentType.Application.Json)
            setBody(requestJson)
        }
        val responseBody = response.bodyAsText()

        /**@Então deverá retornar o status 400*/
        response.status shouldBe HttpStatusCode.BadRequest

        /**@E deverá retornar uma mensagem infornado que é necessario informar um passphrase*/
        val expectedMessages = "Passphrase is required"
        responseBody shouldBe expectedMessages

        /**@E o status tanto da sala quanto a dos players não deveram sofrer alterações*/
        val gameUpdated = getFirstGame()
        gameUpdated.status shouldBe CREATED
        Games.games.first().players.first().statusInGame shouldBe PlayerStatus.IN_LOBBY
    }
}
