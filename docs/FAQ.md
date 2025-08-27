# FAQ

- [owner](#owner)
- [Como saber se é meu turno para jogar?](#turno)
- [Cores](#cores)

<div id="owner"></div>

## Owner

O **owner** do jogo tem as permissões para: 
- iniciar jogo;
- encerrar jogo;
- expulsar jogador;
- pular vez de jogador;

Caso o owner do jogo saia da sala que ele mesmo criou, o poder será passado para o proximo jogador.

<div id="turno"></div>

## Como saber se é meu turno para jogar?
Cada jogador ao entrar na partida recebe um numero. Ao utilizar a api de consulta de [status da partida](docs/REQUESTS.md#consulta-partida) é possível saber o nome do jogador que está na vez.

<div id="cores"></div>

## Cores
As cores disponiveis são as mesmas do jogo original, dentre elas são:
- **RED**
- **BLUE**
- **YELLOW**
- **GREEN**

Não se preocupe em escrever com maiusculo ou minusculo, o sistema sabe tratar, porém precisa ser escrito corretamente.