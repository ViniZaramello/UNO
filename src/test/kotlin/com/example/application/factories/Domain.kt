package com.example.application.factories

import com.example.application.model.Card
import com.example.application.model.Colors
import com.example.application.model.Game
import com.example.application.model.GameMode
import com.example.application.model.GameStatus
import com.example.application.model.Player
import com.example.application.model.PlayerStatus
import com.example.application.model.SpecialType
import com.example.application.model.vo.PlayerLimit
import com.example.application.model.vo.StackCards
import java.util.UUID

object Domain {

    fun player(
        name: String = "Player",
        passphrase: String = "passphrase",
        owner: Boolean = true,
        status: PlayerStatus = PlayerStatus.IN_LOBBY,
        number: Int = 1

    ) = Player(
        name = name,
        passphrase = passphrase,
        owner = owner,
        statusInGame = status,
        number = number
    )

    fun game(
        id: UUID = UUID.randomUUID(),
        gameMode: GameMode = GameMode.DEFAULT,
        status: GameStatus = GameStatus.CREATED,
        playerLimit: PlayerLimit = PlayerLimit(),
        players: MutableList<Player> = mutableListOf(player()),
        playerTurn: Int = 1,
        stacks: StackCards = StackCards(),
        firstCard: Card = stacks.cardsInTable.first(),
        buyCardQuantity: Int = 0,
        reverse: Boolean = false,
        blockPending: Boolean = false
    ) = Game(
        id = id,
        gameMode = gameMode,
        status = status,
        playerLimit = playerLimit,
        players = players,
        playerTurn = playerTurn,
        stacks = stacks,
        firstCard = firstCard,
        buyCardQuantity = buyCardQuantity,
        reverse = reverse,
        blockPending = blockPending
    )

    fun card(
        id: String = UUID.randomUUID().toString(),
        color: Colors = Colors.RED,
        name: String = "Five Red",
        number: String = "5",
        special: SpecialType = SpecialType.NONE

    ) = Card(
        id = id,
        color = color,
        name = name,
        number = number,
        especial = special,
    )
}
