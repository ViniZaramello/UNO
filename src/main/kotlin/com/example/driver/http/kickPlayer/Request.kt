package com.example.driver.http.kickPlayer

import com.example.application.command.KickPlayer
import java.util.*
import kotlinx.serialization.Serializable

@Serializable
data class Request(
    val gameId: String,
    val playerName: String,
    val passphrase: String,
    val targetPlayerName: String
) {
    fun toCommand(): KickPlayer {
        return KickPlayer(
            gameId = UUID.fromString(gameId.trim()),
            playerName = playerName.trim(),
            passphrase = passphrase.trim(),
            playerTarget = targetPlayerName.trim()
        )
    }
}
