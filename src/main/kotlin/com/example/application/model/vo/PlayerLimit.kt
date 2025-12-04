package com.example.application.model.vo

import MyMessages.player_limit

data class PlayerLimit(val playerLimit: Int = 12) {
    init {
        check(playerLimit < 12) { player_limit }
    }
}
