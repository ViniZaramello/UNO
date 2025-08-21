package com.example.driver.http.buyCard

import com.example.application.factories.Domain
import com.example.application.handler.BuyCardHandler
import com.example.application.model.Card
import com.example.application.model.Colors
import com.example.application.model.GameStatus.PLAYING
import com.example.application.model.GameStatus.WAITING
import com.example.application.model.Games
import com.example.application.model.PlayerStatus
import com.example.application.model.SpecialType
import com.example.application.model.vo.StackCards
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
import io.mockk.every
import io.mockk.spyk
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
    fun `Should return 202 when player buy card`() = testApplication {
        application {
            buyCardRoute(BuyCardHandler())
            module()
        }
        /**@Dado que exista um jogo em andamento com 2 jogadores jogando*/
        val playerList = mutableListOf(
            Domain.player(status = PlayerStatus.PLAYING),
            Domain.player(name = "joca", passphrase = "xpto", number = 2, status = PlayerStatus.PLAYING)
        )
        val game = Domain.game(players = playerList, status = PLAYING)
        game.initialCards()
        Games.addGame(game)

        /**@E que exista uma request valida*/
        val requestJson = Json.encodeToString(
            Request(
                gameId = game.id.toString(),
                playerName = game.players.first().name,
                passphrase = game.players.first().passphrase
            )
        )

        /**@Quando for feito uma requisição para comprar uma carta*/
        val response = client.post("/player/buyCard") {
            contentType(ContentType.Application.Json)
            setBody(requestJson)
        }

        /**@Então deverá retornar o status 202*/
        response.status shouldBe HttpStatusCode.Accepted

        /**@E deverá remover uma carta da pilha de compra*/
        val gameUpdated = getFirstGame()
        gameUpdated.stacks.cardsInDeck.size shouldBe 92

        /**@E o primeiro jogador deverá conter 8 cartas em mãos*/
        val firstPlayer = gameUpdated.findPlayer("Player")
        firstPlayer.cards.size shouldBe 8

        /**@E deverá retornar as informações da carta comprada*/
        val responseBody = response.bodyAsText()
        val json = Json.decodeFromString<Card>(responseBody)
        json shouldBe firstPlayer.cards.last()

        /**@E o segundo jogador deverá conter apenas 7 cartas em mãos*/
        val secondPlayer = gameUpdated.findPlayer("joca")
        secondPlayer.cards.size shouldBe 7
    }

    @Test
    fun `Should return 202 and pass the turn when the player takes a non-special card`() = testApplication {
        application {
            buyCardRoute(BuyCardHandler())
            module()
        }

        /**@Dado que exista um jogo em andamento com 2 jogadores jogando*/
        val playerList = mutableListOf(
            Domain.player(status = PlayerStatus.PLAYING),
            Domain.player(name = "joca", passphrase = "xpto", number = 2, status = PlayerStatus.PLAYING)
        )
        val fakeCard = Domain.card()
        val stacksSpy = spyk(StackCards())
        val game = Domain.game(players = playerList, status = PLAYING, stacks = stacksSpy)
        val card = Domain.card(name = "One blue", number = "1", color = Colors.BLUE)

        game.initialCards()
        game.stacks.cardsInTable.add(card)
        Games.addGame(game)

        /**@E que exista uma request valida*/
        val requestJson = Json.encodeToString(
            Request(
                gameId = game.id.toString(),
                playerName = game.players.first().name,
                passphrase = game.players.first().passphrase
            )
        )

        /**@Quando for solicitado a compra de uma carta, deverá retornar uma carta incompativel com a que está na mesa*/
        every { stacksSpy.getRandomCard() } returns fakeCard

        /**@Quando for feito uma requisição com dados validos para comprar uma carta*/
        val response = client.post("/player/buyCard") {
            contentType(ContentType.Application.Json)
            setBody(requestJson)
        }

        /**@Então deverá retornar o status 202*/
        response.status shouldBe HttpStatusCode.Accepted

        /**@E o turno deverá passar para a vez do segundo jogador*/
        val gameUpdated = getFirstGame()
        gameUpdated.playerTurn shouldBe 2

        /**@E o primeiro jogador deverá ter 8 cartas em mãos*/
        val firstPlayer = gameUpdated.findPlayer("Player")
        firstPlayer.cards.size shouldBe 8

        /**@E deverá retornar as informações da carta comprada*/
        val responseBody = response.bodyAsText()
        val json = Json.decodeFromString<Card>(responseBody)
        json shouldBe firstPlayer.cards.last()

        /**@E a carta deverá ser a mesma do mock*/
        json shouldBe fakeCard

        /**@E o segundo jogador deverá conter apenas 7 cartas em mãos*/
        val secondPlayer = gameUpdated.findPlayer("joca")
        secondPlayer.cards.size shouldBe 7
    }

    @Test
    fun `Should return 202 and not pass the turn when the player takes a special card`(): Unit = testApplication {
        application {
            buyCardRoute(BuyCardHandler())
            module()
        }
        /**@Dado que exista um jogo em andamento com 2 jogadores jogando*/
        val playerList = mutableListOf(
            Domain.player(status = PlayerStatus.PLAYING),
            Domain.player(name = "joca", passphrase = "xpto", status = PlayerStatus.PLAYING)
        )
        val fakeCard = Domain.card(
            name = "Plus two",
            number = "plus-two",
            color = Colors.BLACK,
            special = SpecialType.BUY_FOUR
        )
        val stacksSpy = spyk(StackCards())
        val game = Domain.game(players = playerList, status = PLAYING, stacks = stacksSpy)
        val card = Domain.card(name = "One blue", number = "1", color = Colors.BLUE)

        game.initialCards()
        game.stacks.cardsInTable.add(card)
        Games.addGame(game)

        /**@E que exista uma request valida*/
        val requestJson = Json.encodeToString(
            Request(
                gameId = game.id.toString(),
                playerName = game.players.first().name,
                passphrase = game.players.first().passphrase
            )
        )

        /**@Quando for solicitado a compra de uma carta, deverá retornar uma carta compativel*/
        every { stacksSpy.getRandomCard() } returns fakeCard

        /**@Quando for feito uma requisição para comprar uma carta*/
        val response = client.post("/player/buyCard") {
            contentType(ContentType.Application.Json)
            setBody(requestJson)
        }

        /**@Então deverá retornar o status 202*/
        response.status shouldBe HttpStatusCode.Accepted

        /**@E o turno deverá permanecer para a vez do mesmo jogador*/
        val gameUpdated = getFirstGame()
        gameUpdated.playerTurn shouldBe 1

        /**@E o primeiro jogador deverá conter 8 cartas em mãos*/
        val firstPlayer = gameUpdated.findPlayer("Player")
        firstPlayer.cards.size shouldBe 8

        /**@E deverá retornar as informações da carta comprada*/
        val responseBody = response.bodyAsText()
        val json = Json.decodeFromString<Card>(responseBody)
        json shouldBe firstPlayer.cards.last()

        /**@E a carta deverá ser a mesma do mock*/
        json shouldBe fakeCard

        /**@E o segundo player deverá ter apenas 7 cartas em mãos*/
        val secondPlayer = gameUpdated.findPlayer("joca")
        secondPlayer.cards.size shouldBe 7
    }

    @Test
    fun `Should return 400 bad request when not on the player's turn`() = testApplication {
        application {
            buyCardRoute(BuyCardHandler())
            configureExceptionHandling()
            module()
        }
        /**@Dado que exista um jogo em andamento com 2 jogadores jogando*/
        val playerList = mutableListOf(
            Domain.player(status = PlayerStatus.PLAYING),
            Domain.player(name = "joca", passphrase = "xpto", status = PlayerStatus.PLAYING)
        )
        val game = Domain.game(
            players = playerList,
            status = PLAYING,
            /**@E que o turno seja do segundo jogador*/
            playerTurn = 2
        )

        game.initialCards()
        Games.addGame(game)

        /**@E que exista uma request com as informações de um jogador que não está na sua vez*/
        val requestJson = Json.encodeToString(
            Request(
                gameId = game.id.toString(),
                playerName = game.players.first().name,
                passphrase = game.players.first().passphrase
            )
        )

        /**@Quando for feito uma requisição para comprar uma carta*/
        val response = client.post("/player/buyCard") {
            contentType(ContentType.Application.Json)
            setBody(requestJson)
        }

        /**@Então deverá retornar o status 400*/
        response.status shouldBe HttpStatusCode.BadRequest

        /**@E deverá retornar uma mensagem informando que o jogador não está no seu turno*/
        val responseBody = response.bodyAsText()
        val json = Json.decodeFromString<Map<String, String>>(responseBody)
        json["message"] shouldBe "It is not your turn to draw a card."

        /**@E os jogadores da sala não deverá sofrer nenhum acréscimo de carta*/
        val gameUpdated = getFirstGame()
        val firstPlayer = gameUpdated.findPlayer("Player")
        firstPlayer.cards.size shouldBe 7

        val secondPlayer = gameUpdated.findPlayer("joca")
        secondPlayer.cards.size shouldBe 7
    }

    @Test
    fun `Should return 400 Bad Request when gameId is invalid`() = testApplication {
        application {
            buyCardRoute(BuyCardHandler())
            configureExceptionHandling()
            module()
        }
        /**@Dado que exista um jogo em andamento com 2 jogadores jogando*/
        val playerList = mutableListOf(
            Domain.player(status = PlayerStatus.PLAYING),
            Domain.player(name = "joca", passphrase = "xpto", status = PlayerStatus.PLAYING)
        )
        val game = Domain.game(
            players = playerList,
            status = PLAYING
        )
        game.initialCards()
        Games.addGame(game)

        /**@E que exista uma request com gameId inexistente*/
        val invalidUuid = randomUUID().toString()
        val requestJson = Json.encodeToString(
            Request(
                gameId = invalidUuid,
                playerName = game.players.first().name,
                passphrase = game.players.first().passphrase
            )
        )

        /**@Quando for feito uma requisição para comprar uma carta*/
        val response = client.post("/player/buyCard") {
            contentType(ContentType.Application.Json)
            setBody(requestJson)
        }

        /**@Então deverá retornar o status 400*/
        response.status shouldBe HttpStatusCode.BadRequest

        /**@E deverá retornar uma mensagem informando que o jogo não foi encontrado*/
        val responseBody = response.bodyAsText()
        val json = Json.decodeFromString<Map<String, String>>(responseBody)
        json["message"] shouldBe "Game $invalidUuid was not found."
    }

    @Test
    fun `Should return 400 when player name is invalid`() = testApplication {
        application {
            buyCardRoute(BuyCardHandler())
            configureExceptionHandling()
            module()
        }

        /**@Dado que exista um jogo com 2 jogadores jogando*/
        val playerList = mutableListOf(
            Domain.player(status = PlayerStatus.PLAYING),
            Domain.player(name = "joca", passphrase = "xpto", status = PlayerStatus.PLAYING)
        )
        val game = Domain.game(
            players = playerList,
            status = PLAYING
        )
        game.initialCards()
        Games.addGame(game)

        /**@E que exista uma request com um playerName inexistente na partida*/
        val requestJson = Json.encodeToString(
            Request(
                gameId = game.id.toString(),
                playerName = "lineuzinho",
                passphrase = game.players.first().passphrase
            )
        )

        /**@Quando for feito uma requisição para comprar uma carta*/
        val response = client.post("/player/buyCard") {
            contentType(ContentType.Application.Json)
            setBody(requestJson)
        }

        /**@Então deverá retornar o status 400*/
        response.status shouldBe HttpStatusCode.BadRequest

        /**@E deverá retornar uma mensagem informando que o jogador não foi encontrado no jogo*/
        val responseBody = response.bodyAsText()
        val json = Json.decodeFromString<Map<String, String>>(responseBody)
        json["message"] shouldBe "Player lineuzinho was not found in the game."
    }

    @Test
    fun `Should return 400 Bad Request and passphrase required when request is invalid`() = testApplication {
        application {
            buyCardRoute(BuyCardHandler())
            configureExceptionHandling()
            module()
        }

        /**@Dado que exista um jogo em andamento com 2 jogadores jogando*/
        val playerList = mutableListOf(
            Domain.player(status = PlayerStatus.PLAYING),
            Domain.player(name = "joca", passphrase = "xpto", status = PlayerStatus.PLAYING)
        )
        val game = Domain.game(
            players = playerList,
            status = PLAYING
        )
        game.initialCards()
        Games.addGame(game)

        /**@E que exista uma request com um passphrase invalido*/
        val requestJson = Json.encodeToString(
            Request(
                gameId = game.id.toString(),
                playerName = game.players.first().name,
                passphrase = "xpto"
            )
        )

        /**@Quando for feito uma requisição para comprar uma carta*/
        val response = client.post("/player/buyCard") {
            contentType(ContentType.Application.Json)
            setBody(requestJson)
        }

        /**@Então deverá retornar o status 400*/
        response.status shouldBe HttpStatusCode.BadRequest

        /**@E deverá retornar uma mensagem informando que o passphrase é invalido*/
        val responseBody = response.bodyAsText()
        val json = Json.decodeFromString<Map<String, String>>(responseBody)
        json["message"] shouldBe "Invalid passphrase."
    }

    @Test
    fun `Should return 400 when passphrase is empty`() = testApplication {
        application {
            buyCardRoute(BuyCardHandler())
            configureExceptionHandling()
            module()
        }

        /**@Dado que exista um jogo em andamento com 2 jogadores jogando*/
        val playerList = mutableListOf(
            Domain.player(status = PlayerStatus.PLAYING),
            Domain.player(name = "joca", passphrase = "xpto", status = PlayerStatus.PLAYING)
        )
        val game = Domain.game(
            players = playerList,
            status = PLAYING
        )
        game.initialCards()
        Games.addGame(game)

        /**@E que exista uma request com o campo de passphrase vazio*/
        val requestJson = Json.encodeToString(
            Request(
                gameId = game.id.toString(),
                playerName = game.players.first().name,
                passphrase = ""
            )
        )

        /**@Quando for feito uma requisição para comprar uma carta*/
        val response = client.post("/player/buyCard") {
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
    fun `Should return 400 bad request when game is not started`() = testApplication {
        application {
            buyCardRoute(BuyCardHandler())
            configureExceptionHandling()
            module()
        }
        /**@Dado que exista um jogo não iniciado com 2 jogadores*/
        val playerList = mutableListOf(
            Domain.player(),
            Domain.player(name = "joca", passphrase = "xpto")
        )
        val game = Domain.game(
            players = playerList,
            /**@E que o status do jogo seja de WAITING*/
            status = WAITING,
        )

        Games.addGame(game)

        /**@E que exista uma request com as informações de um jogador que não está na sua vez*/
        val requestJson = Json.encodeToString(
            Request(
                gameId = game.id.toString(),
                playerName = game.players.first().name,
                passphrase = game.players.first().passphrase
            )
        )

        /**@Quando for feito uma requisição para comprar uma carta*/
        val response = client.post("/player/buyCard") {
            contentType(ContentType.Application.Json)
            setBody(requestJson)
        }

        /**@Então deverá retornar o status 400*/
        response.status shouldBe HttpStatusCode.BadRequest

        /**@E deverá retornar uma mensagem informando que a partida não foi iniciada*/
        val responseBody = response.bodyAsText()
        val json = Json.decodeFromString<Map<String, String>>(responseBody)
        json["message"] shouldBe "The game must be started before you can play."

        /**@E os jogadores da sala não deverá sofrer nenhum acréscimo de carta*/
        val gameUpdated = getFirstGame()
        val firstPlayer = gameUpdated.findPlayer("Player")
        val secondPlayer = gameUpdated.findPlayer("joca")

        /**@E o estado da sala não deve sofrer alterações*/
        gameUpdated.status shouldBe WAITING

        firstPlayer.cards.size shouldBe 0
        secondPlayer.cards.size shouldBe 0
    }
}
