- [Gerenciamento de jogos](#gerenciamento-jogo)
    - [Criar jogo](#criar-jogo)
    - [Entrar em um jogo](#entrar-em-um-jogo)
    - [Iniciar jogo](#iniciar-jogo)
    - [Finalizar jogo](#finalizar-jogo)
    - [Sair do jogo](#sair-de-um-jogo)
    - [Pular a vez de um jogador](#pular-vez-de-um-jogador)
    - [Expulsar jogador](#expulsar-jogador)

- [Ações do jogador](#acoes-do-jogador)
    - [Lançar uma carta](#lancar-uma-carta)
    - [Comprar uma carta](#comprar-uma-carta)
    - [Sinalizar a ultima carta ](#sinalizar-ultima-carta)
    - [Passar turno ](#passar-turno)
- [Consultas](#endpoints-de-consulta)
    - [Consulta informações da partida](#consulta-partida)
    - [Consultar carta do jogador](#consulta-carta-do-jogador)

<div id='gerenciamento-jogo'></div>

## Endpoints de Gerenciamento de Jogos

<div id='criar-jogo'></div>

### [POST] /game/createGame

Cria um novo jogo UNO. O jogador que cria o jogo torna-se automaticamente o proprietário (owner).

- **Informações extra sobre o endpoint**
    - Deve ser usado para criar um novo jogo.
    - O id gerado deverá ser usado para acessar o jogo
    - O criador da sala será setado como unico [owner](/docs/FAQ.md#owner).

**Parâmetros:**

| Campo      | Tipo   | Obrigatório | Descrição               |
|------------|--------|-------------|-------------------------|
| name       | string | Sim         | Nome do jogador criador |
| passphrase | string | Sim         | Senha do jogador        |

### Request

```json
{
  "name": "João",
  "passphrase": "minhasenha123"
}
```

### Response

**201 Created**

```json
{
  "gameId": "123e4567-e89b-12d3-a456-426614174000"
}
```

**400 Bad Request**

---

<div id='entrar-em-um-jogo'></div>

### [POST] /game/joinGame

Adiciona um jogador a um jogo existente.

**Parâmetros:**

| Campo      | Tipo   | Obrigatório | Descrição         |
|------------|--------|-------------|-------------------|
| name       | string | Sim         | Nome do jogador   |
| passphrase | string | Sim         | Senha do jogador  |
| gameId     | string | Sim         | ID do jogo (UUID) |

### Request

```json
{
  "name": "Maria",
  "passphrase": "senha456",
  "gameId": "123e4567-e89b-12d3-a456-426614174000"
}
```

### Response

**204 No content**

**400 Bad Request**

**404 Not Found**

**409 Conflict**

---

<div id='iniciar-jogo'></div>

### [POST] /game/startGame

Inicia um jogo criado.

- **Informações extra sobre o endpoint**
    - Apenas o [owner](docs/FAQ.md#owner) do jogo pode iniciar a partida.
    - A partida só iniciará se tiver pelo menos 2 jogadores na partida
    - O limite da sala atual é de 4 jogadores.

**Parâmetros:**

| Campo      | Tipo   | Obrigatório | Descrição             |
|------------|--------|-------------|-----------------------|
| gameId     | string | Sim         | ID do jogo            |
| playerName | string | Sim         | Nome do proprietário  |
| passphrase | string | Sim         | Senha do proprietário |

### Request

```json
{
  "gameId": "123e4567-e89b-12d3-a456-426614174000",
  "playerName": "João",
  "passphrase": "minhasenha123"
}
```

### Response

**204 No content**

**400 Bad Request**

**404 Not Found**

---

<div id='finalizar-jogo'></div>

### [POST] /game/endGame

Encerra um jogo em andamento.

- **Informações extra sobre o endpoint**
    - Apenas o [owner](docs/FAQ.md#owner) do jogo pode encerrar a partida.
    - É possivel [reiniciar a partida](#iniciar-jogo) quando o jogo estiver encerrado, não há necessidade de recriar a
      sala :)

**Parâmetros:**

| Campo      | Tipo   | Obrigatório | Descrição             |
|------------|--------|-------------|-----------------------|
| gameId     | string | Sim         | ID do jogo            |
| playerName | string | Sim         | Nome do proprietário  |
| passphrase | string | Sim         | Senha do proprietário |

### Request

```json
{
  "gameId": "123e4567-e89b-12d3-a456-426614174000",
  "playerName": "João",
  "passphrase": "minhasenha123"
}
```

### Response

**204 No content**

**400 Bad Request**

**404 Not Found**

---

<div id='sair-de-um-jogo'></div>

### [POST] /game/quitGame

Permite que um jogador saia voluntariamente do jogo.

- **Informações extra sobre o endpoint**
    - O jogador poderá sair da partida em andamento ou quando estiver no aguardando o inicio

**Parâmetros:**

| Campo      | Tipo   | Obrigatório | Descrição        |
|------------|--------|-------------|------------------|
| name       | string | Sim         | Nome do jogador  |
| passphrase | string | Sim         | Senha do jogador |
| gameId     | string | Sim         | ID do jogo       |

### Request

```json
{
  "name": "João",
  "passphrase": "minhasenha123",
  "gameId": "123e4567-e89b-12d3-a456-426614174000"
}
```

### Response

**204 No content**

**400 Bad Request**

**404 Not Found**

---


<div id='pular-vez-de-um-jogador'></div>

### [POST] /player/skipPlayer

Força o pulo da vez de um jogador (efeito de carta especial).

- **Informações extra sobre o endpoint**
    - Apenas o [owner](docs/FAQ.md#owner) do jogo pode pular a vez de um jogador.
    - O jogador alvo deve estar na vez para que o endpoint funcione

**Parâmetros:**

| Campo            | Tipo   | Obrigatório | Descrição             |
|------------------|--------|-------------|-----------------------|
| gameId           | string | Sim         | ID do jogo            |
| playerName       | string | Sim         | Nome do jogador atual |
| passphrase       | string | Sim         | Senha do jogador      |
| targetPlayerName | string | Sim         | Nome do jogador alvo  |

### Request

```json
{
  "gameId": "123e4567-e89b-12d3-a456-426614174000",
  "playerName": "João",
  "passphrase": "minhasenha123",
  "targetPlayerName": "Maria"
}
```

### Response

**204 No content**

**400 Bad Request**

**404 Not Found**

--- 

<div id='expulsar-jogador'></div>

### [POST] /game/kickPlayer

Remove um jogador do jogo. Apenas o proprietário pode expulsar jogadores.

- **Informações extra sobre o endpoint**
    - Apenas o [owner](docs/FAQ.md#owner) do jogo pode expulsar um jogador.

**Parâmetros:**

| Campo            | Tipo   | Obrigatório | Descrição                     |
|------------------|--------|-------------|-------------------------------|
| gameId           | string | Sim         | ID do jogo                    |
| playerName       | string | Sim         | Nome do proprietário          |
| passphrase       | string | Sim         | Senha do proprietário         |
| targetPlayerName | string | Sim         | Nome do jogador a ser expulso |

### Request

```json
{
  "gameId": "123e4567-e89b-12d3-a456-426614174000",
  "playerName": "João",
  "passphrase": "minhasenha123",
  "targetPlayerName": "Maria"
}
```

### Response

**200 OK**

**400 Bad Request**

**404 Not Found**

---

<div id='acoes-do-jogador'></div>

## Endpoints de Ações do Jogador

<div id='lancar-uma-carta'></div>

### [POST] /player/throwCard

Permite que um jogador jogue uma carta.

- **Informações extra sobre o endpoint**
    - Só poderá lançar uma carta caso esteja [no seu turno](docs/FAQ.md#turno)
    - Caso possua uma carta especial de comprar 4 ou de mudar a cor, deverá incluir a propriedade color na request com a
      **[cor de sua escolha](docs/FAQ.md#cores)** para o proximo turno

**Parâmetros:**

| Campo      | Tipo   | Obrigatório | Descrição                                                                                      |
|------------|--------|-------------|------------------------------------------------------------------------------------------------|
| gameId     | string | Sim         | ID do jogo                                                                                     |
| playerName | string | Sim         | Nome do jogador                                                                                |
| passphrase | string | Sim         | Senha do jogador                                                                               |
| cardId     | string | Sim         | ID da carta a ser jogada                                                                       |
| color      | string | Não         | Caso use uma carta especial de troca de cores, é necessario passar esse campo informando a cor |

### Request

```json
{
  "gameId": "123e4567-e89b-12d3-a456-426614174000",
  "playerName": "João",
  "passphrase": "minhasenha123",
  "cardId": "456e7890-e89b-12d3-a456-426614174000",
  "color": "RED"
}
```

### Response

**204 No content**

**400 Bad Request**

**404 Not Found**

**Regras de Negócio:**

---

<div id='comprar-uma-carta'></div>

### [POST] /player/buyCard

Permite que um jogador compre cartas do monte.

- **Informações extra sobre o endpoint**
    - Só poderá comprar carta caso esteja na sua vez.
    - Caso a carta retirada não for compativel, sua vez será pulada automaticamente
    - Caso a carta tirada for compativel, o jogo permitirá você lançar ela ou passar a vez

**Parâmetros:**

| Campo      | Tipo   | Obrigatório | Descrição        |
|------------|--------|-------------|------------------|
| gameId     | string | Sim         | ID do jogo       |
| playerName | string | Sim         | Nome do jogador  |
| passphrase | string | Sim         | Senha do jogador |

**Exemplo de Request:**

```json
{
  "gameId": "123e4567-e89b-12d3-a456-426614174000",
  "playerName": "João",
  "passphrase": "minhasenha123"
}
```

### Response

**200 OK**

```json
{
  "card": {
    "id": "card-789e0123-e89b-12d3-a456-426614174000",
    "number": "5",
    "color": "RED",
    "especial": "NONE",
    "name": "Five Red"
  }
}
```

**400 Bad Request**

**404 Not Found**

---

<div id='sinalizar-ultima-carta'></div>

### [POST] /player/flagLastCard

Sinaliza UNO quando o jogador tem apenas uma carta.

- **Informações extra sobre o endpoint**
    - Caso tenha sobrado apenas uma carta na sua mão após um turno, deverá sinalisar que possui apenas uma carta
    - Terá até a proxima rotação de vez para sinalizar que está com a ultima carta, caso não sinalisar e chegar a sua
      vez, será penalizado
    - Caso sinalize mesmo estando com mais cartas, será penalizado.

**Parâmetros:**

| Campo      | Tipo   | Obrigatório | Descrição        |
|------------|--------|-------------|------------------|
| gameId     | string | Sim         | ID do jogo       |
| playerName | string | Sim         | Nome do jogador  |
| passphrase | string | Sim         | Senha do jogador |

### Request

```json
{
  "gameId": "123e4567-e89b-12d3-a456-426614174000",
  "playerName": "João",
  "passphrase": "minhasenha123"
}
```

### Response

**204 No content**

**400 Bad Request**

**404 Not Found**

---

<div id='passar-turno'></div>

### [POST] /player/passTurn

Sinaliza UNO quando o jogador tem apenas uma carta.

- **Informações extra sobre o endpoint**
    - Caso tenha comprado uma carta com paridade mas quer passar seu turno, utilize esse endpoint

**Parâmetros:**

| Campo      | Tipo   | Obrigatório | Descrição        |
|------------|--------|-------------|------------------|
| gameId     | string | Sim         | ID do jogo       |
| playerName | string | Sim         | Nome do jogador  |
| passphrase | string | Sim         | Senha do jogador |

### Request

```json
{
  "gameId": "123e4567-e89b-12d3-a456-426614174000",
  "playerName": "João",
  "passphrase": "minhasenha123"
}
```

### Response

**204 No content**

**400 Bad Request**

**404 Not Found**

---

<div id='consultas'></div>

## Endpoints de Consulta

<div id='consulta-partida'></div>

### [GET] /query/game/{gameId}/status

Retorna o status atual completo do jogo.

**Parâmetros de URL:**

| Campo  | Tipo   | Descrição  |
|--------|--------|------------|
| gameId | string | ID do jogo |

**Resposta de Sucesso (200):**

```json
{
  "gameId": "123e4567-e89b-12d3-a456-426614174000",
  "playerTurn": "João",
  "totalPlayers": 3,
  "cardsInStackQuantity": 40,
  "cardsInTableQuantity": 43,
  "cardsInHandQuantity": 20,
  "lastCardInTable": {
    "color": "RED",
    "number": "7",
    "especial": "NONE",
    "name": "Seven Red"
  },
  "playerStats": [
    {
      "name": "João",
      "cardCount": 5,
      "status": "PLAYING",
      "isOwner": true,
      "playerNumber": 1
    },
    {
      "name": "Maria",
      "cardCount": 7,
      "status": "PLAYING",
      "isOwner": false,
      "playerNumber": 2
    }
  ],
  "lastCard": {
    "color": "RED",
    "number": "7",
    "especial": "NONE",
    "name": "Seven Red"
  },
  "reverse": false,
  "gameStatus": "PLAYING"
}
```

---

<div id='consulta-carta-do-jogador'></div>

### [GET] /query/player/{gameId}/{playerName}/{passphrase}/cards

Retorna as cartas na mão do jogador especificado.

**Parâmetros de URL:**

| Campo      | Tipo   | Descrição        |
|------------|--------|------------------|
| gameId     | string | ID do jogo       |
| playerName | string | Nome do jogador  |
| passphrase | string | Senha do jogador |

**Resposta de Sucesso (200):**

```json
{
  "playerDetails": {
    "name": "João",
    "number": 1,
    "isYourTurn": true,
    "totalCards": 5,
    "cards": [
      {
        "id": "card-1",
        "number": "3",
        "color": "BLUE",
        "especial": "NONE",
        "name": "Three Blue"
      },
      {
        "id": "card-2",
        "number": "buy-four",
        "color": "BLACK",
        "especial": "BUY_FOUR",
        "name": "Buy Four"
      }
    ],
    "playerStatus": "PLAYING"
  },
  "gameStats": {
    "gameId": "123e4567-e89b-12d3-a456-426614174000",
    "playerTurn": "João",
    "totalPlayers": 3,
    "cardsInStackQuantity": 40,
    "cardsInTableQuantity": 43,
    "cardsInHandQuantity": 20,
    "lastCardInTable": {
      "color": "RED",
      "number": "7",
      "especial": "NONE",
      "name": "Seven Red"
    },
    "playerStats": [
      {
        "name": "João",
        "cardCount": 5,
        "status": "PLAYING",
        "isOwner": true,
        "playerNumber": 1
      },
      {
        "name": "Maria",
        "cardCount": 7,
        "status": "PLAYING",
        "isOwner": false,
        "playerNumber": 2
      }
    ],
    "lastCard": {
      "color": "RED",
      "number": "7",
      "especial": "NONE",
      "name": "Seven Red"
    },
    "reverse": false,
    "gameStatus": "PLAYING"
  }
}
```
