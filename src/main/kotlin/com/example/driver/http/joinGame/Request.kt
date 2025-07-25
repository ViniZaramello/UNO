package com.example.driver.http.joinGame

import com.example.application.command.JoinPlayerInGame
import com.example.application.model.Player
import java.util.UUID
import kotlinx.serialization.Serializable

@Serializable
data class Request(
    val name: String,
    val passphrase: String,
    val gameId: String,
) {
    fun toCommand(): JoinPlayerInGame {
        val player = Player(
            name = name.trim(),
            passphrase = passphrase.trim(),
        )
        return JoinPlayerInGame(player = player, gameId = UUID.fromString(gameId.trim()))
    }
}
