# UNO Game API - Documentação Detalhada

## Visão Geral

A API UNO permite criar e gerenciar partidas do jogo de cartas UNO através de endpoints REST. Esta documentação detalha todos os endpoints disponíveis, parâmetros, respostas e regras de negócio.

## Base URL

```
http://localhost:8080
```

## Autenticação

A API utiliza um sistema simples de autenticação baseado em:
- **playerName**: Nome do jogador
- **passphrase**: Senha do jogador

Todos os endpoints que modificam o estado do jogo requerem estes parâmetros.

## Endpoints de Gerenciamento de Jogos

### POST /game/createGame

Cria um novo jogo UNO. O jogador que cria o jogo torna-se automaticamente o proprietário (owner).

**Parâmetros:**
| Campo | Tipo | Obrigatório | Descrição |
|-------|------|-------------|-----------|
| name | string | Sim | Nome do jogador criador |
| passphrase | string | Sim | Senha do jogador |

**Exemplo de Request:**
```json
{
    "name": "João",
    "passphrase": "minhasenha123"
}
```

**Resposta de Sucesso (201):**
```json
{
    "gameId": "123e4567-e89b-12d3-a456-426614174000"
}
```

**Erros Possíveis:**
- `400 Bad Request`: Nome ou senha em branco

---

### POST /game/joinGame

Adiciona um jogador a um jogo existente.

**Parâmetros:**
| Campo | Tipo | Obrigatório | Descrição |
|-------|------|-------------|-----------|
| name | string | Sim | Nome do jogador |
| passphrase | string | Sim | Senha do jogador |
| gameId | string | Sim | ID do jogo (UUID) |

**Exemplo de Request:**
```json
{
    "name": "Maria",
    "passphrase": "senha456",
    "gameId": "123e4567-e89b-12d3-a456-426614174000"
}
```

**Resposta de Sucesso (204):**
Sem conteúdo

**Regras de Negócio:**
- Máximo 4 jogadores por jogo
- Não é possível entrar em jogo já iniciado
- Cada jogador recebe automaticamente 7 cartas ao entrar
- Nome do jogador deve ser único no jogo

**Erros Possíveis:**
- `400 Bad Request`: Parâmetros inválidos
- `404 Not Found`: Jogo não encontrado
- `409 Conflict`: Jogo já iniciado ou lotado

---

### POST /game/startGame

Inicia um jogo criado. Apenas o proprietário pode iniciar o jogo.

**Parâmetros:**
| Campo | Tipo | Obrigatório | Descrição |
|-------|------|-------------|-----------|
| gameId | string | Sim | ID do jogo |
| playerName | string | Sim | Nome do proprietário |
| passphrase | string | Sim | Senha do proprietário |

**Exemplo de Request:**
```json
{
    "gameId": "123e4567-e89b-12d3-a456-426614174000",
    "playerName": "João",
    "passphrase": "minhasenha123"
}
```

**Resposta de Sucesso (204):**
Sem conteúdo

**Regras de Negócio:**
- Mínimo 2 jogadores para iniciar
- Status do jogo muda para `IN_GAME`
- Primeira carta é virada na mesa
- Primeiro jogador é definido aleatoriamente

---

### POST /game/endGame

Encerra um jogo em andamento. Apenas o proprietário pode encerrar.

**Parâmetros:**
| Campo | Tipo | Obrigatório | Descrição |
|-------|------|-------------|-----------|
| gameId | string | Sim | ID do jogo |
| playerName | string | Sim | Nome do proprietário |
| passphrase | string | Sim | Senha do proprietário |

**Resposta de Sucesso (204):**
Sem conteúdo

**Regras de Negócio:**
- Status do jogo muda para `GAME_OVER`
- Todas as cartas retornam ao monte
- Jogadores são desconectados

---

### POST /game/quitGame

Permite que um jogador saia voluntariamente do jogo.

**Parâmetros:**
| Campo | Tipo | Obrigatório | Descrição |
|-------|------|-------------|-----------|
| name | string | Sim | Nome do jogador |
| passphrase | string | Sim | Senha do jogador |
| gameId | string | Sim | ID do jogo |

**Resposta de Sucesso (204):**
Sem conteúdo

**Regras de Negócio:**
- Cartas do jogador retornam ao monte
- Se for o proprietário, outro jogador pode assumir
- Ordem de jogada é reajustada

---

### POST /game/kickPlayer

Remove um jogador do jogo. Apenas o proprietário pode expulsar jogadores.

