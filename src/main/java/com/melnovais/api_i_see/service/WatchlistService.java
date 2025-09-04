package com.melnovais.api_i_see.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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
@Slf4j
public class WatchlistService {

    // URLs base para requisicoes
    private static final String SEARCH_URL = "https://api.themoviedb.org/3/search/tv?api_key={api_key}&query={series_name}";
    private static final String FAVORITE_URL = "https://api.themoviedb.org/3/account/{account_id}/favorite?api_key={api_key}&session_id={session_id}";
    private static final String ADD_TO_LIST_URL = "https://api.themoviedb.org/4/list/{list_id}/items?api_key={api_key}&session_id={session_id}";

    private static final String MEDIA_TYPE_TV = "tv";
    //private static final String SUCCESS_MSG = "Serie com id: %d %s com sucesso.";
    private static final String ERROR_MSG = "Erro ao %s serie com ID %d: %s";

    @Value("${tmdb.api.key}")
    private String apiKey;
    @Value("${tmdb.api.token}")
    private String apiToken;

    // Utilitarios para requisicoes HTTP e JSON
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public WatchlistService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public List<Integer> buscarSeriesIds(List<String> seriesNomes) throws Exception {
        List<Integer> seriesIds = new ArrayList<>();

        for (String nome : seriesNomes) {
            // remove qualquer "(xxxx)" com 4 dígitos no final
            String nomeLimpo = nome.replaceAll("\\(\\d{4}\\)", "").trim();

            String response = restTemplate.getForObject(SEARCH_URL, String.class, apiKey, nomeLimpo);
            JsonNode results = objectMapper.readTree(response).path("results");

            if (results.isArray() && results.size() > 0) {
                seriesIds.add(results.get(0).path("id").asInt());
            } else {
                log.warn("Serie nao encontrada: {}", nomeLimpo);
            }
        }

        log.info("ids das series: {}", objectMapper.writeValueAsString(seriesIds));
        return seriesIds;
    }

    public ResponseEntity<List<Map<String, Object>>> listarTendencias(String accountId, String sessionId) {
        String url = "https://api.themoviedb.org/3/trending/tv/week?api_key=" + apiKey;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("Authorization", "Bearer " + apiToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

            if (response.getBody() != null && response.getBody().containsKey("results")) {
                List<Map<String, Object>> results = (List<Map<String, Object>>) response.getBody().get("results");
                return ResponseEntity.ok(results);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.emptyList());
            }
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("Erro na requisicao para a API: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }

    public ResponseEntity<String> adicionarSeriesFavoritas(List<Integer> seriesIds, String accountId, String sessionId) {
        return processFavoriteSeries(seriesIds, accountId, sessionId, FAVORITE_URL, "favoritada");
    }

    public ResponseEntity<String> listarFavoritos(String accountId, String sessionId) {
        String url = "https://api.themoviedb.org/3/account/" + accountId + "/favorite/tv?session_id=" + sessionId;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("Authorization", "Bearer " + apiToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        return restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
    }

    public ResponseEntity<String> adicionarSeriesWatchlist(List<Integer> seriesIds, String listId, String sessionId) {
        return processWatchlistSeries(seriesIds, listId, sessionId, ADD_TO_LIST_URL, "adicionada a lista");
    }

    // Processa adicao de series em uma lista customizada
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

            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class, listId, apiKey, sessionId);

            List<String> mensagens = new ArrayList<>();
            processResponse(response, seriesIds, action, mensagens);

            return ResponseEntity.ok(String.join("\n", mensagens));
        } catch (Exception e) {
            log.error("Erro ao processar resposta da API: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao processar a resposta da API.");
        }
    }

    // Processa adicao de series como favoritas
    private ResponseEntity<String> processFavoriteSeries(List<Integer> seriesIds, String accountId, String sessionId, String url, String action) {
        try {
            List<String> mensagens = new ArrayList<>();
            for (Integer seriesId : seriesIds) {
                Map<String, Object> body = Map.of(
                        "media_type", MEDIA_TYPE_TV,
                        "media_id", seriesId,
                        "favorite", true);

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

                ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class, accountId, apiKey, sessionId);
                processResponse(response, List.of(seriesId), action, mensagens);
            }
            return ResponseEntity.ok(String.join("\n", mensagens));
        } catch (Exception e) {
            log.error("Erro ao processar resposta da API: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao processar a resposta da API.");
        }
    }

    // Processa resposta da API e gera mensagens
    private void processResponse(ResponseEntity<String> response, List<Integer> seriesIds, String action, List<String> mensagens) {
        try {
            if (response.getStatusCode() == HttpStatus.OK || response.getStatusCode() == HttpStatus.CREATED) {
                JsonNode rootNode = objectMapper.readTree(response.getBody());
                boolean success = rootNode.path("success").asBoolean(false);
                String statusMessage = rootNode.path("status_message").asText();

                for (Integer seriesId : seriesIds) {
                    if (success) {
                        mensagens.add(String.format("Serie com id %d %s com sucesso", seriesId, action));
                    } else {
                        mensagens.add(String.format("Falha ao %s serie %d", action, seriesId));
                    }
                }
            } else {
                mensagens.add(String.format(ERROR_MSG, action, seriesIds, response.getBody()));
                log.error(ERROR_MSG, action, seriesIds, response.getBody());
            }
        } catch (Exception e) {
            mensagens.add(String.format("Erro ao processar a resposta da API para series %s: %s", seriesIds, e.getMessage()));
            log.error("Erro ao processar resposta da API", e);
        }
    }


    // Trata erros retornados pela API
    private void handleErrors(JsonNode rootNode, Integer seriesId, String action, List<String> mensagens) {
        JsonNode errorMessages = rootNode.path("error");
        if (errorMessages.isArray() && errorMessages.size() > 0) {
            String errorMessage = errorMessages.get(0).asText();
            if ("Media has already been taken".equals(errorMessage)) {
                mensagens.add(String.format("Serie com id: %d ja esta na lista.", seriesId));
            } else {
                mensagens.add(String.format(ERROR_MSG, action, seriesId, errorMessage));
            }
        }
    }
}

