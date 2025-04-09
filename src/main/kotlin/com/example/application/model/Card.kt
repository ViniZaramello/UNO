package com.example.application.model

import java.util.UUID


data class Card(
    val id: UUID = UUID.randomUUID(),
    val color: Colors,
    val especial: SpecialType,
    val name: String,
    val png: String,
)
