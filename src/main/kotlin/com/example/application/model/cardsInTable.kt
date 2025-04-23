package com.example.application.model

import kotlinx.serialization.json.Json

const val DECK = """
[
  {
    "id":"0e091edd-0d4f-43eb-b2ac-28f505e89500",
    "color": "RED",
    "especial": "NONE",
    "name": "zero red",
    "png": "https://www.google.com.br"
  },
  {
    "id":"0e091edd-0d4f-43eb-b2ac-28f505e89510",
    "color": "BLUE",
    "especial": "NONE",
    "name": "zero blue",
    "png": "https://www.google.com.br"
  },
  {
    "id":"0e091edd-0d4f-43eb-b2ac-28f505e89520",
    "color": "YELLOW",
    "especial": "NONE",
    "name": "zero yellow",
    "png": "https://www.google.com.br"
  },
  {
    "id":"0e091edd-0d4f-43eb-b2ac-28f505e89530",
    "color": "GREEN",
    "especial": "NONE",
    "name": "zero green",
    "png": "https://www.google.com.br"
  },
  {
    "id":"0e091edd-0d4f-43eb-b2ac-28f505e89501",
    "color": "RED",
    "especial": "NONE",
    "name": "one red",
    "png": "https://www.google.com.br"
  },
  {
    "id":"0e091edd-0d4f-43eb-b2ac-28f505e89511",
    "color": "BLUE",
    "especial": "NONE",
    "name": "one blue",
    "png": "https://www.google.com.br"
  },
  {
    "id":"0e091edd-0d4f-43eb-b2ac-28f505e89521",
    "color": "YELLOW",
    "especial": "NONE",
    "name": "one yellow",
    "png": "https://www.google.com.br"
  },
  {
    "id":"0e091edd-0d4f-43eb-b2ac-28f505e89521",
    "color": "GREEN",
    "especial": "NONE",
    "name": "one green",
    "png": "https://www.google.com.br"
  },
  {
    "id":"0e091edd-0d4f-43eb-b2ac-28f505e89502",
    "color": "RED",
    "especial": "NONE",
    "name": "two red",
    "png": "https://www.google.com.br"
  },
  {
    "id":"0e091edd-0d4f-43eb-b2ac-28f505e89512",
    "color": "BLUE",
    "especial": "NONE",
    "name": "two blue",
    "png": "https://www.google.com.br"
  },
  {
    "id":"0e091edd-0d4f-43eb-b2ac-28f505e89522",
    "color": "YELLOW",
    "especial": "NONE",
    "name": "two yellow",
    "png": "https://www.google.com.br"
  },
  {
    "id":"0e091edd-0d4f-43eb-b2ac-28f505e89532",
    "color": "GREEN",
    "especial": "NONE",
    "name": "two green",
    "png": "https://www.google.com.br"
  },
  {
    "id":"0e091edd-0d4f-43eb-b2ac-28f505e89503",
    "color": "RED",
    "especial": "NONE",
    "name": "three red",
    "png": "https://www.google.com.br"
  },
  {
    "id":"0e091edd-0d4f-43eb-b2ac-28f505e89513",
    "color": "BLUE",
    "especial": "NONE",
    "name": "three blue",
    "png": "https://www.google.com.br"
  },
  {
    "id":"0e091edd-0d4f-43eb-b2ac-28f505e89523",
    "color": "YELLOW",
    "especial": "NONE",
    "name": "three yellow",
    "png": "https://www.google.com.br"
  },
  {
    "id":"0e091edd-0d4f-43eb-b2ac-28f505e89533",
    "color": "GREEN",
    "especial": "NONE",
    "name": "three green",
    "png": "https://www.google.com.br"
  },
  {
    "id":"0e091edd-0d4f-43eb-b2ac-28f505e89504",
    "color": "RED",
    "especial": "NONE",
    "name": "four red",
    "png": "https://www.google.com.br"
  },
  {
    "id":"0e091edd-0d4f-43eb-b2ac-28f505e89514",
    "color": "BLUE",
    "especial": "NONE",
    "name": "four blue",
    "png": "https://www.google.com.br"
  },
  {
    "id":"0e091edd-0d4f-43eb-b2ac-28f505e89524",
    "color": "YELLOW",
    "especial": "NONE",
    "name": "four yellow",
    "png": "https://www.google.com.br"
  },
  {
    "id":"0e091edd-0d4f-43eb-b2ac-28f505e89534",
    "color": "GREEN",
    "especial": "NONE",
    "name": "four green",
    "png": "https://www.google.com.br"
  },
  {
    "id":"0e091edd-0d4f-43eb-b2ac-28f505e89505",
    "color": "RED",
    "especial": "NONE",
    "name": "five red",
    "png": "https://www.google.com.br"
  },
  {
    "id":"0e091edd-0d4f-43eb-b2ac-28f505e89515",
    "color": "BLUE",
    "especial": "NONE",
    "name": "five blue",
    "png": "https://www.google.com.br"
  },
  {
    "id":"0e091edd-0d4f-43eb-b2ac-28f505e89525",
    "color": "YELLOW",
    "especial": "NONE",
    "name": "five yellow",
    "png": "https://www.google.com.br"
  },
  {
    "id":"0e091edd-0d4f-43eb-b2ac-28f505e89535",
    "color": "GREEN",
    "especial": "NONE",
    "name": "five green",
    "png": "https://www.google.com.br"
  },
  {
    "id":"0e091edd-0d4f-43eb-b2ac-28f505e89506",
    "color": "RED",
    "especial": "NONE",
    "name": "six red",
    "png": "https://www.google.com.br"
  },
  {
    "id":"0e091edd-0d4f-43eb-b2ac-28f505e89516",
    "color": "BLUE",
    "especial": "NONE",
    "name": "six blue",
    "png": "https://www.google.com.br"
  },
  {
    "id":"0e091edd-0d4f-43eb-b2ac-28f505e89526",
    "color": "YELLOW",
    "especial": "NONE",
    "name": "six yellow",
    "png": "https://www.google.com.br"
  },
  {
    "id":"0e091edd-0d4f-43eb-b2ac-28f505e89536",
    "color": "GREEN",
    "especial": "NONE",
    "name": "six green",
    "png": "https://www.google.com.br"
  },
  {
    "id":"0e091edd-0d4f-43eb-b2ac-28f505e89507",
    "color": "RED",
    "especial": "NONE",
    "name": "seven red",
    "png": "https://www.google.com.br"
  },
  {
    "id":"0e091edd-0d4f-43eb-b2ac-28f505e89517",
    "color": "BLUE",
    "especial": "NONE",
    "name": "seven blue",
    "png": "https://www.google.com.br"
  },
  {
    "id":"0e091edd-0d4f-43eb-b2ac-28f505e89527",
    "color": "YELLOW",
    "especial": "NONE",
    "name": "seven yellow",
    "png": "https://www.google.com.br"
  },
  {
    "id":"0e091edd-0d4f-43eb-b2ac-28f505e89537",
    "color": "GREEN",
    "especial": "NONE",
    "name": "seven green",
    "png": "https://www.google.com.br"
  },
  {
    "id":"0e091edd-0d4f-43eb-b2ac-28f505e89508",
    "color": "RED",
    "especial": "NONE",
    "name": "eight red",
    "png": "https://www.google.com.br"
  },
  {
    "id":"0e091edd-0d4f-43eb-b2ac-28f505e89518",
    "color": "BLUE",
    "especial": "NONE",
    "name": "eight blue",
    "png": "https://www.google.com.br"
  },
  {
    "id":"0e091edd-0d4f-43eb-b2ac-28f505e89528",
    "color": "YELLOW",
    "especial": "NONE",
    "name": "eight yellow",
    "png": "https://www.google.com.br"
  },
  {
    "id":"0e091edd-0d4f-43eb-b2ac-28f505e89538",
    "color": "GREEN",
    "especial": "NONE",
    "name": "eight green",
    "png": "https://www.google.com.br"
  },
  {
    "id":"0e091edd-0d4f-43eb-b2ac-28f505e89509",
    "color": "RED",
    "especial": "NONE",
    "name": "nine red",
    "png": "https://www.google.com.br"
  },
  {
    "id":"0e091edd-0d4f-43eb-b2ac-28f505e89519",
    "color": "BLUE",
    "especial": "NONE",
    "name": "nine blue",
    "png": "https://www.google.com.br"
  },
  {
    "id":"0e091edd-0d4f-43eb-b2ac-28f505e89529",
    "color": "YELLOW",
    "especial": "NONE",
    "name": "nine yellow",
    "png": "https://www.google.com.br"
  },
  {
    "id":"0e091edd-0d4f-43eb-b2ac-28f505e89539",
    "color": "GREEN",
    "especial": "NONE",
    "name": "nine green",
    "png": "https://www.google.com.br"
  },
  {
    "id":"0e091edd-0d4f-43eb-b2ac-28f505e89000",
    "color": "RED",
    "especial": "BLOCK",
    "name": "block red",
    "png": "https://www.google.com.br"
  },
  {
    "id":"0e091edd-0d4f-43eb-b2ac-28f505e89010",
    "color": "BLUE",
    "especial": "BLOCK",
    "name": "block blue",
    "png": "https://www.google.com.br"
  },
  {
    "id":"0e091edd-0d4f-43eb-b2ac-28f505e89020",
    "color": "YELLOW",
    "especial": "BLOCK",
    "name": "block yellow",
    "png": "https://www.google.com.br"
  },
  {
    "id":"0e091edd-0d4f-43eb-b2ac-28f505e89030",
    "color": "GREEN",
    "especial": "BLOCK",
    "name": "block green",
    "png": "https://www.google.com.br"
  },
  {
    "id":"0e091edd-0d4f-43eb-b2ac-28f505e89001",
    "color": "RED",
    "especial": "BUY_TWO",
    "name": "buy-two red",
    "png": "https://www.google.com.br"
  },
  {
    "id":"0e091edd-0d4f-43eb-b2ac-28f505e89011",
    "color": "BLUE",
    "especial": "BUY_TWO",
    "name": "buy-two blue",
    "png": "https://www.google.com.br"
  },
  {
    "id":"0e091edd-0d4f-43eb-b2ac-28f505e89021",
    "color": "YELLOW",
    "especial": "BUY_TWO",
    "name": "buy-two yellow",
    "png": "https://www.google.com.br"
  },
  {
    "id":"0e091edd-0d4f-43eb-b2ac-28f505e89031",
    "color": "GREEN",
    "especial": "BUY_TWO",
    "name": "buy-two green",
    "png": "https://www.google.com.br"
  },
  {
    "id":"0e091edd-0d4f-43eb-b2ac-28f505e89000",
    "color": "BLACK",
    "especial": "BUY_FOUR",
    "name": "buy-four",
    "png": "https://www.google.com.br"
  },
  {
    "id":"0e091edd-0d4f-43eb-b2ac-28f505e89001",
    "color": "RED",
    "especial": "REVERSE",
    "name": "reverse red",
    "png": "https://www.google.com.br"
  },
  {
    "id":"0e091edd-0d4f-43eb-b2ac-28f505e89011",
    "color": "BLUE",
    "especial": "REVERSE",
    "name": "reverse blue",
    "png": "https://www.google.com.br"
  },
  {
    "id":"0e091edd-0d4f-43eb-b2ac-28f505e89021",
    "color": "YELLOW",
    "especial": "REVERSE",
    "name": "reverse yellow",
    "png": "https://www.google.com.br"
  },
  {
    "id":"0e091edd-0d4f-43eb-b2ac-28f505e89031",
    "color": "GREEN",
    "especial": "REVERSE",
    "name": "reverse green",
    "png": "https://www.google.com.br"
  },
  {
    "id":"0e091edd-0d4f-43eb-b2ac-28f505e89001",
    "color": "BLACK",
    "especial": "CHANGE_COLOR",
    "name": "change-color",
    "png": "https://www.google.com.br"
  }
]
"""

val newCardDeck: List<Card> =  Json.decodeFromString(DECK) //criar uma classe para cada um dessas variaveis
