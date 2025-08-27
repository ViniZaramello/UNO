# UNO Card Game API

* [Overview](#overview)
    - [Casos de uso](#use_cases)

* [Arquitetura](#architecture)
* [Técnologias utilizadas](#tecnologias)

* [Build e execução](#build)
* [FAQ](#faq)

<div id="overview"></div>

## Overview

Esta API permite criar e gerenciar partidas do jogo UNO, incluindo criação de jogos, entrada de jogadores, jogabilidade
completa com cartas especiais, e todas as regras tradicionais do UNO.

<div id="use_cases"></div>

## Casos de Uso

- [Criar um jogo](docs/REQUESTS.md#criar-jogo)
- [Entrar em um jogo](docs/REQUESTS.md#entrar-em-um-jogo)
- [Iniciar um jogo](docs/REQUESTS.md#iniciar-jogo)
- [Finalizar um jogo](docs/REQUESTS.md#finalizar-jogo)
- [Sair de um jogo](docs/REQUESTS.md#sair-de-um-jogo)
- [Comprar uma carta](docs/REQUESTS.md#comprar-uma-carta)
- [Pular a vez de um jogador](docs/REQUESTS.md#pular-vez-de-um-jogador)
- [Sinalizar a ultima carta (uno!)](docs/REQUESTS.md#sinalizar-ultima-carta)
- [Lançar uma carta](docs/REQUESTS.md#lancar-uma-carta)
- [Expulsar um jogador](docs/REQUESTS.md#expulsar-jogador)
- [Consulta informações da partida](docs/REQUESTS.md#consulta-partida)
- [Consultar carta do jogador](docs/REQUESTS.md#consulta-carta-do-jogador)
- [Passar turno](docs/REQUESTS.md#passar-turno)

---

<div id="tecnologias"></div>

## Tecnologias Utilizadas

| Tecnologia                        | Descrição                          |
|-----------------------------------|------------------------------------|
| [Kotlin](https://kotlinlang.org/) | Linguagem de programação principal |
| [Ktor](https://ktor.io/)          | Framework web para APIs REST       |
| [Koin](https://insert-koin.io/)   | Injeção de dependência             |
| [Gradle](https://gradle.org/)     | Sistema de build                   |

<div id="build"></div>

## Build e Execução

Para construir ou executar o projeto, use um dos seguintes comandos:

| Comando           | Descrição                                          |
|-------------------|----------------------------------------------------|
| `./gradlew test`  | Executar os testes                                 |
| `./gradlew build` | Construir o projeto                                |
| `./gradlew run`   | Executar o servidor                                |
| `buildFatJar`     | Construir JAR executável com todas as dependências |
| `buildImage`      | Construir imagem Docker                            |
| `runDocker`       | Executar usando imagem Docker local                |

---

<div id="architecture"></div>

## Arquitetura

A aplicação segue os princípios da **Arquitetura Hexagonal**, organizando o código em camadas bem definidas:

### Estrutura de Camadas

```
src/main/kotlin/com/example/
├── application/           # Camada de Aplicação
│   ├── command/          # Comandos (casos de uso)
│   ├── handler/          # Manipuladores de comandos
│   ├── model/            # Modelos de domínio
│   └── ports/            # Portas (interfaces)
│       ├── inbound/      # Portas de entrada
│       └── outbound/     # Portas de saída
├── driver/               # Camada de Adaptadores (Entrada)
│   └── http/            # Endpoints HTTP
├── driven/               # Camada de Adaptadores (Saída)
├── query/                # Consultas (CQS)
└── configuration/        # Configurações
```

### Componentes Principais

- **Application Layer**: Contém a lógica de negócio e casos de uso
- **Driver Adapters**: Adaptadores para entrada (HTTP endpoints)
- **Driven Adapters**: Adaptadores para saída (banco de dados, serviços externos)
- **Query Layer**: Separação de comandos e consultas (CQS pattern)

## Desenvolvimento

### Testes

```bash
# Executar todos os testes
./gradlew test

# Executar testes com relatório detalhado
./gradlew test --info
```

### Estrutura de Testes

```
src/test/kotlin/com/example/
├── ApplicationTest.kt              # Testes da aplicação
├── application/
│   ├── shared/Queries.kt          # Consultas compartilhadas
│   └── factories/Domain.kt        # Factories para testes
└── driver/http/                   # Testes dos endpoints
    ├── createGame/EndpointTest.kt
    ├── startGame/EndpointTest.kt
    └── endGame/EndpointTest.kt
```

<div id="faq"></div>

## FAQ

- [Documentação dos endpoints](docs/REQUESTS.md)
- [FAQ](docs/FAQ.md)
