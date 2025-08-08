# Arquitetura UNO Game API

## Visão Geral da Arquitetura

A aplicação segue os princípios da **Clean Architecture** (Arquitetura Hexagonal), garantindo:
- Separação clara de responsabilidades
- Independência de frameworks
- Testabilidade
- Manutenibilidade

## Diagrama da Arquitetura

```
┌─────────────────────────────────────────────────────────────┐
│                     EXTERNAL INTERFACES                     │
├─────────────────────────────────────────────────────────────┤
│  HTTP Clients  │  WebSocket  │  Database  │  External APIs  │
└─────────────────┴─────────────┴────────────┴─────────────────┘
         │                │             │              │
         ▼                ▼             ▼              ▼
┌─────────────────────────────────────────────────────────────┐
│                     ADAPTERS LAYER                         │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌─────────────────┐              ┌─────────────────┐      │
│  │   DRIVER         │              │     DRIVEN      │      │
│  │   ADAPTERS       │              │    ADAPTERS     │      │
│  │  (Inbound)       │              │   (Outbound)    │      │
│  │                  │              │                 │      │
│  │ • HTTP Routes    │              │ • Database      │      │
│  │ • Controllers    │              │ • File System   │      │
│  │ • Middlewares    │              │ • External APIs │      │
│  │ • Serialization  │              │ • Messaging     │      │
│  └─────────────────┘              └─────────────────┘      │
│           │                                │                │
└───────────┼────────────────────────────────┼────────────────┘
            │                                │
            ▼                                ▼
┌─────────────────────────────────────────────────────────────┐
│                    APPLICATION LAYER                        │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌─────────────────┐              ┌─────────────────┐      │
│  │     PORTS        │              │    HANDLERS     │      │
│  │   (Interfaces)   │              │  (Use Cases)    │      │
│  │                  │              │                 │      │
│  │ • Inbound Ports  │◄────────────►│ • CreateGame    │      │
│  │ • Outbound Ports │              │ • JoinGame      │      │
│  │ • Command Handler│              │ • ThrowCard     │      │
│  │ • Query Handler  │              │ • BuyCard       │      │
│  └─────────────────┘              └─────────────────┘      │
│                                             │               │
│  ┌─────────────────────────────────────────┼───────────────┤
│  │                COMMANDS & QUERIES       │               │
│  │                                         ▼               │
│  │ • CreateGame     • ThrowCard       ┌─────────────────┐  │
│  │ • JoinGame       • BuyCard         │    DOMAIN       │  │
│  │ • StartGame      • FlagLastCard    │    MODELS       │  │
│  │ • EndGame        • SkipPlayer      │                 │  │
│  │ • QuitGame       • KickPlayer      │ • Game          │  │
│  │                                    │ • Player        │  │
│  │ • GameStatus     • PlayerCards     │ • Card          │  │
│  │                                    │ • Colors        │  │
│  └────────────────────────────────────│ • SpecialType   │  │
│                                       │ • GameStatus    │  │
└───────────────────────────────────────│ • PlayerStatus  │  │
                                        └─────────────────┘  │
                                                             │
┌─────────────────────────────────────────────────────────────┤
│                      DOMAIN LAYER                           │
├─────────────────────────────────────────────────────────────┤
│           Pure Business Logic (No Dependencies)             │
└─────────────────────────────────────────────────────────────┘
```

## Fluxo de Dados

### Comando (Command Flow)
```
HTTP Request → Route → Endpoint → Command → Handler → Domain → Response
```

### Consulta (Query Flow)  
```
HTTP Request → Route → Query Endpoint → DAO → Domain → Response
```

## Camadas Detalhadas

### 1. Domain Layer (Núcleo)
- **Responsabilidade**: Regras de negócio puras
- **Componentes**:
  - Entidades (Game, Player, Card)
  - Value Objects (Colors, SpecialType)
  - Regras de domínio
- **Dependências**: Nenhuma

