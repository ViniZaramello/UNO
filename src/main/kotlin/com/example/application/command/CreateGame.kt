package com.example.application.command

import com.example.application.model.Game
import com.example.application.model.Player

data class CreateGame(
    val player: Player,
    val game: Game
) : CommandHandler {
}
