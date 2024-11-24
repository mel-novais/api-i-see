package com.melnovais.api_i_see.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class WatchlistService {

    private static final String API_KEY = "eba6b92a7ac8f2164e0b744b637a5af5";
    private static final String SEARCH_URL = "https://api.themoviedb.org/3/search/tv?api_key={api_key}&query={series_name}";
    private static final String FAVORITE_URL = "https://api.themoviedb.org/3/account/{account_id}/favorite?api_key={api_key}&session_id={session_id}";
    private static final String ADD_TO_LIST_URL = "https://api.themoviedb.org/4/list/{list_id}/items?api_key={api_key}&session_id={session_id}";
    private static final String MEDIA_TYPE_TV = "tv";
    private static final String SUCCESS_MSG = "Serie com ID %d %s com sucesso.";
    private static final String ERROR_MSG = "Erro ao %s serie com ID %d: %s";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private static final Logger logger = LoggerFactory.getLogger(WatchlistService.class);

    public WatchlistService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public List<Integer> buscarSeriesIds(List<String> seriesNomes) throws Exception {
        List<Integer> seriesIds = new ArrayList<>();
        for (String nome : seriesNomes) {
            String response = restTemplate.getForObject(SEARCH_URL, String.class, API_KEY, nome);
            JsonNode results = objectMapper.readTree(response).path("results");
            if (results.isArray() && results.size() > 0) {
                seriesIds.add(results.get(0).path("id").asInt());
            } else {
                logger.warn("Serie nao encontrada: {}", nome);
            }
        }
        logger.info("IDs das series no formato JSON: {}", objectMapper.writeValueAsString(seriesIds));
        return seriesIds;
    }

    public ResponseEntity<String> adicionarSeriesFavoritas(List<Integer> seriesIds, String accountId, String sessionId) {
        return processFavoriteSeries(seriesIds, accountId, sessionId, FAVORITE_URL, "favoritada");
    }

    public ResponseEntity<String> adicionarSeriesWatchlist(List<Integer> seriesIds, String listId, String sessionId) {
        return processWatchlistSeries(seriesIds, listId, sessionId, ADD_TO_LIST_URL, "adicionada a lista");
    }

    private ResponseEntity<String> processWatchlistSeries(List<Integer> seriesIds, String listId, String sessionId, String url, String action) {
        try {
            List<Map<String, Object>> items = new ArrayList<>();
            for (Integer seriesId : seriesIds) {
                Map<String, Object> item = Map.of("media_type", MEDIA_TYPE_TV, "media_id", seriesId);
                items.add(item);
            }

            Map<String, Object> body = Map.of("items", items);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class, listId, API_KEY, sessionId);
            List<String> mensagens = new ArrayList<>();
            processResponse(response, seriesIds, action, mensagens);
            return ResponseEntity.ok(String.join("\n", mensagens));
        } catch (Exception e) {
            logger.error("Erro ao processar a resposta da API: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao processar a resposta da API.");
        }
    }

    private ResponseEntity<String> processFavoriteSeries(List<Integer> seriesIds, String accountId, String sessionId, String url, String action) {
        try {
            List<String> mensagens = new ArrayList<>();
            for (Integer seriesId : seriesIds) {
                Map<String, Object> body = Map.of("media_type", MEDIA_TYPE_TV, "media_id", seriesId, "favorite", true);
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

                ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class, accountId, API_KEY, sessionId);
                processResponse(response, List.of(seriesId), action, mensagens);
            }
            return ResponseEntity.ok(String.join("\n", mensagens));
        } catch (Exception e) {
            logger.error("Erro ao processar a resposta da API: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao processar a resposta da API.");
        }
    }

    private void processResponse(ResponseEntity<String> response, List<Integer> seriesIds, String action, List<String> mensagens) throws Exception {
        if (response.getStatusCode() == HttpStatus.OK || response.getStatusCode() == HttpStatus.CREATED) {
            JsonNode rootNode = objectMapper.readTree(response.getBody());
            JsonNode results = rootNode.path("results");
            for (JsonNode result : results) {
                boolean success = result.path("success").asBoolean();
                int mediaId = result.path("media_id").asInt();
                if (success) {
                    mensagens.add(String.format(SUCCESS_MSG, mediaId, action));
                    logger.info(SUCCESS_MSG, mediaId, action);
                } else {
                    handleErrors(result, mediaId, action, mensagens);
                }
            }
        } else {
            mensagens.add(String.format(ERROR_MSG, action, seriesIds, response.getBody()));
            logger.error(ERROR_MSG, action, seriesIds, response.getBody());
        }
    }

    private void handleErrors(JsonNode rootNode, Integer seriesId, String action, List<String> mensagens) {
        JsonNode errorMessages = rootNode.path("error");
        if (errorMessages.isArray() && errorMessages.size() > 0) {
            String errorMessage = errorMessages.get(0).asText();
            if ("Media has already been taken".equals(errorMessage)) {
                mensagens.add(String.format("Serie com ID %d ja esta na lista.", seriesId));
            } else {
                mensagens.add(String.format(ERROR_MSG, action, seriesId, errorMessage));
            }
        }
    }
}