### 2. Application Layer
- **Responsabilidade**: Coordenação e casos de uso
- **Componentes**:
  - Handlers (manipulam comandos)
  - Commands (representam intenções)
  - Ports (interfaces)
- **Dependências**: Apenas Domain Layer

### 3. Adapters Layer
- **Driver Adapters (Inbound)**:
  - HTTP Endpoints
  - Request/Response DTOs
  - Middlewares
  - Validações
- **Driven Adapters (Outbound)**:
  - Persistência de dados
  - Serviços externos
  - Sistema de arquivos

## Padrões Implementados

### CQRS (Command Query Responsibility Segregation)
```
Commands (Write):           Queries (Read):
├── CreateGame             ├── GameStatus
├── JoinGame              ├── PlayerCards
├── StartGame             └── ...
├── ThrowCard
└── ...
```

### Repository Pattern
```kotlin
interface GameRepository {
    fun save(game: Game)
    fun findById(id: UUID): Game?
    fun update(game: Game)
}
```

### Command Pattern
```kotlin
interface CommandHandler<C, R> {
    fun handle(command: C): R
}
```

## Estrutura de Diretórios

```
src/main/kotlin/com/example/
├── application/                 # Application Layer
│   ├── command/                # Commands (DTOs)
│   ├── handler/                # Use Case Handlers
│   ├── model/                  # Domain Models
│   └── ports/                  # Interfaces
│       ├── inbound/           # Input Ports
│       └── outbound/          # Output Ports
├── driver/                     # Driver Adapters
│   ├── http/                  # HTTP Endpoints
│   └── middlewares/           # Middleware Components
├── driven/                     # Driven Adapters
├── query/                      # Query Side (CQRS)
└── configuration/              # Framework Configuration
```

## Princípios Aplicados

### Dependency Inversion
- Camadas internas não dependem de camadas externas
- Dependências apontam sempre para dentro
- Interfaces definem contratos

### Single Responsibility
- Cada classe tem uma única responsabilidade
- Separação clara entre Commands e Queries
- Handlers específicos para cada caso de uso

### Open/Closed Principle
- Extensível através de novos handlers
- Fechado para modificação das interfaces
- Novos adapters sem alterar o núcleo

## Vantagens da Arquitetura

### Testabilidade
- Domain isolado para testes unitários
- Handlers testáveis com mocks
- Adapters testáveis independentemente

### Manutenibilidade
- Mudanças isoladas por camada
- Acoplamento baixo
- Coesão alta

### Flexibilidade
- Fácil troca de frameworks
- Novos adapters sem impacto
- Evolução independente das camadas

## Exemplos de Implementação

### Command Handler
```kotlin
class CreateGameHandler : CommandHandler<CreateGame, String> {
    override fun handle(command: CreateGame): String {
        // 1. Validar comando
        // 2. Criar entidade de domínio
        // 3. Aplicar regras de negócio
        // 4. Persistir via repositório
        // 5. Retornar resultado
    }
}
```

### HTTP Endpoint
```kotlin
fun Application.createGameRoute(handler: CommandHandler<CreateGame, String>) {
    routing {
        post("/game/createGame") {
            // 1. Receber request
            // 2. Validar dados
            // 3. Converter para command
            // 4. Delegar para handler
            // 5. Retornar response
        }
    }
}
```

### Domain Model
```kotlin
data class Game(
    val id: String,
    val players: MutableList<Player>,
    val gameStatus: GameStatus,
    val gameMode: GameMode
) {
    fun addPlayer(player: Player) {
        // Regras de negócio puras
    }
    
    fun startGame() {
        // Lógica de domínio
    }
}
```

## Futuras Extensões

### Possíveis Melhorias
- Event Sourcing
- Websockets para tempo real
- Cache distribuído
- Métricas e observabilidade
- Autenticação JWT

### Novos Adapters
- Database adapter (PostgreSQL/MongoDB)
- Message broker adapter (RabbitMQ/Kafka)
- Cache adapter (Redis)
- Notification adapter (Email/SMS)