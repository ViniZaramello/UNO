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


1 - Cria game

2 - Entra no jogo,
(adiciona um player no game criado),
(não deve permitir que seja possivel entrar no jogo já iniciado),
(verificar quantidade de jogadores, limite n),
(atribuir o numero do player),
(já gera as 7 cartas)

3 - Inicia o jogo, (altera o status para in_game)

4 - Compra carta, (define quantidades que deve comprar e o usuario)

5 - Lança carta,
(verifica a vez do player),
(verifica se bate com as caracteristicas da ultima carta lançada, cor ou nome),
(caso bata, deve subtrair do baralho),
(caso a carta seja especial, deve impactar o proximo player),
(caso a carta seja de bloqueio, deve pular a vez do proximo), -> if(card.bloqued) { playerturn = Player.number + 2},
(troca o turno para o proximo player),
(verifica se o player está apenas com uma carta e se não foi sinalizado uno, caso não tenha sido senalizado, penalizar com +2),
(faz uma contagem de carta após subtrair a carta lançada, caso ele não tenha mais cartas, encerra o jogo)

6 - UNO! (Ao chegar em uma carta o player deverá enviar um sinal de uno, caso contrario, será penalizado com +4 cartas na troca de turnos)

7 - Encerra o jogo (altera o status do game para Game Over)

Fazer um player Number auto increment no mongo
