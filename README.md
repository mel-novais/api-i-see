# API I See

## Descrição
`api-i-see` é uma API REST criada com Spring Boot para gerenciar séries de usuários no site The Movie Database (TMDB) (https://www.themoviedb.org/). Ela permite que os usuários enviem uma lista de séries para serem favoritadas ou adicionadas em uma watchlist em seu perfil, integrando-se com a API do TMDB (https://developer.themoviedb.org/docs/getting-started). O projeto utiliza SLF4J para logging e ferramentas como Lombok e Spring DevTools para facilitar o desenvolvimento. O objetivo principal é facilitar a migração rápida de listas de séries favoritas para o TMDB.

### Funcionalidades
- Recebe uma lista de séries e adiciona aos favoritos ou a uma watchlist criada do usuário, integrando-se com a API do TMDB.
- Sistema de log centralizado para rastreamento de erros e monitoramento.

## Requisitos
- Java 21+
- Maven 3.8+
- IDE (recomenda-se IntelliJ IDEA)

## Instalação e Execução
Clone o repositório:
```bash
  git clone https://github.com/seu-usuario/api-i-see.git
```
- Para acessar os dados da sua conta, crie um perfil em https://www.themoviedb.org/ , em seguida, vá até 'Configurações' > 'API' > 'Criar' > 'Developer'. Aceite os termos e preencha as informações, indicando que o projeto é pessoal. Após isso, você terá acesso à sua API Key.

- Obetenha seu session_id usando essa url: https://api.themoviedb.org/3/authentication/token/new?api_key=insira_aqui
na respota copie o request_token.
- Envie uma nova requisição para essa url: https://api.themoviedb.org/3/authentication/session/new?api_key=insira_aqui
e no corpo na request mande esse json
```json
{
  "request_token": "insira_aqui"
}
```
na respota copie o session_id.

- Obtenha seu account_id usando essa url: https://api.themoviedb.org/3/account?api_key=insira_aqui&session_id=insira_aqui
 na respota copie o id.

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
- URL: /api/adicionarFavoritos?accountId=insira_aqui&sessionId=insira_aqui
- Método: POST
- Descrição: Envia um array com IDs de séries e adiciona aos favoritos do usuário.
- Exemplo de Corpo da Requisição:
```json
[
    1399,
    88640,
    86831
]
```

Adicionar séries a uma watchlist
Crie uma watchlist copie o id dela e cole em 'idWatchlist' na url.
- URL: /api/adicionarWatchList?listId=insira_aqui&sessionId=insira_aqui
- Método: POST
- Descrição: Envia um array com IDs de séries e adiciona a watchlist criado pelo usuário.
- Exemplo de Corpo da Requisição:
```json
[
      {
        "media_type": "tv",
        "media_id": 158762
    },
]
```

exemplos de respostas:
```txt
Serie com ID 158762 ja esta na lista.
Serie com ID 158762 adicionada a lista com sucesso.
```

--------------------------------------------------------------------------------------------

## Boas Práticas no Desenvolvimento do Projeto
Neste projeto, eu segui uma série de boas práticas de desenvolvimento de software para garantir que o código seja robusto, fácil de manter e eficiente.

Injeção de Dependências
Eu utilizei construtores para inicializar serviços, permitindo uma melhor gestão das dependências e promovendo a testabilidade do código.

Tratamento de Exceções
Implementei captura e log de exceções para lidar com possíveis erros na comunicação com a API externa, garantindo que o sistema possa tratar falhas de forma adequada e fornecer feedback útil aos usuários.

Uso de Constantes
Defini constantes para URLs e mensagens, o que facilita a manutenção do código e previne a repetição de strings ao longo do projeto.

Separação de Responsabilidades
Segmentei o projeto em camadas bem definidas entre o serviço (WatchlistService) e o controlador (WatchlistController). Cada camada tem responsabilidades claras e definidas, seguindo o princípio de responsabilidade única.

Boas Práticas de Log
Adotei o uso de logs para monitorar operações importantes e capturar possíveis erros, ajudando na depuração e monitoramento do sistema.

Manipulação de JSON
Utilizei o ObjectMapper para ler e interpretar respostas JSON da API, garantindo que o código lida corretamente com os dados recebidos.

Uso de Streams e Lambdas
Utilizei Streams para simplificar o processamento de coleções, como na extração de IDs de séries no controlador, tornando o código mais conciso e legível.

Documentação
Adicionei comentários detalhados no código para explicar a funcionalidade de cada método e a lógica aplicada, facilitando a compreensão e manutenção por outros desenvolvedores.
