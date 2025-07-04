package com.example.application.command

import com.example.application.model.Player
import java.util.UUID

data class JoinPlayerInGame(
    val player: Player,
    val gameId: UUID
): CommandHandler
