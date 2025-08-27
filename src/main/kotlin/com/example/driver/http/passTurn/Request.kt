package com.example.driver.http.passTurn

import com.example.application.command.PassTurn
import java.util.UUID
import kotlinx.serialization.Serializable

@Serializable
data class Request(
    val gameId: String,
    val playerName: String,
    val passphrase: String
) {
    fun toCommand(): PassTurn {
        return PassTurn(
            gameId = UUID.fromString(gameId.trim()),
            playerName = playerName.trim(),
            passphrase = passphrase.trim()
        )
    }
}
