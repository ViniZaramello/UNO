# UNO Card Game API

Uma API REST para o jogo de cartas UNO, desenvolvida em Kotlin com Ktor framework, seguindo princípios de Clean Architecture.

## Sobre o Projeto

Esta API permite criar e gerenciar partidas do jogo UNO, incluindo criação de jogos, entrada de jogadores, jogabilidade completa com cartas especiais, e todas as regras tradicionais do UNO.

## Tecnologias Utilizadas

| Tecnologia | Descrição |
|------------|-----------|
| [Kotlin](https://kotlinlang.org/) | Linguagem de programação principal |
| [Ktor](https://ktor.io/) | Framework web para APIs REST |
| [kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization) | Serialização JSON |
| [Koin](https://insert-koin.io/) | Injeção de dependência |
| [Gradle](https://gradle.org/) | Sistema de build |

## Recursos Técnicos

| Recurso | Descrição |
|---------|-----------|
| [Routing](https://start.ktor.io/p/routing) | Sistema de roteamento estruturado |
| [CORS](https://start.ktor.io/p/cors) | Compartilhamento de recursos entre origens |
| [Content Negotiation](https://start.ktor.io/p/content-negotiation) | Conversão automática de conteúdo baseada em headers |
| [kotlinx.serialization](https://start.ktor.io/p/kotlinx-serialization) | Serialização JSON usando kotlinx.serialization |

## Build e Execução

Para construir ou executar o projeto, use um dos seguintes comandos:

| Comando | Descrição |
|---------|-----------|
| `./gradlew test` | Executar os testes |
| `./gradlew build` | Construir o projeto |
| `./gradlew run` | Executar o servidor |
| `buildFatJar` | Construir JAR executável com todas as dependências |
| `buildImage` | Construir imagem Docker |
| `runDocker` | Executar usando imagem Docker local |

Se o servidor iniciar com sucesso, você verá a seguinte saída:

```
2024-12-04 14:32:45.584 [main] INFO  Application - Application started in 0.303 seconds.
2024-12-04 14:32:45.682 [main] INFO  Application - Responding at http://0.0.0.0:8080
```
## Arquitetura

A aplicação segue os princípios da **Clean Architecture** (Arquitetura Hexagonal), organizando o código em camadas bem definidas:

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
├── query/                # Consultas (CQRS)
└── configuration/        # Configurações
```

### Componentes Principais

- **Application Layer**: Contém a lógica de negócio e casos de uso
- **Driver Adapters**: Adaptadores para entrada (HTTP endpoints)
- **Driven Adapters**: Adaptadores para saída (banco de dados, serviços externos)
- **Query Layer**: Separação de comandos e consultas (CQRS pattern)

## Modelo de Dados

### Card (Carta)
```kotlin
data class Card(
    val id: String = UUID.randomUUID().toString(),
    val number: String,
    var color: Colors,
    val especial: SpecialType,
    val name: String,
    val png: String? = null
)
```

### Colors (Cores)
```kotlin
enum class Colors {
    RED,     // Vermelho
    BLUE,    // Azul  
    YELLOW,  // Amarelo
    GREEN,   // Verde
    BLACK    // Preto (cartas especiais)
}
```

### SpecialType (Tipo Especial)
```kotlin
enum class SpecialType {
    NONE,         // Carta comum
    BLOCK,        // Bloqueio
    REVERSE,      // Reverso
    BUY_TWO,      // Comprar +2
    BUY_FOUR,     // Comprar +4
    CHANGE_COLOR  // Trocar cor
}
```

### Player (Jogador)
```kotlin
data class Player(
    val name: String,
    val passphrase: String,
    val owner: Boolean = false,
    val playerNumber: Int? = null,
    val playerStatus: PlayerStatus = PlayerStatus.WAITING
)
```

### Game (Jogo)
```kotlin
data class Game(
    val id: String = UUID.randomUUID().toString(),
    val players: MutableList<Player>,
    val gameStatus: GameStatus,
    val gameMode: GameMode,
    // ... outros campos
)
```

## API Endpoints

### Gerenciamento de Jogos

#### POST /game/createGame
Cria um novo jogo UNO.

**Request:**
```json
{
    "name": "string",
    "passphrase": "string"
}
```

**Response:** `201 Created`
```json
{
    "gameId": "uuid"
}
```

**Validações:**
- Nome do jogador não pode estar em branco
- Senha não pode estar em branco

---

#### POST /game/joinGame  
Adiciona um jogador a um jogo existente.

**Request:**
```json
{
    "name": "string",
    "passphrase": "string", 
    "gameId": "uuid"
}
```

**Response:** `204 No Content`

**Validações:**
- Nome do jogador não pode estar em branco
- Senha não pode estar em branco
- ID do jogo deve ser válido
- Jogo não pode estar em andamento

---

#### POST /game/startGame
Inicia um jogo criado.

**Request:**
```json
{
    "gameId": "uuid",
    "playerName": "string",
    "passphrase": "string"
}
```

**Response:** `204 No Content`

**Regras:**
- Altera o status do jogo para `IN_GAME`
- Cada jogador recebe 7 cartas iniciais
- Define o primeiro jogador

---

#### POST /game/endGame
Encerra um jogo em andamento.

**Request:**
```json
{
    "gameId": "uuid",
    "playerName": "string", 
    "passphrase": "string"
}
```

**Response:** `204 No Content`

**Regras:**
- Altera o status do jogo para `GAME_OVER`

---

#### POST /game/quitGame
Jogador sai de um jogo.

**Request:**
```json
{
    "name": "string",
    "passphrase": "string",
    "gameId": "uuid"
}
```

**Response:** `204 No Content`

**Regras:**
- Remove jogador do jogo
- Se for o criador, jogo pode ser encerrado
- Cartas do jogador retornam ao monte

---

#### POST /game/kickPlayer
Remove um jogador do jogo (apenas para criador).

**Request:**
```json
{
    "gameId": "uuid",
    "playerName": "string",
    "passphrase": "string", 
    "targetPlayerName": "string"
}
```

**Response:** `201 Created`

**Regras:**
- Apenas criador do jogo pode expulsar jogadores
- Jogador expulso não pode retornar ao jogo
- Cartas do jogador retornam ao monte

### Ações do Jogador

#### POST /player/throwCard
Jogador lança uma carta na mesa.

**Request:**
```json
{
    "gameId": "uuid",
    "playerName": "string",
    "passphrase": "string",
    "cardId": "uuid"
}
```

**Response:** `204 No Content`

**Regras:**
- Verifica se é a vez do jogador
- Verifica se a carta é compatível (cor ou número)
- Remove carta da mão do jogador
- Aplica efeitos de cartas especiais
- Passa a vez para o próximo jogador
- Verifica condição de vitória

---

#### POST /player/buyCard
Jogador compra cartas do monte.

**Request:**
```json
{
    "gameId": "uuid",
    "playerName": "string",
    "passphrase": "string",
    "quantity": 1
}
```

**Response:** `200 OK`
```json
{
    "card": {
        "id": "uuid",
        "number": "string",
        "color": "enum",
        "especial": "enum", 
        "name": "string"
    }
}
```

**Regras:**
- Quantidade padrão: 1 carta
- Cartas especiais podem forçar compra de 2 ou 4 cartas

---

#### POST /player/flagLastCard
Jogador sinaliza UNO (última carta).

**Request:**
```json
{
    "gameId": "uuid",
    "playerName": "string",
    "passphrase": "string"
}
```

**Response:** `204 No Content`

**Regras:**
- Deve ser chamado quando jogador tem apenas 1 carta
- Se não sinalizar, será penalizado com +2 cartas

---

#### POST /player/skipPlayer
Pula a vez de um jogador (efeito de carta especial).

**Request:**
```json
{
    "gameId": "uuid",
    "playerName": "string",
    "passphrase": "string"
}
```

**Response:** `204 No Content`

### Consultas

#### GET /query/game/{gameId}/status
Retorna o status atual do jogo.

**Response:** `200 OK`
```json
{
    "gameId": "uuid",
    "gameStatus": "enum",
    "currentPlayer": "string",
    "players": [
        {
            "name": "string",
            "cardCount": 7,
            "status": "enum"
        }
    ],
    "lastCard": {
        "color": "enum",
        "number": "string",
        "especial": "enum"
    }
}
```

---

#### GET /query/player/{gameId}/{playerName}/cards
Retorna as cartas da mão do jogador.

**Response:** `200 OK` 
```json
{
    "cards": [
        {
            "id": "uuid",
            "number": "string", 
            "color": "enum",
            "especial": "enum",
            "name": "string"
        }
    ]
}
```

## Casos de Uso

### 1. Criar e Iniciar Jogo
1. **Criar jogo**: Jogador cria um jogo fornecendo nome e senha
2. **Outros jogadores entram**: Até 4 jogadores podem entrar no jogo
3. **Iniciar jogo**: Criador inicia o jogo
4. **Distribuir cartas**: Cada jogador recebe 7 cartas
5. **Primeira carta**: Uma carta é virada na mesa

### 2. Jogabilidade
1. **Verificar vez**: Sistema verifica de quem é a vez
2. **Jogador joga carta**: Carta deve ser compatível em cor ou número
3. **Aplicar efeitos**: Cartas especiais aplicam efeitos
4. **Próximo jogador**: Sistema passa a vez
5. **Verificar UNO**: Se jogador tem 1 carta, deve sinalizar
6. **Verificar vitória**: Se jogador não tem cartas, vence

### 3. Cartas Especiais
- **BLOCK**: Próximo jogador perde a vez
- **REVERSE**: Inverte ordem de jogada  
- **BUY_TWO**: Próximo jogador compra 2 cartas
- **BUY_FOUR**: Próximo jogador compra 4 cartas
- **CHANGE_COLOR**: Jogador escolhe nova cor

### 4. Regras UNO
- **Sinalizar UNO**: Obrigatório quando tem 1 carta
- **Penalização**: +2 cartas se não sinalizar
- **Vitória**: Primeiro a ficar sem cartas
- **Compra obrigatória**: Se não pode jogar

## Status e Códigos de Erro

### Status do Jogo
- `WAITING`: Aguardando jogadores
- `IN_GAME`: Jogo em andamento  
- `GAME_OVER`: Jogo finalizado

### Status do Jogador  
- `WAITING`: Aguardando início
- `PLAYING`: Jogando
- `WINNER`: Vencedor
- `QUIT`: Saiu do jogo

### Códigos HTTP
- `200 OK`: Sucesso com retorno de dados
- `201 Created`: Recurso criado com sucesso
- `204 No Content`: Sucesso sem retorno de dados
- `400 Bad Request`: Erro de validação
- `404 Not Found`: Recurso não encontrado
- `500 Internal Server Error`: Erro interno

## Desenvolvimento

### Testes
O projeto inclui testes automatizados para os endpoints e lógica de negócio:

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

### Validações de Request
Todos os endpoints incluem validações de entrada:
- Campos obrigatórios não podem estar em branco
- IDs devem ser UUIDs válidos
- Senhas são verificadas para autenticação
- Estados do jogo são validados antes de ações

### Exemplo de Uso da API

#### 1. Criar e Iniciar um Jogo
```bash
# 1. Criar jogo
curl -X POST http://localhost:8080/game/createGame \
  -H "Content-Type: application/json" \
  -d '{
    "name": "João",
    "passphrase": "minhasenha"
  }'

# Response: {"gameId": "123e4567-e89b-12d3-a456-426614174000"}

# 2. Outro jogador entra
curl -X POST http://localhost:8080/game/joinGame \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Maria", 
    "passphrase": "outrasenha",
    "gameId": "123e4567-e89b-12d3-a456-426614174000"
  }'

# 3. Iniciar jogo
curl -X POST http://localhost:8080/game/startGame \
  -H "Content-Type: application/json" \
  -d '{
    "gameId": "123e4567-e89b-12d3-a456-426614174000",
    "playerName": "João",
    "passphrase": "minhasenha"
  }'
```

#### 2. Jogabilidade
```bash
# Consultar cartas da mão
curl http://localhost:8080/query/player/123e4567-e89b-12d3-a456-426614174000/João/cards

# Jogar uma carta
curl -X POST http://localhost:8080/player/throwCard \
  -H "Content-Type: application/json" \
  -d '{
    "gameId": "123e4567-e89b-12d3-a456-426614174000",
    "playerName": "João",
    "passphrase": "minhasenha", 
    "cardId": "card-uuid-here"
  }'

# Comprar carta
curl -X POST http://localhost:8080/player/buyCard \
  -H "Content-Type: application/json" \
  -d '{
    "gameId": "123e4567-e89b-12d3-a456-426614174000",
    "playerName": "João",
    "passphrase": "minhasenha",
    "quantity": 1
  }'
```

## Links Úteis

- [Documentação Detalhada da API](docs/API.md) - Documentação completa de todos os endpoints
- [Arquitetura do Sistema](docs/ARCHITECTURE.md) - Detalhes da arquitetura e padrões utilizados
- [Documentação do Ktor](https://ktor.io/docs/home.html)
- [Kotlin Documentation](https://kotlinlang.org/docs/)
- [Clean Architecture](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [CQRS Pattern](https://martinfowler.com/bliki/CQRS.html)

## Contribuição

### Padrões de Código
- Seguir convenções do Kotlin
- Utilizar Clean Architecture
- Separar comandos de consultas (CQRS)
- Testes para toda funcionalidade nova
- Validações em todos os endpoints

### Estrutura de Commits
- feat: nova funcionalidade
- fix: correção de bug
- docs: atualização de documentação
- test: adição ou correção de testes
- refactor: refatoração de código
