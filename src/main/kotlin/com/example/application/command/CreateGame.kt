package com.example.application.command

import com.example.application.model.Player

data class CreateGame(val player: Player) : CommandHandler
