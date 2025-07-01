package com.example.application.command

import java.util.UUID

data class KickPlayer(
    val gameId: UUID,
    val playerName: String,
    val passphrase: String,
    val playerTarget: String
) : CommandHandler
