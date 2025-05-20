package com.example.application.handler

import com.example.application.command.StartGame
import com.example.application.ports.inbound.CommandHandler

class StartGameHandler() : CommandHandler<StartGame, Unit> {
    override suspend fun handle(command: StartGame) {
        //TODO: Adiciona logica de iniciar o jogo
    }
}