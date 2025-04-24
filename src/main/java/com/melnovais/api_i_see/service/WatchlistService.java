package com.melnovais.api_i_see.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class WatchlistService {

    // Constantes para a chave da API e URLs das requisicoes
    private static final String API_KEY = "eba6b92a7ac8f2164e0b744b637a5af5";
    private static final String SEARCH_URL = "https://api.themoviedb.org/3/search/tv?api_key={api_key}&query={series_name}";
    private static final String FAVORITE_URL = "https://api.themoviedb.org/3/account/{account_id}/favorite?api_key={api_key}&session_id={session_id}";
    private static final String ADD_TO_LIST_URL = "https://api.themoviedb.org/4/list/{list_id}/items?api_key={api_key}&session_id={session_id}";
    private static final String MEDIA_TYPE_TV = "tv";
    private static final String SUCCESS_MSG = "Serie com ID %d %s com sucesso.";
    private static final String ERROR_MSG = "Erro ao %s serie com ID %d: %s";

    // Instancias para realizar requisicoes HTTP e manipulacao de JSON
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private static final Logger logger = LoggerFactory.getLogger(WatchlistService.class);

    // Construtor que inicializa as dependencias
    public WatchlistService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    // Metodo para buscar os IDs das series a partir dos nomes fornecidos
    public List<Integer> buscarSeriesIds(List<String> seriesNomes) throws Exception {
        List<Integer> seriesIds = new ArrayList<>();
        for (String nome : seriesNomes) {
            // Realiza a requisicao para buscar a serie pelo nome
            String response = restTemplate.getForObject(SEARCH_URL, String.class, API_KEY, nome);
            // Le o JSON e extrai os resultados
            JsonNode results = objectMapper.readTree(response).path("results");
            // Verifica se existem resultados e adiciona o primeiro ID encontrado
            if (results.isArray() && results.size() > 0) {
                seriesIds.add(results.get(0).path("id").asInt());
            } else {
                // Log de aviso caso a serie nao seja encontrada
                logger.warn("Serie nao encontrada: {}", nome);
            }
        }
        // Loga os IDs das series encontradas
        logger.info("IDs das series no formato JSON: {}", objectMapper.writeValueAsString(seriesIds));
        return seriesIds;
    }

    public ResponseEntity<List<Map<String, Object>>> listarTendencias(String accountId, String sessionId) {
        String url = "https://api.themoviedb.org/3/trending/tv/week?api_key=eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiJlYmE2YjkyYTdhYzhmMjE2NGUwYjc0NGI2MzdhNWFmNSIsIm5iZiI6MTY5Mzc4MDEzNS40Mywic3ViIjoiNjRmNTA4YTc4YzIyYzAwMGUzZWU3ZjI3Iiwic2NvcGVzIjpbImFwaV9yZWFkIl0sInZlcnNpb24iOjF9.3M1S6-JPtSocSPI9Ma9GZbT_6_ZEAceOVOsXSpFlQIQ";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("Authorization", "Bearer eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiJlYmE2YjkyYTdhYzhmMjE2NGUwYjc0NGI2MzdhNWFmNSIsIm5iZiI6MTY5Mzc4MDEzNS40Mywic3ViIjoiNjRmNTA4YTc4YzIyYzAwMGUzZWU3ZjI3Iiwic2NvcGVzIjpbImFwaV9yZWFkIl0sInZlcnNpb24iOjF9.3M1S6-JPtSocSPI9Ma9GZbT_6_ZEAceOVOsXSpFlQIQ");  // Certifique-se de atualizar sua chave API aqui

        HttpEntity<String> entity = new HttpEntity<>(headers);

        RestTemplate restTemplate = new RestTemplate();
        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

            if (response.getBody() != null && response.getBody().containsKey("results")) {
                List<Map<String, Object>> results = (List<Map<String, Object>>) response.getBody().get("results");

                List<Map<String, Object>> tvResults = results.stream()
                        .collect(Collectors.toList());

                return ResponseEntity.ok(tvResults);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.emptyList());
            }
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            logger.error("Erro na requisição para a API do TMDb: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }

    // Metodo para adicionar series aos favoritos
    public ResponseEntity<String> adicionarSeriesFavoritas(List<Integer> seriesIds, String accountId, String sessionId) {
        return processFavoriteSeries(seriesIds, accountId, sessionId, FAVORITE_URL, "favoritada");
    }

    public ResponseEntity<String> listarFavoritos(String accountId, String sessionId) {
        String url = "https://api.themoviedb.org/3/account/" + accountId + "/favorite/tv?session_id=" + sessionId;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");

        headers.set("Authorization", "Bearer eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiJlYmE2YjkyYTdhYzhmMjE2NGUwYjc0NGI2MzdhNWFmNSIsIm5iZiI6MTY5Mzc4MDEzNS40Mywic3ViIjoiNjRmNTA4YTc4YzIyYzAwMGUzZWU3ZjI3Iiwic2NvcGVzIjpbImFwaV9yZWFkIl0sInZlcnNpb24iOjF9.3M1S6-JPtSocSPI9Ma9GZbT_6_ZEAceOVOsXSpFlQIQ");

        HttpEntity<String> entity = new HttpEntity<>(headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        return response;
    }

    // Metodo para adicionar series a watchlist
    public ResponseEntity<String> adicionarSeriesWatchlist(List<Integer> seriesIds, String listId, String sessionId) {
        return processWatchlistSeries(seriesIds, listId, sessionId, ADD_TO_LIST_URL, "adicionada a lista");
    }

    // Metodo para processar a adicao de series a watchlist
    private ResponseEntity<String> processWatchlistSeries(List<Integer> seriesIds, String listId, String sessionId, String url, String action) {
        try {
            // Cria a lista de itens a partir dos IDs das series
            List<Map<String, Object>> items = new ArrayList<>();
            for (Integer seriesId : seriesIds) {
                // Cria um mapa para cada serie a ser adicionada
                Map<String, Object> item = Map.of("media_type", MEDIA_TYPE_TV, "media_id", seriesId);
                items.add(item);
            }

            // Cria o corpo da requisicao com a lista de itens
            Map<String, Object> body = Map.of("items", items);

            // Configura os cabecalhos da requisicao
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            // Envia a requisicao POST para a API e retorna a resposta
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class, listId, API_KEY, sessionId);
            List<String> mensagens = new ArrayList<>();
            // Processa a resposta recebida da API
            processResponse(response, seriesIds, action, mensagens);
            return ResponseEntity.ok(String.join("\n", mensagens));
        } catch (Exception e) {
            logger.error("Erro ao processar a resposta da API: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao processar a resposta da API.");
        }
    }

    // Metodo para processar a adicao de series aos favoritos
    private ResponseEntity<String> processFavoriteSeries(List<Integer> seriesIds, String accountId, String sessionId, String url, String action) {
        try {
            List<String> mensagens = new ArrayList<>();
            for (Integer seriesId : seriesIds) {
                // Cria o corpo da requisicao para cada serie
                Map<String, Object> body = Map.of("media_type", MEDIA_TYPE_TV, "media_id", seriesId, "favorite", true);
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

                // Envia a requisicao POST para a API e retorna a resposta
                ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class, accountId, API_KEY, sessionId);
                // Processa a resposta recebida da API
                processResponse(response, List.of(seriesId), action, mensagens);
            }
            return ResponseEntity.ok(String.join("\n", mensagens));
        } catch (Exception e) {
            logger.error("Erro ao processar a resposta da API: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao processar a resposta da API.");
        }
    }

    // Metodo para processar a resposta da requisicao
    private void processResponse(ResponseEntity<String> response, List<Integer> seriesIds, String action, List<String> mensagens) throws Exception {
        // Verifica se a resposta foi bem-sucedida (200 OK ou 201 Created)
        if (response.getStatusCode() == HttpStatus.OK || response.getStatusCode() == HttpStatus.CREATED) {
            // Le o corpo da resposta JSON
            JsonNode rootNode = objectMapper.readTree(response.getBody());
            JsonNode results = rootNode.path("results");

            // Processa cada resultado retornado
            for (JsonNode result : results) {
                boolean success = result.path("success").asBoolean();
                int mediaId = result.path("media_id").asInt();

                if (success) {
                    // Adiciona mensagem de sucesso
                    mensagens.add(String.format(SUCCESS_MSG, mediaId, action));
                    logger.info(SUCCESS_MSG, mediaId, action);
                } else {
                    // Trata os erros retornados pela API
                    handleErrors(result, mediaId, action, mensagens);
                }
            }
        } else {
            // Adiciona mensagem de erro se a resposta nao for bem-sucedida
            mensagens.add(String.format(ERROR_MSG, action, seriesIds, response.getBody()));
            logger.error(ERROR_MSG, action, seriesIds, response.getBody());
        }
    }

    // Metodo para tratar erros retornados pela API
    private void handleErrors(JsonNode rootNode, Integer seriesId, String action, List<String> mensagens) {
        JsonNode errorMessages = rootNode.path("error");
        if (errorMessages.isArray() && errorMessages.size() > 0) {
            String errorMessage = errorMessages.get(0).asText();
            if ("Media has already been taken".equals(errorMessage)) {
                // Adiciona mensagem indicando que a serie ja esta na lista
                mensagens.add(String.format("Serie com ID %d ja esta na lista.", seriesId));
            } else {
                // Adiciona mensagem de erro
                mensagens.add(String.format(ERROR_MSG, action, seriesId, errorMessage));
            }
        }
    }
}
