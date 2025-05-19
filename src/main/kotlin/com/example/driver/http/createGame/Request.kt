package com.example.driver.http.createGame

import com.example.application.command.CreateGame
import com.example.application.model.Player
import kotlinx.serialization.Serializable

@Serializable
data class Request(
    val name: String,
    val passphrase: String
) {
    fun toCommand(): CreateGame {
        val player = Player(
            name = name.trim(),
            passphrase = passphrase.trim(),
            owner = true,
        )
        return CreateGame(player = player)
    }
}
