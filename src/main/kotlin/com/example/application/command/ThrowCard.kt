package com.example.application.command

import java.util.UUID

data class ThrowCard(
    val gameId: UUID,
    val playerName: String,
    val passphrase: String,
    val cardId: UUID,
    val color: String? = null,
) : CommandHandler
