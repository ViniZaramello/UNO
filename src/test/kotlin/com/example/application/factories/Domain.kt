package com.example.application.factories

import com.example.application.model.Game
import com.example.application.model.Player

object Domain {

    fun player(
        name: String = "Player",
        passphrase: String = "passphrase",
        isOwner: Boolean = true,
    ) = Player(
        name = name,
        passphrase = passphrase,
        owner = isOwner,
    )

    fun game(
        player: Player = player(),
    ) = Game(
        players = mutableListOf(player),

    )
}