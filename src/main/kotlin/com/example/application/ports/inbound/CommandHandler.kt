package com.example.application.ports.inbound

import com.example.application.command.CommandHandler

interface CommandHandler<T : CommandHandler, R : Any> {
    suspend fun handle(command: T): R
}
