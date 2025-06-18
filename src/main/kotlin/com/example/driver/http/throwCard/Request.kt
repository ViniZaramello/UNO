package com.example.driver.http.throwCard

import com.example.application.command.ThrowCard
import java.util.*
import kotlinx.serialization.Serializable

@Serializable
data class Request(
    val gameId: String,
    val playerName: String,
    val passphrase: String,
    val cardId: String,
    val color: String? = null
) {
    fun toCommand(): ThrowCard {
        return ThrowCard(
            gameId = UUID.fromString(gameId.trim()),
            playerName = playerName.trim(),
            passphrase = passphrase.trim(),
            cardId = UUID.fromString(cardId.trim()),
            color = color?.trim()
        )
    }
}
