package com.example.application.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Card(
    val id: String = UUID.randomUUID().toString(),
    val color: Colors,
    val especial: SpecialType,
    val name: String,
    val png: String,
)
