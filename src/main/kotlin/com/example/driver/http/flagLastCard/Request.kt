package com.example.driver.http.flagLastCard

import com.example.application.command.FlagLastCard
import java.util.*
import kotlinx.serialization.Serializable

@Serializable
data class Request(
    val gameId: String,
    val playerName: String,
    val passphrase: String
) {
    fun toCommand(): FlagLastCard {
        return FlagLastCard(
            gameId = UUID.fromString(gameId.trim()),
            playerName = playerName.trim(),
            passphrase = passphrase.trim()
        )
    }
}
