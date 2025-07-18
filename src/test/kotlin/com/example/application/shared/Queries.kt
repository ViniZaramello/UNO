package com.example.application.shared

import com.example.application.model.Game
import com.example.application.model.Games

fun getFirstGame(): Game {
    return Games.games.first()
}