**Parâmetros:**
| Campo | Tipo | Obrigatório | Descrição |
|-------|------|-------------|-----------|
| gameId | string | Sim | ID do jogo |
| playerName | string | Sim | Nome do proprietário |
| passphrase | string | Sim | Senha do proprietário |
| targetPlayerName | string | Sim | Nome do jogador a ser expulso |

**Resposta de Sucesso (201):**
Informações do jogo atualizado

**Regras de Negócio:**
- Apenas proprietário pode expulsar
- Não é possível se auto-expulsar
- Jogador expulso não pode retornar

## Endpoints de Ações do Jogador

### POST /player/throwCard

Permite que um jogador jogue uma carta.

**Parâmetros:**
| Campo | Tipo | Obrigatório | Descrição |
|-------|------|-------------|-----------|
| gameId | string | Sim | ID do jogo |
| playerName | string | Sim | Nome do jogador |
| passphrase | string | Sim | Senha do jogador |
| cardId | string | Sim | ID da carta a ser jogada |

**Exemplo de Request:**
```json
{
    "gameId": "123e4567-e89b-12d3-a456-426614174000",
    "playerName": "João",
    "passphrase": "minhasenha123",
    "cardId": "card-456e7890-e89b-12d3-a456-426614174000"
}
```

**Resposta de Sucesso (204):**
Sem conteúdo

**Regras de Negócio:**
- Deve ser a vez do jogador
- Carta deve ser compatível (mesma cor ou número)
- Cartas especiais aplicam efeitos imediatos
- Verifica condição de vitória
- Verifica necessidade de sinalizar UNO

**Efeitos de Cartas Especiais:**
- **BLOCK**: Próximo jogador perde a vez
- **REVERSE**: Inverte ordem de jogada
- **BUY_TWO**: Próximo jogador compra 2 cartas
- **BUY_FOUR**: Próximo jogador compra 4 cartas
- **CHANGE_COLOR**: Jogador escolhe nova cor ativa

---

### POST /player/buyCard

Permite que um jogador compre cartas do monte.

**Parâmetros:**
| Campo | Tipo | Obrigatório | Descrição |
|-------|------|-------------|-----------|
| gameId | string | Sim | ID do jogo |
| playerName | string | Sim | Nome do jogador |
| passphrase | string | Sim | Senha do jogador |
| quantity | integer | Não | Quantidade de cartas (padrão: 1) |

**Exemplo de Request:**
```json
{
    "gameId": "123e4567-e89b-12d3-a456-426614174000",
    "playerName": "João",
    "passphrase": "minhasenha123",
    "quantity": 2
}
```

**Resposta de Sucesso (200):**
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

**Regras de Negócio:**
- Quantidade padrão: 1 carta
- Efeitos especiais podem forçar compra múltipla
- Vez passa para próximo jogador após compra

---

### POST /player/flagLastCard

Sinaliza UNO quando o jogador tem apenas uma carta.

**Parâmetros:**
| Campo | Tipo | Obrigatório | Descrição |
|-------|------|-------------|-----------|
| gameId | string | Sim | ID do jogo |
| playerName | string | Sim | Nome do jogador |
| passphrase | string | Sim | Senha do jogador |

**Resposta de Sucesso (204):**
Sem conteúdo

**Regras de Negócio:**
- Obrigatório quando jogador tem 1 carta
- Se não sinalizar, recebe penalização de +2 cartas
- Deve ser chamado antes da próxima rodada

---

### POST /player/skipPlayer

Força o pulo da vez de um jogador (efeito de carta especial).

**Parâmetros:**
| Campo | Tipo | Obrigatório | Descrição |
|-------|------|-------------|-----------|
| gameId | string | Sim | ID do jogo |
| playerName | string | Sim | Nome do jogador atual |
| passphrase | string | Sim | Senha do jogador |

**Resposta de Sucesso (204):**
Sem conteúdo

## Endpoints de Consulta

### GET /query/game/{gameId}/status

Retorna o status atual completo do jogo.

**Parâmetros de URL:**
| Campo | Tipo | Descrição |
|-------|------|-----------|
| gameId | string | ID do jogo |

**Resposta de Sucesso (200):**
```json
{
    "gameId": "123e4567-e89b-12d3-a456-426614174000",
    "gameStatus": "IN_GAME",
    "currentPlayer": "João",
    "totalPlayers": 3,
    "players": [
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
    "direction": "CLOCKWISE",
    "winner": null
}
```

---

### GET /query/player/{gameId}/{playerName}/cards

Retorna as cartas na mão do jogador especificado.

