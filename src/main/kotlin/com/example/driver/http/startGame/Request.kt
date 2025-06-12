package com.example.driver.http.startGame

import com.example.application.command.StartGame
import kotlinx.serialization.Serializable

@Serializable
data class Request(
    val gameId: String,
    val playerName: String,
    val passphrase: String
) {
    fun toCommand(): StartGame {
        return StartGame(
            gameId = gameId.trim(),
            playerName = playerName.trim(),
            passphrase = passphrase.trim()
        )
    }
}
