package com.example.application.command

import java.util.UUID

data class QuitGame(
    val gameId: UUID,
    val playerName: String,
    val passphrase: String
) : CommandHandler