**Parâmetros de URL:**
| Campo | Tipo | Descrição |
|-------|------|-----------|
| gameId | string | ID do jogo |
| playerName | string | Nome do jogador |

**Resposta de Sucesso (200):**
```json
{
    "playerName": "João",
    "gameId": "123e4567-e89b-12d3-a456-426614174000",
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
            "number": "",
            "color": "BLACK",
            "especial": "BUY_FOUR",
            "name": "Wild Draw Four"
        }
    ]
}
```

## Modelos de Dados

### Card
```json
{
    "id": "string (UUID)",
    "number": "string",
    "color": "Colors enum",
    "especial": "SpecialType enum", 
    "name": "string",
    "png": "string (opcional)"
}
```

### Colors Enum
- `RED` - Vermelho
- `BLUE` - Azul
- `YELLOW` - Amarelo  
- `GREEN` - Verde
- `BLACK` - Preto (cartas especiais)

### SpecialType Enum
- `NONE` - Carta comum
- `BLOCK` - Bloquear próximo jogador
- `REVERSE` - Reverter direção
- `BUY_TWO` - Comprar 2 cartas
- `BUY_FOUR` - Comprar 4 cartas
- `CHANGE_COLOR` - Trocar cor

### GameStatus Enum
- `WAITING` - Aguardando jogadores
- `IN_GAME` - Jogo em andamento
- `GAME_OVER` - Jogo finalizado

### PlayerStatus Enum
- `WAITING` - Aguardando início
- `PLAYING` - Jogando
- `WINNER` - Vencedor
- `QUIT` - Saiu do jogo

## Códigos de Erro HTTP

| Código | Descrição | Quando Ocorre |
|--------|-----------|---------------|
| 200 | OK | Consulta bem-sucedida |
| 201 | Created | Recurso criado |
| 204 | No Content | Ação executada com sucesso |
| 400 | Bad Request | Parâmetros inválidos |
| 401 | Unauthorized | Credenciais inválidas |
| 403 | Forbidden | Ação não permitida |
| 404 | Not Found | Recurso não encontrado |
| 409 | Conflict | Estado conflitante |
| 500 | Internal Server Error | Erro interno |

## Exemplos de Fluxo Completo

### Cenário: Jogo Completo Entre 2 Jogadores

#### 1. Criação do Jogo
```bash
curl -X POST http://localhost:8080/game/createGame \
  -H "Content-Type: application/json" \
  -d '{"name": "João", "passphrase": "senha123"}'
```

#### 2. Segundo Jogador Entra
```bash
curl -X POST http://localhost:8080/game/joinGame \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Maria",
    "passphrase": "senha456", 
    "gameId": "game-id-aqui"
  }'
```

#### 3. Iniciar Jogo
```bash
curl -X POST http://localhost:8080/game/startGame \
  -H "Content-Type: application/json" \
  -d '{
    "gameId": "game-id-aqui",
    "playerName": "João",
    "passphrase": "senha123"
  }'
```

#### 4. Consultar Status
```bash
curl http://localhost:8080/query/game/game-id-aqui/status
```

#### 5. Ver Cartas do Jogador
```bash
curl http://localhost:8080/query/player/game-id-aqui/João/cards
```

#### 6. Jogar uma Carta
```bash
curl -X POST http://localhost:8080/player/throwCard \
  -H "Content-Type: application/json" \
  -d '{
    "gameId": "game-id-aqui",
    "playerName": "João", 
    "passphrase": "senha123",
    "cardId": "card-id-aqui"
  }'
```

#### 7. Comprar Carta (se necessário)
```bash
curl -X POST http://localhost:8080/player/buyCard \
  -H "Content-Type: application/json" \
  -d '{
    "gameId": "game-id-aqui",
    "playerName": "Maria",
    "passphrase": "senha456",
    "quantity": 1
  }'
```

#### 8. Sinalizar UNO
```bash
curl -X POST http://localhost:8080/player/flagLastCard \
  -H "Content-Type: application/json" \
  -d '{
    "gameId": "game-id-aqui",
    "playerName": "João",
    "passphrase": "senha123"
  }'
```

## Rate Limiting

Atualmente não há limitação de taxa implementada, mas recomenda-se:
- Máximo 10 requisições por segundo por jogador
- Timeout de 30 segundos para jogadas

## Versionamento

A API atualmente não possui versionamento. Todas as mudanças são retrocompatíveis.

Versão atual: `1.0.0`

## Suporte

Para dúvidas sobre a API:
- Consulte a documentação técnica no README.md
- Analise os testes em `src/test/kotlin/`
- Verifique os exemplos de uso acima