package com.example.driver.http.kickPlayer

import com.example.application.factories.Domain
import com.example.application.handler.KickPlayerHandler
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
    fun `Should return 204 when kick the player`() = testApplication {
        application {
            kickPlayerRoute(KickPlayerHandler())
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
        Games.addGame(game)

        /**@E que exista uma request com um gameId inexistente*/
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
        val gameUpdated = getFirstGame()
        gameUpdated.players.size shouldBe 2

        /**@E o numero do primeiro jogador da lista não deverá ser alterado */
        val firstPlayer = gameUpdated.findPlayer("Player")
        firstPlayer.number shouldBe 1

        /**@E o numero do ultimo jogador da lista deverá ser alterado */
        val secondPlayer = gameUpdated.findPlayer("loren")
        secondPlayer.number shouldBe 2
    }

    //TODO: Criar teste positivo para quando o jogador for expulso da sala com o jogo em andamento
    @Test
    fun `Should return 204 when kick the player while it is game in progress`() = testApplication {
        application {
            kickPlayerRoute(KickPlayerHandler())
            configureExceptionHandling()
            module()
        }
        /**@Dado que exista um jogo já criado com 2 jogadores em partida*/
        val playerList = mutableListOf(
            Domain.player(status = PlayerStatus.PLAYING),
            Domain.player(name = "joca", passphrase = "xpto",status = PlayerStatus.PLAYING, number = 2),
            Domain.player(name = "loren", passphrase = "xpto",status = PlayerStatus.PLAYING, number = 3)
        )
        val game = Domain.game(
            players = playerList,
            status = PLAYING
        )
        /**@E que o jogo está em andamento*/
        game.initialCards()
        Games.addGame(game)

        /**@E que exista uma request com um gameId inexistente*/
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
        val gameUpdated = getFirstGame()
        gameUpdated.players.size shouldBe 2

        /**@E as cartas que estavam na mão do jogador expulsado deverá retornar a pilha de cartas*/
        gameUpdated.stacks.cardsInDeck.size shouldBe 93

        /**@E o numero do primeiro jogador da lista não deverá ser alterado */
        val firstPlayer = gameUpdated.findPlayer("Player")
        firstPlayer.number shouldBe 1

        /**@E o numero do ultimo jogador da lista deverá ser alterado */
        val secondPlayer = gameUpdated.findPlayer("loren")
        secondPlayer.number shouldBe 2
    }

    @Test
    fun `Should return 400 Bad Request when gameId is invalid`() = testApplication {
        application {
            kickPlayerRoute(KickPlayerHandler())
            configureExceptionHandling()
            module()
        }
        /**@Dado que exista um jogo já criado com 2 jogadores em partida*/
        val playerList = mutableListOf(
            Domain.player(status = PlayerStatus.PLAYING, owner = false),
            Domain.player(name = "joca", passphrase = "xpto", status = PlayerStatus.PLAYING, number = 2)
        )
        val game = Domain.game(
            players = playerList,
            status = PLAYING,
            /**@E que o turno é do jogador numero 2*/
            playerTurn = 2
        )
        /**@E que o jogo está em andamento*/
        game.initialCards()
        Games.addGame(game)

        /**@E que exista uma request com um gameId inexistente*/
        val invalidUuid = randomUUID().toString()
        val requestJson = Json.encodeToString(
            Request(
                gameId = invalidUuid,
                playerName = game.players.first().name,
                passphrase = game.players.first().passphrase,
                targetPlayerName = "joca"
            )
        )

        /**@Quando for feito uma requisição para pular um jogador*/
        val response = client.post("/game/kickPlayer") {
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
        val gameUpdated = getFirstGame()
        gameUpdated.playerTurn shouldBe 2

        /**@E o jogador alvo não deverá sofrer penalidade */
        val secondPlayer = gameUpdated.findPlayer("joca")
        secondPlayer.cards.size shouldBe 7
    }

    @Test
    fun `Should return 400 Bad Request when player name is invalid`() = testApplication {
        application {
            kickPlayerRoute(KickPlayerHandler())
            configureExceptionHandling()
            module()
        }
        /**@Dado que exista um jogo já criado com 2 jogadores em partida*/
        val playerList = mutableListOf(
            Domain.player(status = PlayerStatus.PLAYING, owner = false),
            Domain.player(name = "joca", passphrase = "xpto", status = PlayerStatus.PLAYING, number = 2)
        )
        val game = Domain.game(
            players = playerList,
            status = PLAYING,
            /**@E que o turno é do jogador numero 2*/
            playerTurn = 2
        )
        /**@E que o jogo está em andamento*/
        game.initialCards()
        Games.addGame(game)

        /**@E que exista uma request com jogador inexistente*/
        val requestJson = Json.encodeToString(
            Request(
                gameId = game.id.toString(),
                playerName = "lineuzinho",
                passphrase = game.players.first().passphrase,
                targetPlayerName = "joca"
            )
        )

        /**@Quando for feito uma requisição para pular um jogador*/
        val response = client.post("/game/kickPlayer") {
            contentType(ContentType.Application.Json)
            setBody(requestJson)
        }

        /**@Então deverá retornar o status 400*/
        response.status shouldBe HttpStatusCode.BadRequest

        /**@E deverá retornar uma mensagem informando que o jogador não foi encontrado*/
        val responseBody = response.bodyAsText()
        val json = Json.decodeFromString<Map<String, String>>(responseBody)
        json["message"] shouldBe "Player lineuzinho was not found in the game."

        /**@E o turno não deverá ser passado */
        val gameUpdated = getFirstGame()
        gameUpdated.playerTurn shouldBe 2

        /**@E o jogador alvo não deverá sofrer penalidade */
        val secondPlayer = gameUpdated.findPlayer("joca")
        secondPlayer.cards.size shouldBe 7
    }

    @Test
    fun `Should return 400 Bad Request when target player is invalid`() = testApplication {
        application {
            kickPlayerRoute(KickPlayerHandler())
            configureExceptionHandling()
            module()
        }
        /**@Dado que exista um jogo já criado com 2 jogadores em partida*/
        val playerList = mutableListOf(
            Domain.player(status = PlayerStatus.PLAYING, owner = true),
            Domain.player(name = "joca", passphrase = "xpto", status = PlayerStatus.PLAYING, number = 2)
        )
        val game = Domain.game(
            players = playerList,
            status = PLAYING,
            /**@E que o turno é do jogador numero 2*/
            playerTurn = 2
        )
        /**@E que o jogo está em andamento*/
        game.initialCards()
        Games.addGame(game)

        /**@E que exista uma request com jogador alvo inexistente*/
        val requestJson = Json.encodeToString(
            Request(
                gameId = game.id.toString(),
                playerName = game.players.first().name,
                passphrase = game.players.first().passphrase,
                targetPlayerName = "josefa"
            )
        )

        /**@Quando for feito uma requisição para pular um jogador*/
        val response = client.post("/game/kickPlayer") {
            contentType(ContentType.Application.Json)
            setBody(requestJson)
        }

        /**@Então deverá retornar o status 400*/
        response.status shouldBe HttpStatusCode.BadRequest

        /**@E deverá retornar uma mensagem informando que o jogador não foi encontrado*/
        val responseBody = response.bodyAsText()
        val json = Json.decodeFromString<Map<String, String>>(responseBody)
        json["message"] shouldBe "Player josefa was not found in the game."

        /**@E o turno não deverá ser passado */
        val gameUpdated = getFirstGame()
        gameUpdated.playerTurn shouldBe 2
    }

    @Test
    fun `Should return 400 Bad Request and passphrase required when request is invalid`() = testApplication {
        application {
            kickPlayerRoute(KickPlayerHandler())
            configureExceptionHandling()
            module()
        }
        /**@Dado que exista um jogo já criado com 2 jogadores em partida*/
        val playerList = mutableListOf(
            Domain.player(status = PlayerStatus.PLAYING, owner = false),
            Domain.player(name = "joca", passphrase = "xpto", status = PlayerStatus.PLAYING, number = 2)
        )
        val game = Domain.game(
            players = playerList,
            status = PLAYING,
            /**@E que o turno é do jogador numero 2*/
            playerTurn = 2
        )
        /**@E que o jogo está em andamento*/
        game.initialCards()
        Games.addGame(game)

        /**@E que exista uma request com passphrase incorreto*/
        val requestJson = Json.encodeToString(
            Request(
                gameId = game.id.toString(),
                playerName = game.players.first().name,
                passphrase = "xpto",
                targetPlayerName = "joca"
            )
        )

        /**@Quando for feito uma requisição para pular a vez do jogador alvo*/
        val response = client.post("/game/kickPlayer") {
            contentType(ContentType.Application.Json)
            setBody(requestJson)
        }

        /**@Então deverá retornar o status 400*/
        response.status shouldBe HttpStatusCode.BadRequest

        /**@E deverá retornar uma mensagem informando que o passphrase é invalido*/
        val responseBody = response.bodyAsText()
        val json = Json.decodeFromString<Map<String, String>>(responseBody)
        json["message"] shouldBe "Invalid passphrase."

        /**@E o turno não deverá ser passado */
        val gameUpdated = getFirstGame()
        gameUpdated.playerTurn shouldBe 2

        /**@E o jogador alvo não deverá sofrer penalidade */
        val secondPlayer = gameUpdated.findPlayer("joca")
        secondPlayer.cards.size shouldBe 7
    }

    @Test
    fun `Should return 400 Bad Request when player is not owner`() = testApplication {
        application {
            kickPlayerRoute(KickPlayerHandler())
            configureExceptionHandling()
            module()
        }
        /**@Dado que exista um jogo já criado com 2 jogadores em partida*/
        val playerList = mutableListOf(
            Domain.player(status = PlayerStatus.PLAYING, owner = false),
            Domain.player(name = "joca", passphrase = "xpto", status = PlayerStatus.PLAYING, number = 2)
        )
        val game = Domain.game(
            players = playerList,
            status = PLAYING,
            /**@E que o turno é do jogador numero 2*/
            playerTurn = 2
        )
        /**@E que o jogo está em andamento*/
        game.initialCards()
        Games.addGame(game)

        /**@E que exista uma request valida com um jogador que não é owner da sala*/
        val requestJson = Json.encodeToString(
            Request(
                gameId = game.id.toString(),
                playerName = playerList.first().name,
                passphrase = playerList.first().passphrase,
                targetPlayerName = "joca"
            )
        )

        /**@Quando for feito uma requisição para pular um jogador*/
        val response = client.post("/game/kickPlayer") {
            contentType(ContentType.Application.Json)
            setBody(requestJson)
        }

        /**@Então deve retornar o status 400*/
        response.status shouldBe HttpStatusCode.BadRequest

        /**@E deverá retornar uma mensagem informando que o jogador não é owner*/
        val responseBody = response.bodyAsText()
        val json = Json.decodeFromString<Map<String, String>>(responseBody)
        json["message"] shouldBe "Player ${playerList.first().name} is not the owner of the game."

        /**@E o turno não deverá ser passado */
        val gameUpdated = getFirstGame()
        gameUpdated.playerTurn shouldBe 2

        /**@E o jogador alvo não deverá sofrer penalidade */
        val secondPlayer = gameUpdated.findPlayer("joca")
        secondPlayer.cards.size shouldBe 7
    }
}
