# API I See

## Descrição
`api-i-see` é uma API REST criada com Spring Boot para gerenciar séries favoritas de usuários em uma plataforma de streaming. Ela permite que os usuários enviem uma lista de séries para serem favoritadas em seu perfil, integrando-se com a API do TMDb (https://www.themoviedb.org/). O projeto utiliza SLF4J para logging e ferramentas como Lombok e Spring DevTools para facilitar o desenvolvimento. O objetivo principal é facilitar a migração rápida de listas de séries favoritas para o TMDb.

### Funcionalidades
- Recebe uma lista de séries e adiciona aos favoritos do usuário, integrando-se com a API do TMDb.
- Realiza operações de busca em série utilizando a API externa.
- Sistema de log centralizado para rastreamento de erros e monitoramento.
- Fácil integração com novas fontes de dados ou serviços externos.

## Requisitos
- Java 21+
- Maven 3.8+
- IDE (recomenda-se IntelliJ IDEA)

## Instalação e Execução
Clone o repositório:
```bash
  git clone https://github.com/seu-usuario/api-i-see.git
```
## Endpoint
Buscar IDs das séries
- URL: /api/buscarSeriesIds
- Método: POST
- Descrição: Envia um array com os nomes das séries no corpo da requisição e recebe um array com os respectivos IDs.
Exemplo de Corpo da Requisição:
```json
[
    "Game of Thrones",
    "Looking For Alaska",
    "Love, Death and Robots"
]
```
Exemplo de Resposta:
```json
[
    1399,
    88640,
    86831
]
```
Adicionar séries aos favoritos
- URL: /api/adicionarFavoritos
- Método: POST
- Descrição:  Envia um array com IDs de séries e adiciona aos favoritos do usuário.
- Exemplo de Corpo da Requisição:
```json
[
    1399,
    88640,
    86831
]
```
