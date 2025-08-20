package com.example.driver.http.throwCard

import com.example.application.factories.Domain
import com.example.application.handler.ThrowCardHandler
import com.example.application.model.Colors
import com.example.application.model.GameStatus.FINISHED
import com.example.application.model.GameStatus.PLAYING
import com.example.application.model.Games
import com.example.application.model.PlayerStatus
import com.example.application.model.SpecialType
import com.example.application.shared.commonCardList
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
    fun `Should return 202 when player throw a card`() = testApplication {
        application {
            throwCardRoute(ThrowCardHandler())
            module()
        }
        /**@Dado que exista um jogo já criado com 2 jogadores em partida*/
        val playerList = mutableListOf(
            Domain.player(status = PlayerStatus.PLAYING),
            Domain.player(name = "joca", passphrase = "xpto", status = PlayerStatus.PLAYING, number = 2)
        )
        val game = Domain.game(
            players = playerList,
            status = PLAYING,
            /**@E que o turno é do jogador numero 1*/
            playerTurn = 1
        )
        val cardInHand = Domain.card(name = "four red", number = "4")

        /**@E que o jogo está em andamento*/
        game.initialCards()
        game.stacks.cardsInTable.add(Domain.card())
        game.players.first().cards.add(cardInHand)
        Games.addGame(game)

        /**@E que exista uma request com dados validos*/
        val requestJson = Json.encodeToString(
            Request(
                gameId = game.id.toString(),
                playerName = game.players.first().name,
                passphrase = game.players.first().passphrase,
                cardId = cardInHand.id
            )
        )

        /**@Quando for feito uma requisição para lançar uma carta*/
        val response = client.post("/player/throwCard") {
            contentType(ContentType.Application.Json)
            setBody(requestJson)
        }

        /**@Então deverá retornar o status 202*/
        response.status shouldBe HttpStatusCode.Accepted

        /**@E o turno do jogador deve ser alterado para o proximo player*/
        val gameUpdated = getFirstGame()
        gameUpdated.playerTurn shouldBe 2

        /**@E o primeiro jogador deverá permanecer com 7 cartas na mão*/
        val firstPlayer = gameUpdated.findPlayer("Player")
        firstPlayer.cards.size shouldBe 7

        /**@E o segundo jogador não deverá conter alteração no total de cartas em sua mão*/
        val secondPlayer = gameUpdated.findPlayer("joca")
        secondPlayer.cards.size shouldBe 7

        /**@E a carta lançada deverá ser adicionada na pilha da mesa*/
        gameUpdated.stacks.cardsInTable.size shouldBe 3
        gameUpdated.stacks.cardsInTable.last().id shouldBe cardInHand.id
        gameUpdated.stacks.cardsInTable.last().name shouldBe cardInHand.name
        gameUpdated.stacks.cardsInTable.last().color shouldBe cardInHand.color
        gameUpdated.stacks.cardsInTable.last().number shouldBe cardInHand.number
        gameUpdated.stacks.cardsInTable.last().especial shouldBe cardInHand.especial
    }

    @Test
    fun `Should return 202 when player throw a special card for purchase`() = testApplication {
        application {
            throwCardRoute(ThrowCardHandler())
            module()
        }
        /**@Dado que exista um jogo já criado com 2 jogadores em partida*/
        val playerList = mutableListOf(
            Domain.player(status = PlayerStatus.PLAYING, cards = commonCardList()),
            Domain.player(
                name = "joca",
                passphrase = "xpto",
                status = PlayerStatus.PLAYING,
                number = 2,
                cards = commonCardList()
            )
        )
        val game = Domain.game(
            players = playerList,
            status = PLAYING,
            /**@E que o turno é do jogador numero 1*/
            playerTurn = 1
        )
        val cardInHand = Domain.card(
            name = "buy-four",
            number = "plusFour",
            special = SpecialType.BUY_FOUR,
            color = Colors.BLACK
        )

        /**@E que o jogo está em andamento*/
        game.stacks.cardsInTable.add(Domain.card())
        game.players.first().cards.add(cardInHand)
        Games.addGame(game)

        /**@E que exista uma request com dados validos*/
        val requestJson = Json.encodeToString(
            Request(
                gameId = game.id.toString(),
                playerName = game.players.first().name,
                passphrase = game.players.first().passphrase,
                cardId = cardInHand.id,
                color = "blue"

            )
        )

        /**@Quando for feito uma requisição para lançar uma carta*/
        val response = client.post("/player/throwCard") {
            contentType(ContentType.Application.Json)
            setBody(requestJson)
        }

        /**@Então deverá retornar o status 202*/
        response.status shouldBe HttpStatusCode.Accepted

        /**@E o turno do jogador deve ser passado novamente para o jogador que lançou*/
        val gameUpdated = getFirstGame()
        gameUpdated.playerTurn shouldBe 1

        /**@E o primeiro jogador deverá permanecer com 7 cartas na mão*/
        val firstPlayer = gameUpdated.findPlayer("Player")
        firstPlayer.cards.size shouldBe 3

        /**@E o segundo jogador não deverá conter alteração no total de cartas em sua mão*/
        val secondPlayer = gameUpdated.findPlayer("joca")
        secondPlayer.cards.size shouldBe 7

        /**@E a carta lançada deverá ser adicionada na pilha da mesa*/
        gameUpdated.stacks.cardsInTable.size shouldBe 3
        gameUpdated.stacks.cardsInTable.last().id shouldBe cardInHand.id
        gameUpdated.stacks.cardsInTable.last().name shouldBe cardInHand.name
        gameUpdated.stacks.cardsInTable.last().color shouldBe cardInHand.color
        gameUpdated.stacks.cardsInTable.last().number shouldBe cardInHand.number
        gameUpdated.stacks.cardsInTable.last().especial shouldBe cardInHand.especial
    }

    @Test
    fun `Should return 202 when player throw a block card and skip next player`() = testApplication {
        application {
            throwCardRoute(ThrowCardHandler())
            module()
        }
        /**@Dado que exista um jogo já criado com 3 jogadores em partida*/
        val playerList = mutableListOf(
            Domain.player(status = PlayerStatus.PLAYING, cards = commonCardList()),
            Domain.player(
                name = "joca",
                passphrase = "xpto",
                status = PlayerStatus.PLAYING,
                number = 2,
                cards = commonCardList()
            ),
            Domain.player(
                name = "carlos",
                passphrase = "xpto",
                status = PlayerStatus.PLAYING,
                number = 3,
                cards = commonCardList()
            )
        )
        val game = Domain.game(
            players = playerList,
            status = PLAYING,
            /**@E que o turno é do jogador numero 1*/
            playerTurn = 1
        )
        val cardInHand = Domain.card(
            name = "block",
            number = "block",
            special = SpecialType.BLOCK,
            color = Colors.RED
        )

        /**@E que o jogo está em andamento*/
        game.stacks.cardsInTable.add(Domain.card())
        game.players.first().cards.add(cardInHand)
        Games.addGame(game)

        /**@E que exista uma request com dados validos*/
        val requestJson = Json.encodeToString(
            Request(
                gameId = game.id.toString(),
                playerName = game.players.first().name,
                passphrase = game.players.first().passphrase,
                cardId = cardInHand.id
            )
        )

        /**@Quando for feito uma requisição para lançar uma carta*/
        val response = client.post("/player/throwCard") {
            contentType(ContentType.Application.Json)
            setBody(requestJson)
        }

        /**@Então deverá retornar o status 202*/
        response.status shouldBe HttpStatusCode.Accepted

        /**@E o turno do jogador deve ser alterado para o proximo player*/
        val gameUpdated = getFirstGame()
        gameUpdated.playerTurn shouldBe 3

        /**@E o primeiro jogador deverá permanecer com 3 cartas na mão*/
        val firstPlayer = gameUpdated.findPlayer("Player")
        firstPlayer.cards.size shouldBe 3

        /**@E o segundo jogador não deverá conter alteração no total de cartas em sua mão*/
        val secondPlayer = gameUpdated.findPlayer("joca")
        secondPlayer.cards.size shouldBe 3

        /**@E a carta lançada deverá ser adicionada na pilha da mesa*/
        gameUpdated.stacks.cardsInTable.size shouldBe 3
        gameUpdated.stacks.cardsInTable.last().id shouldBe cardInHand.id
        gameUpdated.stacks.cardsInTable.last().name shouldBe cardInHand.name
        gameUpdated.stacks.cardsInTable.last().color shouldBe cardInHand.color
        gameUpdated.stacks.cardsInTable.last().number shouldBe cardInHand.number
        gameUpdated.stacks.cardsInTable.last().especial shouldBe cardInHand.especial
    }

    @Test
    fun `Should return 202 when player throw a special card for purchase when next player have a purshase card`() =
        testApplication {
            application {
                throwCardRoute(ThrowCardHandler())
                module()
            }
            /**@Dado que exista um jogo já criado com 2 jogadores em partida*/
            val firstPlayerSpecialCard = Domain.card(
                name = "buy-four",
                number = "plusFour",
                special = SpecialType.BUY_FOUR,
                color = Colors.BLACK
            )
            val secondPlayerSpecialCard = Domain.card(
                name = "buy-four",
                number = "plusFour",
                special = SpecialType.BUY_FOUR,
                color = Colors.BLACK
            )

            val firstPlayerCards = commonCardList()
            firstPlayerCards.add(firstPlayerSpecialCard)

            val secondPlayerCards = commonCardList()
            secondPlayerCards.add(secondPlayerSpecialCard)

            val playerList = mutableListOf(
                Domain.player(status = PlayerStatus.PLAYING, cards = firstPlayerCards),
                Domain.player(
                    name = "joca",
                    passphrase = "xpto",
                    status = PlayerStatus.PLAYING,
                    number = 2,
                    cards = secondPlayerCards
                )
            )

            val game = Domain.game(
                players = playerList,
                status = PLAYING,
                /**@E que o turno é do jogador numero 1*/
                playerTurn = 1
            )


            /**@E que o jogo está em andamento*/
            game.stacks.cardsInTable.add(Domain.card())
            Games.addGame(game)

            /**@E que exista uma request com dados validos*/
            val requestJson = Json.encodeToString(
                Request(
                    gameId = game.id.toString(),
                    playerName = game.players.first().name,
                    passphrase = game.players.first().passphrase,
                    cardId = firstPlayerSpecialCard.id,
                    color = "blue"

                )
            )

            /**@Quando for feito uma requisição para lançar uma carta*/
            val response = client.post("/player/throwCard") {
                contentType(ContentType.Application.Json)
                setBody(requestJson)
            }

            /**@Então deverá retornar o status 202*/
            response.status shouldBe HttpStatusCode.Accepted

            /**@E o turno do jogador deve ser passado para o proximo jogador*/
            val gameUpdated = getFirstGame()
            gameUpdated.playerTurn shouldBe 2

            /**@E deverá adicionar a quantidade de cartas como pendencia*/
            gameUpdated.buyCardQuantity shouldBe 4

            /**@E o primeiro jogador deverá permanecer com 3 cartas na mão*/
            val firstPlayer = gameUpdated.findPlayer("Player")
            firstPlayer.cards.size shouldBe 3

            /**@E o segundo jogador não deverá conter alteração no total de cartas em sua mão*/
            val secondPlayer = gameUpdated.findPlayer("joca")
            secondPlayer.cards.size shouldBe 4

            /**@E a carta lançada deverá ser adicionada na pilha da mesa*/
            gameUpdated.stacks.cardsInTable.size shouldBe 3
            gameUpdated.stacks.cardsInTable.last().id shouldBe firstPlayerSpecialCard.id
            gameUpdated.stacks.cardsInTable.last().name shouldBe firstPlayerSpecialCard.name
            gameUpdated.stacks.cardsInTable.last().color shouldBe firstPlayerSpecialCard.color
            gameUpdated.stacks.cardsInTable.last().number shouldBe firstPlayerSpecialCard.number
            gameUpdated.stacks.cardsInTable.last().especial shouldBe firstPlayerSpecialCard.especial
        }

    @Test
    fun `Should return 202 when player throw a reverse card`() = testApplication {
        application {
            throwCardRoute(ThrowCardHandler())
            module()
        }
        /**@Dado que exista um jogo já criado com 3 jogadores em partida*/
        val playerList = mutableListOf(
            Domain.player(status = PlayerStatus.PLAYING, cards = commonCardList()),
            Domain.player(
                name = "joca",
                passphrase = "xpto",
                status = PlayerStatus.PLAYING,
                number = 2,
                cards = commonCardList()
            ),
            Domain.player(
                name = "abu",
                passphrase = "xpto",
                status = PlayerStatus.PLAYING,
                number = 3,
                cards = commonCardList()
            )
        )
        val game = Domain.game(
            players = playerList,
            status = PLAYING,
            /**@E que o turno é do jogador numero 1*/
            playerTurn = 1
        )
        val cardInHand = Domain.card(
            name = "reverse red",
            number = "reverse",
            special = SpecialType.REVERSE,
            color = Colors.RED
        )

        /**@E que o jogo está em andamento*/
        game.stacks.cardsInTable.add(Domain.card())
        game.players.first().cards.add(cardInHand)
        Games.addGame(game)

        /**@E que exista uma request com dados validos*/
        val requestJson = Json.encodeToString(
            Request(
                gameId = game.id.toString(),
                playerName = game.players.first().name,
                passphrase = game.players.first().passphrase,
                cardId = cardInHand.id,
                color = "blue"

            )
        )

        /**@Quando for feito uma requisição para lançar uma carta*/
        val response = client.post("/player/throwCard") {
            contentType(ContentType.Application.Json)
            setBody(requestJson)
        }

        /**@Então deverá retornar o status 202*/
        response.status shouldBe HttpStatusCode.Accepted

        /**@E o turno do jogador deve ser passado para o terceiro jogador*/
        val gameUpdated = getFirstGame()
        gameUpdated.playerTurn shouldBe 3
        game.reverse shouldBe true

        /**@E o primeiro jogador deverá permanecer com 3 cartas na mão*/
        val firstPlayer = gameUpdated.findPlayer("Player")
        firstPlayer.cards.size shouldBe 3

        /**@E a carta lançada deverá ser adicionada na pilha da mesa*/
        gameUpdated.stacks.cardsInTable.size shouldBe 3
        gameUpdated.stacks.cardsInTable.last().id shouldBe cardInHand.id
        gameUpdated.stacks.cardsInTable.last().name shouldBe cardInHand.name
        gameUpdated.stacks.cardsInTable.last().color shouldBe cardInHand.color
        gameUpdated.stacks.cardsInTable.last().number shouldBe cardInHand.number
        gameUpdated.stacks.cardsInTable.last().especial shouldBe cardInHand.especial
    }

    @Test
    fun `Should return 202 when player throw a last card`() = testApplication {
        application {
            throwCardRoute(ThrowCardHandler())
            module()
        }
        /**@Dado que exista um jogo já criado com 2 jogadores em partida*/
        val playerList = mutableListOf(
            Domain.player(status = PlayerStatus.PLAYING, lastCard = true),
            Domain.player(
                name = "joca",
                passphrase = "xpto",
                status = PlayerStatus.PLAYING,
                number = 2,
                cards = commonCardList()
            )
        )
        val game = Domain.game(
            players = playerList,
            status = PLAYING,
            /**@E que o turno é do jogador numero 1*/
            playerTurn = 1
        )
        val cardInHand = Domain.card(name = "four red", number = "4")

        /**@E que o jogo está em andamento*/
        game.stacks.cardsInTable.add(Domain.card())
        game.players.first().cards.add(cardInHand)
        Games.addGame(game)

        /**@E que exista uma request com dados validos*/
        val requestJson = Json.encodeToString(
            Request(
                gameId = game.id.toString(),
                playerName = game.players.first().name,
                passphrase = game.players.first().passphrase,
                cardId = cardInHand.id
            )
        )

        /**@Quando for feito uma requisição para lançar uma carta*/
        val response = client.post("/player/throwCard") {
            contentType(ContentType.Application.Json)
            setBody(requestJson)
        }

        /**@Então deverá retornar o status 202*/
        response.status shouldBe HttpStatusCode.Accepted

        /**@E o turno do jogador deve ser alterado para o proximo player*/
        val gameUpdated = getFirstGame()
        gameUpdated.playerTurn shouldBe 2

        /**@E o primeiro jogador deverá permanecer com nenhuma carta na mão*/
        val firstPlayer = gameUpdated.findPlayer("Player")
        firstPlayer.cards.size shouldBe 0

        /**@E seu status deverá ser atualizado para FINISHED*/
        firstPlayer.statusInGame shouldBe PlayerStatus.FINISHED

        /**@E o segundo jogador não deverá conter alteração no total de cartas em sua mão*/
        val secondPlayer = gameUpdated.findPlayer("joca")
        secondPlayer.cards.size shouldBe 3

        /**@E a carta lançada deverá ser adicionada na pilha da mesa*/
        gameUpdated.stacks.cardsInTable.size shouldBe 3
        gameUpdated.stacks.cardsInTable.last().id shouldBe cardInHand.id
        gameUpdated.stacks.cardsInTable.last().name shouldBe cardInHand.name
        gameUpdated.stacks.cardsInTable.last().color shouldBe cardInHand.color
        gameUpdated.stacks.cardsInTable.last().number shouldBe cardInHand.number
        gameUpdated.stacks.cardsInTable.last().especial shouldBe cardInHand.especial
    }

    @Test
    fun `Should return 202 when player throw a last card and not signal`() = testApplication {
        application {
            throwCardRoute(ThrowCardHandler())
            module()
        }
        /**@Dado que exista um jogo já criado com 2 jogadores em partida*/
        val playerList = mutableListOf(
            Domain.player(status = PlayerStatus.PLAYING),
            Domain.player(
                name = "joca",
                passphrase = "xpto",
                status = PlayerStatus.PLAYING,
                number = 2,
                cards = commonCardList()
            )
        )
        val game = Domain.game(
            players = playerList,
            status = PLAYING,
            /**@E que o turno é do jogador numero 1*/
            playerTurn = 1
        )
        val cardInHand = Domain.card(name = "four red", number = "4")

        /**@E que o jogo está em andamento*/
        game.stacks.cardsInTable.add(Domain.card())
        game.players.first().cards.add(cardInHand)
        Games.addGame(game)

        /**@E que exista uma request com dados validos*/
        val requestJson = Json.encodeToString(
            Request(
                gameId = game.id.toString(),
                playerName = game.players.first().name,
                passphrase = game.players.first().passphrase,
                cardId = cardInHand.id
            )
        )

        /**@Quando for feito uma requisição para lançar uma carta*/
        val response = client.post("/player/throwCard") {
            contentType(ContentType.Application.Json)
            setBody(requestJson)
        }

        /**@Então deverá retornar o status 202*/
        response.status shouldBe HttpStatusCode.Accepted

        /**@E o turno do jogador deve ser alterado para o proximo player*/
        val gameUpdated = getFirstGame()
        gameUpdated.playerTurn shouldBe 2

        /**@E o primeiro jogador deverá permanecer com duas cartas na mão*/
        val firstPlayer = gameUpdated.findPlayer("Player")
        firstPlayer.cards.size shouldBe 2

        /**@E o segundo jogador não deverá conter alteração no total de cartas em sua mão*/
        val secondPlayer = gameUpdated.findPlayer("joca")
        secondPlayer.cards.size shouldBe 3

        /**@E a carta lançada deverá ser adicionada na pilha da mesa*/
        gameUpdated.stacks.cardsInTable.size shouldBe 3
        gameUpdated.stacks.cardsInTable.last().id shouldBe cardInHand.id
        gameUpdated.stacks.cardsInTable.last().name shouldBe cardInHand.name
        gameUpdated.stacks.cardsInTable.last().color shouldBe cardInHand.color
        gameUpdated.stacks.cardsInTable.last().number shouldBe cardInHand.number
        gameUpdated.stacks.cardsInTable.last().especial shouldBe cardInHand.especial
    }

    @Test
    fun `Should return 400 Bad Request when color is invalid`() = testApplication {
        application {
            throwCardRoute(ThrowCardHandler())
            configureExceptionHandling()
            module()
        }
        /**@Dado que exista um jogo já criado com 2 jogadores em partida*/
        val playerList = mutableListOf(
            Domain.player(status = PlayerStatus.PLAYING, owner = false),
            Domain.player(name = "joca", passphrase = "xpto", status = PlayerStatus.PLAYING, number = 2)
        )
        val cardInHand = Domain.card(
            color = Colors.BLACK,
            name = "change-color",
            number = "changeColor",
            special = SpecialType.CHANGE_COLOR
        )
        val game = Domain.game(
            players = playerList,
            status = PLAYING,
            /**@E que o turno é do jogador numero 1*/
            playerTurn = 1
        )
        /**@E que o jogo está em andamento*/
        game.players.first().cards.add(cardInHand)
        game.initialCards()
        Games.addGame(game)

        /**@E que exista uma request com uma cor invalida*/
        val requestJson = Json.encodeToString(
            Request(
                gameId = game.id.toString(),
                playerName = game.players.first().name,
                passphrase = game.players.first().passphrase,
                cardId = game.players.first().cards.first().id,
                color = "purple"
            )
        )

        /**@Quando for feito uma requisição para lançar uma carta*/
        val response = client.post("/player/throwCard") {
            contentType(ContentType.Application.Json)
            setBody(requestJson)
        }

        /**@Então deverá retornar o status 400*/
        response.status shouldBe HttpStatusCode.BadRequest

        /**@E deverá retornar uma mensagem informando que a cor é inválida*/
        val responseBody = response.bodyAsText()
        val json = Json.decodeFromString<Map<String, String>>(responseBody)
        json["message"] shouldBe "The color purple is not valid."

        /**@E o turno não deverá ser passado */
        val gameUpdated = getFirstGame()
        gameUpdated.playerTurn shouldBe 2

        /**@E os jogadores não deverão sofrer alteração na quantidade de carta */
        val firstPlayer = gameUpdated.findPlayer("Player")
        firstPlayer.cards.size shouldBe 8

        val secondPlayer = gameUpdated.findPlayer("joca")
        secondPlayer.cards.size shouldBe 7
    }

    @Test
    fun `Should return 400 Bad Request when player name is invalid`() = testApplication {
        application {
            throwCardRoute(ThrowCardHandler())
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
                cardId = game.players.first().cards.first().id
            )
        )

        /**@Quando for feito uma requisição para pular um jogador*/
        val response = client.post("/player/throwCard") {
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

        /**@E os jogadores não deverão sofrer alteração na quantidade de carta */
        val firstPlayer = gameUpdated.findPlayer("Player")
        firstPlayer.cards.size shouldBe 7

        val secondPlayer = gameUpdated.findPlayer("joca")
        secondPlayer.cards.size shouldBe 7
    }

    @Test
    fun `Should return 400 Bad Request when it is not the target player turn`() = testApplication {
        application {
            throwCardRoute(ThrowCardHandler())
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

        /**@E que exista uma request valida*/
        val requestJson = Json.encodeToString(
            Request(
                gameId = game.id.toString(),
                playerName = game.players.first().name,
                passphrase = game.players.first().passphrase,
                cardId = game.players.first().cards.first().id
            )
        )

        /**@Quando for feito uma requisição para lançar uma carta*/
        val response = client.post("/player/throwCard") {
            contentType(ContentType.Application.Json)
            setBody(requestJson)
        }

        /**@Então deverá retornar o status 400*/
        response.status shouldBe HttpStatusCode.BadRequest

        /**@E deverá retornar uma mensagem informando que não está na vez do jogador*/
        val responseBody = response.bodyAsText()
        val json = Json.decodeFromString<Map<String, String>>(responseBody)
        json["message"] shouldBe "It is not your turn to play a card."

        /**@E a carta não deverá ser adicionado na pilha da mesa*/
        val gameUpdated = getFirstGame()
        gameUpdated.stacks.cardsInTable.size shouldBe 1

        /**@E o turno deverá permanecer para o jogador 2*/
        gameUpdated.playerTurn shouldBe 2

        /**@E o primeiro jogador não deverá ter sua carta subtraida*/
        val firstPlayer = gameUpdated.findPlayer("Player")
        firstPlayer.cards.size shouldBe 7

        /**@E o segundo jogador não deverá sofrer qualquer alteração na quantidade de cartas*/
        val secondPlayer = gameUpdated.findPlayer("joca")
        secondPlayer.cards.size shouldBe 7
    }

    @Test
    fun `Should return 400 Bad Request when gameStatus is different of PLAYING`() = testApplication {
        application {
            throwCardRoute(ThrowCardHandler())
            configureExceptionHandling()
            module()
        }
        /**@Dado que exista um jogo já criado e com o status FINISHED*/
        val players = mutableListOf(Domain.player(), Domain.player(name = "joca", passphrase = "xpto"))
        val game = Domain.game(status = FINISHED, players = players)
        game.initialCards()
        Games.addGame(game)

        /**@E que exista uma request valida*/
        val requestJson = Json.encodeToString(
            Request(
                gameId = game.id.toString(),
                playerName = game.players.first().name,
                passphrase = game.players.first().passphrase,
                cardId = game.players.first().cards.first().id
            )
        )

        /**@Quando for feito uma requisição para lançar uma carta*/
        val response = client.post("/player/throwCard") {
            contentType(ContentType.Application.Json)
            setBody(requestJson)
        }

        /**@Então deverá retornar o status 400*/
        response.status shouldBe HttpStatusCode.BadRequest

        /**@E deverá retornar uma mensagem informando que o jogo precisa ser iniciado*/
        val responseBody = response.bodyAsText()
        val json = Json.decodeFromString<Map<String, String>>(responseBody)
        json["message"] shouldBe "The game must be started before you can play."

        /**@E a carta não deverá ir para a pilha da mesa*/
        val gameUpdated = getFirstGame()
        gameUpdated.stacks.cardsInTable.size shouldBe 1

        /**@E o turno não deverá ser passado para o proximo jogador*/
        gameUpdated.playerTurn shouldBe 1

        /**@E o jogador que lançou a carta não deverá ter ela descontada da sua mão */
        val firstPlayer = gameUpdated.findPlayer("Player")
        firstPlayer.cards.size shouldBe 7
    }

    @Test
    fun `Should return 400 Bad Request and passphrase required when request is invalid`() = testApplication {
        application {
            throwCardRoute(ThrowCardHandler())
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
            /**@E que o turno é do jogador numero 1*/
            playerTurn = 1
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
                cardId = game.players.first().cards.first().id
            )
        )

        /**@Quando for feito uma requisição para lançar uma carta*/
        val response = client.post("/player/throwCard") {
            contentType(ContentType.Application.Json)
            setBody(requestJson)
        }

        /**@Então deverá retornar o status 400*/
        response.status shouldBe HttpStatusCode.BadRequest

        /**@E deverá retornar uma mensagem informando que o passphrase é invalido*/
        val responseBody = response.bodyAsText()
        val json = Json.decodeFromString<Map<String, String>>(responseBody)
        json["message"] shouldBe "Invalid passphrase."

        /**@E a carta não deverá ir para a pilha da mesa*/
        val gameUpdated = getFirstGame()
        gameUpdated.stacks.cardsInTable.size shouldBe 1

        /**@E o turno não deverá ser passado para o proximo jogador*/
        gameUpdated.playerTurn shouldBe 1

        /**@E o jogador que lançou a carta não deverá ter ela descontada da sua mão */
        val firstPlayer = gameUpdated.findPlayer("Player")
        firstPlayer.cards.size shouldBe 7
    }

    @Test
    fun `Should return 400 Bad Request when cardId is invalid`() = testApplication {
        application {
            throwCardRoute(ThrowCardHandler())
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
            /**@E que o turno é do jogador numero 1*/
            playerTurn = 1
        )
        /**@E que o jogo está em andamento*/
        game.initialCards()
        Games.addGame(game)

        /**@E que exista uma request com um cardId inexistente*/
        val requestJson = Json.encodeToString(
            Request(
                gameId = game.id.toString(),
                playerName = game.players.first().name,
                passphrase = game.players.first().passphrase,
                cardId = randomUUID().toString()
            )
        )

        /**@Quando for feito uma requisição para lançar uma carta*/
        val response = client.post("/player/throwCard") {
            contentType(ContentType.Application.Json)
            setBody(requestJson)
        }

        /**@Então deverá retornar o status 400*/
        response.status shouldBe HttpStatusCode.BadRequest

        /**@E deverá retornar uma mensagem informando que a carta não foi encontrada*/
        val responseBody = response.bodyAsText()
        val json = Json.decodeFromString<Map<String, String>>(responseBody)
        json["message"] shouldBe "The player does not have this card in hand."

        /**@E a carta não deverá ir para a pilha da mesa*/
        val gameUpdated = getFirstGame()
        gameUpdated.stacks.cardsInTable.size shouldBe 1

        /**@E o turno não deverá ser passado para o proximo jogador*/
        gameUpdated.playerTurn shouldBe 1

        /**@E o jogador que lançou a carta não deverá ter ela descontada da sua mão */
        val firstPlayer = gameUpdated.findPlayer("Player")
        firstPlayer.cards.size shouldBe 7
    }

    @Test
    fun `Should return 400 Bad Request when gameId is invalid`() = testApplication {
        application {
            throwCardRoute(ThrowCardHandler())
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
            /**@E que o turno é do jogador numero 1*/
            playerTurn = 1
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
                cardId = game.players.first().cards.first().id
            )
        )

        /**@Quando for feito uma requisição para lançar uma carta*/
        val response = client.post("/player/throwCard") {
            contentType(ContentType.Application.Json)
            setBody(requestJson)
        }

        /**@Então deverá retornar o status 400*/
        response.status shouldBe HttpStatusCode.BadRequest

        /**@E deverá retornar uma mensagem informando que o jogo não foi encontrado*/
        val responseBody = response.bodyAsText()
        val json = Json.decodeFromString<Map<String, String>>(responseBody)
        json["message"] shouldBe "Game $invalidUuid was not found."

        /**@E a carta não deverá ir para a pilha da mesa*/
        val gameUpdated = getFirstGame()
        gameUpdated.stacks.cardsInTable.size shouldBe 1

        /**@E o turno não deverá ser passado para o proximo jogador*/
        gameUpdated.playerTurn shouldBe 1

        /**@E o jogador que lançou a carta não deverá ter ela descontada da sua mão */
        val firstPlayer = gameUpdated.findPlayer("Player")
        firstPlayer.cards.size shouldBe 7
    }

    @Test
    fun `Should return 400 Bad Request when parity is invalid`() = testApplication {
        application {
            throwCardRoute(ThrowCardHandler())
            configureExceptionHandling()
            module()
        }
        /**@Dado que exista um jogo já criado com 2 jogadores em partida*/
        val playerList = mutableListOf(
            Domain.player(status = PlayerStatus.PLAYING, owner = false),
            Domain.player(name = "joca", passphrase = "xpto", status = PlayerStatus.PLAYING, number = 2)
        )
        val cardInHand = Domain.card(
            color = Colors.BLUE,
            name = "eight blue",
            number = "8",
            special = SpecialType.NONE
        )
        val game = Domain.game(
            players = playerList,
            status = PLAYING,
            /**@E que o turno é do jogador numero 1*/
            playerTurn = 1
        )
        /**@E que o jogo está em andamento*/
        game.players.first().cards.add(cardInHand)
        game.initialCards()
        game.stacks.cardsInTable.add(Domain.card())
        Games.addGame(game)

        /**@E que exista uma request com uma cor invalida*/
        val requestJson = Json.encodeToString(
            Request(
                gameId = game.id.toString(),
                playerName = game.players.first().name,
                passphrase = game.players.first().passphrase,
                cardId = game.players.first().cards.first().id
            )
        )

        /**@Quando for feito uma requisição para lançar uma carta*/
        val response = client.post("/player/throwCard") {
            contentType(ContentType.Application.Json)
            setBody(requestJson)
        }

        /**@Então deverá retornar o status 400*/
        response.status shouldBe HttpStatusCode.BadRequest

        /**@E deverá retornar uma mensagem informando que a carta não é compativel*/
        val responseBody = response.bodyAsText()
        val json = Json.decodeFromString<Map<String, String>>(responseBody)
        json["message"] shouldBe "The card does not have any similar characteristics to the card on the table."

        /**@E o turno não deverá ser passado */
        val gameUpdated = getFirstGame()
        gameUpdated.playerTurn shouldBe 1
        gameUpdated.stacks.cardsInTable.size shouldBe 2

        /**@E os jogadores não deverão sofrer alteração na quantidade de carta */
        val firstPlayer = gameUpdated.findPlayer("Player")
        firstPlayer.cards.size shouldBe 8

        val secondPlayer = gameUpdated.findPlayer("joca")
        secondPlayer.cards.size shouldBe 7
    }
}
