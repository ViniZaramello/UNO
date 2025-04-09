package com.example.application.ports.outbound

interface Card {
    fun getRandomCards(quantity: Int): List<Card>
}
