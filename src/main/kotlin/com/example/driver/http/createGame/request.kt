package com.example.driver.http.createGame

import com.example.application.model.Player
import kotlinx.serialization.Serializable

@Serializable
data class request(
    val name: String,
    val passphrase: String
) {
    fun toCommand() {
        val player = Player(
            name = name,
            passphrase = passphrase,
            owner = true,
        )
    }
}
