# ktor-sample-graphql

This project was created using the [Ktor Project Generator](https://start.ktor.io).

Here are some useful links to get you started:

- [Ktor Documentation](https://ktor.io/docs/home.html)
- [Ktor GitHub page](https://github.com/ktorio/ktor)
- The [Ktor Slack chat](https://app.slack.com/client/T09229ZC6/C0A974TJ9). You'll need
  to [request an invite](https://surveys.jetbrains.com/s3/kotlin-slack-sign-up) to join.

## Features

Here's a list of features included in this project:

| Name                                                                   | Description                                                                        |
| ------------------------------------------------------------------------|------------------------------------------------------------------------------------ |
| [Koin](https://start.ktor.io/p/koin)                                   | Provides dependency injection                                                      |
| [Routing](https://start.ktor.io/p/routing)                             | Provides a structured routing DSL                                                  |
| [CORS](https://start.ktor.io/p/cors)                                   | Enables Cross-Origin Resource Sharing (CORS)                                       |
| [Content Negotiation](https://start.ktor.io/p/content-negotiation)     | Provides automatic content conversion according to Content-Type and Accept headers |
| [kotlinx.serialization](https://start.ktor.io/p/kotlinx-serialization) | Handles JSON serialization using kotlinx.serialization library                     |

## Building & Running

To build or run the project, use one of the following tasks:

| Task                          | Description                                                          |
| -------------------------------|---------------------------------------------------------------------- |
| `./gradlew test`              | Run the tests                                                        |
| `./gradlew build`             | Build everything                                                     |
| `buildFatJar`                 | Build an executable JAR of the server with all dependencies included |
| `buildImage`                  | Build the docker image to use with the fat JAR                       |
| `publishImageToLocalRegistry` | Publish the docker image locally                                     |
| `run`                         | Run the server                                                       |
| `runDocker`                   | Run using the local docker image                                     |

If the server starts successfully, you'll see the following output:

```
2024-12-04 14:32:45.584 [main] INFO  Application - Application started in 0.303 seconds.
2024-12-04 14:32:45.682 [main] INFO  Application - Responding at http://0.0.0.0:8080
```
//game
//create
//start
//endGame_uno
//
//player
//buyCard (quantity: int, playerName)
//endpoints: Comprar+4, Livre, default=1
//
//buySpecificCard (playerName: String, uuidCard: uuid)
//unoNotify
//
//card
//createCard
//removeCard
//updateCard


# UNO!

//game //create //start //endGame_uno // //player //buyCard (quantity: int, playerName) //endpoints: Comprar+4, Livre, default=1 // //buySpecificCard (playerName: String, uuidCard: uuid) //unoNotify // //card //createCard //removeCard //updateCard



## Model

- O que considerar para estar pronto?

  R: Todos os vos/model e os passos completos e acessiveis

- **Card**
  - [ ]  Criar o data class Card

    id: UUID

    color: **Colors**

    especial: **SpecialType**

    name: String

    png: String

  - **Colors**
    - [ ]  Criar enum Colors
    - [ ]  Incluir as cores: vermelho, azul, amarelo, verde e preto
  - **SpecialType**
    - [ ]  Criar enum SpecialType
    - [ ]  Deve incluir no enum: NONE, BLOCK, REVERSE, BUY_TWO, BUY_FOUR, CHANGE_COLOR
  - **CardsInTable**
    - [ ]  Criar VO CardsMovements

- **PlayerLimit**



## Endpoints

- O que considerar para estar pronto?

  R: Todos os endpoints e os passos completos e funcionais

- **Cria game**
  - [ ]  Cria game
  - [ ] 
- **Entra no jogo**
  - [ ]  Entra no jogo - Adiciona um player no game criado
  - [ ]  Não deve permitir que seja possivel entrar em um jogo já iniciado
  - [ ]  verificar quantidade de jogadores, limite, n
  - [ ]  atribuir o numero do player
  - [ ]  já gerar 7 cartas assim que um player sem adicionado no jogo
- **Inicia o jogo**
  - [ ]  Altera o status para in_game
- **Compra carta**
  - [ ]  Define quantidades que deve comprar e o usuario
- **Lança carta**
  - [ ]  Verifica a vez do player
  - [ ]  Verifica se bate com as caracteristicas da ultima carta lançada, cor ou nome
  - [ ]  caso bata, deve subtrair do baralho do jogador
  - [ ]  caso a carta seja especial, deve impactar o proximo player
  - [ ]  caso a carta seja de bloqueio, deve pular a vez do proximo

    Exemplo: if(card.blocked){ playerturn = Player.number +2}

  - [ ]  troca o turno para o proximo player
  - [ ]  verifica se o player está apenas com uma carta e se não foi sinalizado uno, caso não tenha sido sinalizado, penalizar com +2
  - [ ]  faz uma contagem de carta após subtrair a carta lançada, caso não tenha mais cartas, encerra o jogo
- UNO!
  - [ ]  Ao chegar em uma carta o player deverá enviar um sinal de uno, caso contrario, será penalizado com + cartas na troca de turnos
- **Encerra o jogo**
  - [ ]  Altera o status do game para Game Over

Fazer um player Number auto increment no mongo
