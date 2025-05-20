package com.example.application.command

data class StartGame(
    val playerName: String,
    val gameId: String
) : CommandHandler