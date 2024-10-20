package com.melnovais.api_i_see.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class WatchlistService {
    // Constantes que armazenam informações importantes para as requisições à API do TMDb
    private static final String API_KEY = "eba6b92a7ac8f2164e0b744b637a5af5";
    private static final String SEARCH_URL = "https://api.themoviedb.org/3/search/tv?api_key={api_key}&query={series_name}";
    private static final String FAVORITE_URL = "https://api.themoviedb.org/3/account/{account_id}/favorite?api_key={api_key}&session_id={session_id}";
    private static final String ADD_TO_LIST_URL = "https://api.themoviedb.org/4/list/{list_id}/items?api_key={api_key}&session_id={session_id}";

    private final RestTemplate restTemplate;  // Responsável por fazer as requisições HTTP
    private final ObjectMapper objectMapper;  // Responsável por manipular dados JSON

    // Logger para registrar eventos importantes no sistema (como sucesso ou falhas)
    private static final Logger logger = LoggerFactory.getLogger(WatchlistService.class);

    // Construtor padrão inicializando o RestTemplate e ObjectMapper
    public WatchlistService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    // Método que busca os IDs das séries pelo nome, utilizando a API do TMDb
    public List<Integer> buscarSeriesIds(List<String> seriesNomes) throws Exception {
        List<Integer> seriesIds = new ArrayList<>();  // Lista para armazenar os IDs das séries

        // Itera sobre cada nome de série fornecido
        for (String nome : seriesNomes) {
            // Faz uma requisição GET para buscar as séries pelo nome
            String response = restTemplate.getForObject(SEARCH_URL, String.class, API_KEY, nome);
            // Lê a resposta JSON e busca o campo "results"
            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode results = rootNode.path("results");

            // Verifica se há resultados para a série
            if (results.isArray() && results.size() > 0) {
                // Adiciona o primeiro ID encontrado à lista
                int mediaId = results.get(0).path("id").asInt();
                seriesIds.add(mediaId);
            } else {
                // Log de advertência caso a série não seja encontrada
                logger.warn("Série não encontrada: {}", nome);
            }
        }

        // Loga os IDs das séries encontradas no formato JSON
        logger.info("IDs das séries no formato JSON: {}", objectMapper.writeValueAsString(seriesIds));
        return seriesIds;  // Retorna a lista de IDs
    }

    // Método para adicionar séries à lista de favoritos da conta
    public ResponseEntity<String> adicionarSeriesFavoritas(List<Integer> seriesIds, String accountId, String sessionId) {
        // Itera sobre cada ID de série
        for (Integer id : seriesIds) {
            // Corpo da requisição com informações da série e do tipo de mídia
            Map<String, Object> body = Map.of(
                    "media_type", "tv",
                    "media_id", id,
                    "favorite", true
            );

            // Configura os headers para a requisição (definindo o tipo de conteúdo como JSON)
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            // Faz a requisição POST para favoritar a série
            ResponseEntity<String> response = restTemplate.postForEntity(FAVORITE_URL, request, String.class, accountId, API_KEY, sessionId);

            // Verifica se a requisição foi bem-sucedida
            if (response.getStatusCode() == HttpStatus.OK) {
                // Loga o sucesso de adicionar a série aos favoritos
                logger.info("Série favoritada com sucesso! ID: {}", id);
            } else {
                // Loga o erro ao tentar favoritar a série
                logger.error("Erro ao favoritar série: {} - {}", id, response.getBody());
            }
        }

        // Retorna uma resposta indicando que todas as séries foram processadas
        return ResponseEntity.ok("Séries processadas com sucesso.");
    }

    // Método para adicionar séries à lista de "Terminadas"
    public ResponseEntity<String> adicionarSeriesTerminadas(List<Integer> seriesIds, String listId, String sessionId) {
        List<Map<String, Object>> items = new ArrayList<>();
        List<String> mensagens = new ArrayList<>();  // Lista para armazenar mensagens sobre o status de cada série

        // Prepara os itens para a requisição
        for (Integer id : seriesIds) {
            Map<String, Object> item = new HashMap<>();
            item.put("media_type", "tv");
            item.put("media_id", id);
            items.add(item);
        }

        // Corpo da requisição
        Map<String, Object> body = new HashMap<>();
        body.put("items", items);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        // Faz a requisição POST para adicionar as séries à lista
        ResponseEntity<String> response = restTemplate.postForEntity(
                ADD_TO_LIST_URL, request, String.class, listId, API_KEY, sessionId
        );

        // Verifica a resposta da API
        if (response.getStatusCode() == HttpStatus.OK) {
            try {
                JsonNode rootNode = objectMapper.readTree(response.getBody());
                JsonNode results = rootNode.path("results");

                // Itera sobre os resultados para verificar o status de cada série
                if (results.isArray()) {
                    for (JsonNode result : results) {
                        boolean success = result.path("success").asBoolean();
                        int mediaId = result.path("media_id").asInt();

                        if (success) {
                            mensagens.add("Série com ID " + mediaId + " adicionada à lista com sucesso.");
                        } else {
                            JsonNode errorMessages = result.path("error");
                            if (errorMessages.isArray() && errorMessages.size() > 0) {
                                String errorMessage = errorMessages.get(0).asText();
                                if ("Media has already been taken".equals(errorMessage)) {
                                    mensagens.add("Série com ID " + mediaId + " já está na lista.");
                                } else {
                                    mensagens.add("Erro ao adicionar série com ID " + mediaId + ": " + errorMessage);
                                }
                            }
                        }
                    }
                }

                // Retorna uma resposta contendo as mensagens sobre o status de cada série
                return ResponseEntity.ok(String.join("\n", mensagens));

            } catch (Exception e) {
                logger.error("Erro ao processar a resposta da API: {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao processar a resposta da API.");
            }
        } else {
            logger.error("Erro ao adicionar séries à lista 'Terminadas': {}", response.getBody());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao adicionar séries à lista 'Terminadas'.");
        }
    }

    // Novo método para verificar se um item está na lista
    public boolean verificarStatusItem(int listId, int mediaId, String mediaType) {
        // URL da API para verificar o status de um item
        String itemStatusUrl = "https://api.themoviedb.org/4/list/{list_id}/item_status?media_id={media_id}&media_type={media_type}";

        // Configurando os headers, com o token de usuário
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("SEU_TOKEN_DE_USUARIO"); // Altere para seu token de acesso de usuário válido
        HttpEntity<String> entity = new HttpEntity<>(headers);

        // Mapeando os parâmetros da URL
        Map<String, Object> params = new HashMap<>();
        params.put("list_id", listId);
        params.put("media_id", mediaId);
        params.put("media_type", mediaType);

        try {
            // Fazendo a requisição GET para verificar o status do item
            ResponseEntity<String> response = restTemplate.exchange(
                    itemStatusUrl,
                    HttpMethod.GET,
                    entity,
                    String.class,
                    params
            );

            // Verificando o status na resposta
            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode rootNode = objectMapper.readTree(response.getBody());
                boolean isInWatchlist = rootNode.path("status").asBoolean();
                return isInWatchlist;
            }
        } catch (Exception e) {
            logger.error("Erro ao verificar status do item: {}", e.getMessage());
        }

        return false; // Retorna false se ocorrer algum erro ou o item não estiver na lista
    }

}