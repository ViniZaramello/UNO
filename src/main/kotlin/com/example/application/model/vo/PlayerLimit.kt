package com.example.application.model.vo

import MyMessages.player_limit

data class PlayerLimit(val playerLimit: Int = 4) {
    init {
        check(playerLimit < 8) { player_limit }
    }
}
