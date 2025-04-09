package com.example.application.model

import com.example.application.model.vo.CardsMovements
import com.example.application.model.vo.PlayerLimit
import java.util.UUID

data class Game(
    val id: UUID = UUID.randomUUID(),
    val gameMode: GameMode = GameMode.DEFAULT,
    val status: GameStatus = GameStatus.CREATED,
    val playerLimit: PlayerLimit,
    val players: List<Player>? = null,
    val playerTurn: Int = 1,
    val leader: Player,
    val cardsInTable: CardsMovements //criar vo
    //criar classe que estenda cardsInTable que consulte as outras lista, busca, adiciona, remove, limpa

)
