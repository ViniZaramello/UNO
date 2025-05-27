package com.example.application.model

import kotlinx.serialization.json.Json

const val DECK = """
[
  {
    "id":"0e091edd-0d4f-43eb-b2ac-28f505e89500",
    "number": "zero",
    "color": "RED",
    "especial": "NONE",
    "name": "zero red"
  },
  {
    "id":"0e091edd-0d4f-43eb-b2ac-28f505e89510",
    "number": "zero",
    "color": "BLUE",
    "especial": "NONE",
    "name": "zero blue"
  },
  {
    "id":"0e091edd-0d4f-43eb-b2ac-28f505e89520",
    "number": "zero",
    "color": "YELLOW",
    "especial": "NONE",
    "name": "zero yellow"
  },
  {
    "id":"0e091edd-0d4f-43eb-b2ac-28f505e89530",
    "number": "zero",
    "color": "GREEN",
    "especial": "NONE",
    "name": "zero green"
  },
  {
    "id":"0e091edd-0d4f-43eb-b2ac-28f505e89501",
    "number": "one",
    "color": "RED",
    "especial": "NONE",
    "name": "one red"
  },
  {
    "id":"0e091edd-0d4f-43eb-b2ac-28f505e89511",
    "number": "one",
    "color": "BLUE",
    "especial": "NONE",
    "name": "one blue"
  },
  {
    "id":"0e091edd-0d4f-43eb-b2ac-28f505e89521",
    "number": "one",
    "color": "YELLOW",
    "especial": "NONE",
    "name": "one yellow"
  },
  {
    "id":"0e091edd-0d4f-43eb-b2ac-28f505e89521",
    "number": "one",
    "color": "GREEN",
    "especial": "NONE",
    "name": "one green"
  },
  {
    "id":"0e091edd-0d4f-43eb-b2ac-28f505e89502",
    "number": "two",
    "color": "RED",
    "especial": "NONE",
    "name": "two red"
  },
  {
    "id":"0e091edd-0d4f-43eb-b2ac-28f505e89512",
    "number": "two",
    "color": "BLUE",
    "especial": "NONE",
    "name": "two blue"
  },
  {
    "id":"0e091edd-0d4f-43eb-b2ac-28f505e89522",
    "number": "two",
    "color": "YELLOW",
    "especial": "NONE",
    "name": "two yellow"
  },
  {
    "id":"0e091edd-0d4f-43eb-b2ac-28f505e89532",
    "number": "two",
    "color": "GREEN",
    "especial": "NONE",
    "name": "two green"
  },
  {
    "id":"0e091edd-0d4f-43eb-b2ac-28f505e89503",
    "number": "three",
    "color": "RED",
    "especial": "NONE",
    "name": "three red"
  },
  {
    "id":"0e091edd-0d4f-43eb-b2ac-28f505e89513",
    "number": "three",
    "color": "BLUE",
    "especial": "NONE",
    "name": "three blue"
  },
  {
    "id":"0e091edd-0d4f-43eb-b2ac-28f505e89523",
    "number": "three",
    "color": "YELLOW",
    "especial": "NONE",
    "name": "three yellow"
  },
  {
    "id":"0e091edd-0d4f-43eb-b2ac-28f505e89533",
    "number": "three",
    "color": "GREEN",
    "especial": "NONE",
    "name": "three green"
  },
  {
    "id":"0e091edd-0d4f-43eb-b2ac-28f505e89504",
    "number": "four",
    "color": "RED",
    "especial": "NONE",
    "name": "four red"
  },
  {
    "id":"0e091edd-0d4f-43eb-b2ac-28f505e89514",
    "number": "four",
    "color": "BLUE",
    "especial": "NONE",
    "name": "four blue"
  },
  {
    "id":"0e091edd-0d4f-43eb-b2ac-28f505e89524",
    "number": "four",
    "color": "YELLOW",
    "especial": "NONE",
    "name": "four yellow"
  },
  {
    "id":"0e091edd-0d4f-43eb-b2ac-28f505e89534",
    "number": "four",
    "color": "GREEN",
    "especial": "NONE",
    "name": "four green"
  },
  {
    "id":"0e091edd-0d4f-43eb-b2ac-28f505e89505",
    "number": "five",
    "color": "RED",
    "especial": "NONE",
    "name": "five red"
  },
  {
    "id":"0e091edd-0d4f-43eb-b2ac-28f505e89515",
    "number": "five",
    "color": "BLUE",
    "especial": "NONE",
    "name": "five blue"
  },
  {
    "id":"0e091edd-0d4f-43eb-b2ac-28f505e89525",
    "number": "five",
    "color": "YELLOW",
    "especial": "NONE",
    "name": "five yellow"
  },
  {
    "id":"0e091edd-0d4f-43eb-b2ac-28f505e89535",
    "number": "five",
    "color": "GREEN",
    "especial": "NONE",
    "name": "five green"
  },
  {
    "id":"0e091edd-0d4f-43eb-b2ac-28f505e89506",
    "number": "six",
    "color": "RED",
    "especial": "NONE",
    "name": "six red"
  },
  {
    "id":"0e091edd-0d4f-43eb-b2ac-28f505e89516",
    "number": "six",
    "color": "BLUE",
    "especial": "NONE",
    "name": "six blue"
  },
  {
    "id":"0e091edd-0d4f-43eb-b2ac-28f505e89526",
    "number": "six",
    "color": "YELLOW",
    "especial": "NONE",
    "name": "six yellow"
  },
  {
    "id":"0e091edd-0d4f-43eb-b2ac-28f505e89536",
    "number": "six",
    "color": "GREEN",
    "especial": "NONE",
    "name": "six green"
  },
  {
    "id":"0e091edd-0d4f-43eb-b2ac-28f505e89507",
    "number": "seven",
    "color": "RED",
    "especial": "NONE",
    "name": "seven red"
  },
  {
    "id":"0e091edd-0d4f-43eb-b2ac-28f505e89517",
    "number": "seven",
    "color": "BLUE",
    "especial": "NONE",
    "name": "seven blue"
  },
  {
    "id":"0e091edd-0d4f-43eb-b2ac-28f505e89527",
    "number": "seven",
    "color": "YELLOW",
    "especial": "NONE",
    "name": "seven yellow"
  },
  {
    "id":"0e091edd-0d4f-43eb-b2ac-28f505e89537",
    "number": "seven",
    "color": "GREEN",
    "especial": "NONE",
    "name": "seven green"
  },
  {
    "id":"0e091edd-0d4f-43eb-b2ac-28f505e89508",
    "number": "eight",
    "color": "RED",
    "especial": "NONE",
    "name": "eight red"
  },
  {
    "id":"0e091edd-0d4f-43eb-b2ac-28f505e89518",
    "number": "eight",
    "color": "BLUE",
    "especial": "NONE",
    "name": "eight blue"
  },
  {
    "id":"0e091edd-0d4f-43eb-b2ac-28f505e89528",
    "number": "eight",
    "color": "YELLOW",
    "especial": "NONE",
    "name": "eight yellow"
  },
  {
    "id":"0e091edd-0d4f-43eb-b2ac-28f505e89538",
    "number": "eight",
    "color": "GREEN",
    "especial": "NONE",
    "name": "eight green"
  },
  {
    "id":"0e091edd-0d4f-43eb-b2ac-28f505e89509",
    "number": "nine",
    "color": "RED",
    "especial": "NONE",
    "name": "nine red"
  },
  {
    "id":"0e091edd-0d4f-43eb-b2ac-28f505e89519",
    "number": "nine",
    "color": "BLUE",
    "especial": "NONE",
    "name": "nine blue"
  },
  {
    "id":"0e091edd-0d4f-43eb-b2ac-28f505e89529",
    "number": "nine",
    "color": "YELLOW",
    "especial": "NONE",
    "name": "nine yellow"
  },
  {
    "id":"0e091edd-0d4f-43eb-b2ac-28f505e89539",
    "number": "nine",
    "color": "GREEN",
    "especial": "NONE",
    "name": "nine green"
  },
  {
    "id":"0e091edd-0d4f-43eb-b2ac-28f505e89000",
    "number": "block",
    "color": "RED",
    "especial": "BLOCK",
    "name": "block red"
  },
  {
    "id":"0e091edd-0d4f-43eb-b2ac-28f505e89010",
    "number": "block",
    "color": "BLUE",
    "especial": "BLOCK",
    "name": "block blue"
  },
  {
    "id":"0e091edd-0d4f-43eb-b2ac-28f505e89020",
    "number": "block",
    "color": "YELLOW",
    "especial": "BLOCK",
    "name": "block yellow"
  },
  {
    "id":"0e091edd-0d4f-43eb-b2ac-28f505e89030",
    "number": "block",
    "color": "GREEN",
    "especial": "BLOCK",
    "name": "block green"
  },
  {
    "id":"0e091edd-0d4f-43eb-b2ac-28f505e89001",
    "number": "plusTwo",
    "color": "RED",
    "especial": "BUY_TWO",
    "name": "buy-two red"
  },
  {
    "id":"0e091edd-0d4f-43eb-b2ac-28f505e89011",
    "number": "plusTwo",
    "color": "BLUE",
    "especial": "BUY_TWO",
    "name": "buy-two blue"
  },
  {
    "id":"0e091edd-0d4f-43eb-b2ac-28f505e89021",
    "number": "plusTwo",
    "color": "YELLOW",
    "especial": "BUY_TWO",
    "name": "buy-two yellow"
  },
  {
    "id":"0e091edd-0d4f-43eb-b2ac-28f505e89031",
    "number": "plusTwo",
    "color": "GREEN",
    "especial": "BUY_TWO",
    "name": "buy-two green"
  },
  {
    "id":"0e091edd-0d4f-43eb-b2ac-28f505e89000",
    "number": "plusFour",
    "color": "BLACK",
    "especial": "BUY_FOUR",
    "name": "buy-four"
  },
  {
    "id":"0e091edd-0d4f-43eb-b2ac-28f505e89001",
    "number": "reverse",
    "color": "RED",
    "especial": "REVERSE",
    "name": "reverse red"
  },
  {
    "id":"0e091edd-0d4f-43eb-b2ac-28f505e89011",
    "number": "reverse",
    "color": "BLUE",
    "especial": "REVERSE",
    "name": "reverse blue"
  },
  {
    "id":"0e091edd-0d4f-43eb-b2ac-28f505e89021",
    "number": "reverse",
    "color": "YELLOW",
    "especial": "REVERSE",
    "name": "reverse yellow"
  },
  {
    "id":"0e091edd-0d4f-43eb-b2ac-28f505e89031",
    "number": "reverse",
    "color": "GREEN",
    "especial": "REVERSE",
    "name": "reverse green"
  },
  {
    "id":"0e091edd-0d4f-43eb-b2ac-28f505e89001",
    "number": "changeColor",
    "color": "BLACK",
    "especial": "CHANGE_COLOR",
    "name": "change-color"
  }
]
"""

val newCardDeck: List<Card> =  Json.decodeFromString(DECK)
