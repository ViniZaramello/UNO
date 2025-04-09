package com.example.application.command

import java.util.UUID

data class StartGame(
    val playerName: String,
    val gameId: UUID
) : CommandHandler