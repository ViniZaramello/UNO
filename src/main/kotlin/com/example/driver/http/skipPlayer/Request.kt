package com.example.driver.http.skipPlayer

import com.example.application.command.SkipPlayer
import java.util.UUID
import kotlinx.serialization.Serializable

@Serializable
data class Request(
    val gameId: String,
    val playerName: String,
    val passphrase: String,
    val targetPlayerName: String
) {
    fun toCommand(): SkipPlayer {
        return SkipPlayer(
            gameId = UUID.fromString(gameId.trim()),
            playerName = playerName.trim(),
            passphrase = passphrase.trim(),
            targetPlayerName = targetPlayerName.trim()
        )
    }
}
