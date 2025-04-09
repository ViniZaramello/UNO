package com.example.application.handler


import com.example.application.command.EndGame
import com.example.application.ports.inbound.CommandHandler

class EndGameHandler: CommandHandler<EndGame, Unit> {
    override suspend fun handle(command: EndGame) {

    }
}