package com.example.application.command

data class EndGame(
    val gameId: String,
    val playerName: String,
    val passphrase: String
) : CommandHandler
