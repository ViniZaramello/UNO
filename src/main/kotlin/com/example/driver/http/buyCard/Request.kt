package com.example.driver.http.buyCard

import com.example.application.command.BuyCard
import java.util.UUID
import kotlinx.serialization.Serializable

@Serializable
data class Request(
    val gameId: String,
    val playerName: String,
    val passphrase: String,
) {
    fun toCommand(): BuyCard {
        return BuyCard(
            gameId = UUID.fromString(gameId.trim()),
            playerName = playerName.trim(),
            passphrase = passphrase.trim(),
        )
    }
}
