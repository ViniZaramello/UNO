package com.example.application.ports.outbound

import com.example.application.model.Game
import java.util.UUID

interface GameRepository {
    fun save(game: Game): Game
    fun findById(id: UUID): Game
    fun delete(game: Game)
}

