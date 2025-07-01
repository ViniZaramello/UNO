package com.example.driver.http.quitGame

import com.example.application.command.QuitGame
import java.util.*
import kotlinx.serialization.Serializable

@Serializable
data class Request(
    val name: String,
    val passphrase: String,
    val gameId: String,
) {
    fun toCommand(): QuitGame {
        return QuitGame(
            gameId = UUID.fromString(gameId.trim()),
            playerName = name.trim(),
            passphrase = passphrase.trim(),
        )
    }
}
