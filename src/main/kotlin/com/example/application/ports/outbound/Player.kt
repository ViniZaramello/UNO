package com.example.application.ports.outbound

import com.example.application.model.Player

interface Player {
    fun create(player: Player): Player
}