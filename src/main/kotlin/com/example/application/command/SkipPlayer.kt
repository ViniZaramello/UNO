package com.example.application.command

import java.util.UUID

data class SkipPlayer(
    val gameId: UUID,
    val playerName: String,
    val passphrase: String,
    val targetPlayerName: String
): CommandHandler
