package com.example.driver.http.endGame

import com.example.application.command.EndGame
import java.util.UUID
import kotlinx.serialization.Serializable

@Serializable
data class Request(
    val gameId: String,
    val playerName: String,
    val passphrase: String
) {
    fun toCommand(): EndGame {
        return EndGame(
            gameId = UUID.fromString(gameId.trim()),
            playerName = playerName.trim(),
            passphrase = passphrase.trim()
        )
    }
}
