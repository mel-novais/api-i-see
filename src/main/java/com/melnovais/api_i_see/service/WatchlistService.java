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
    private static final String API_KEY = "";
    private static final String SEARCH_URL = "https://api.themoviedb.org/3/search/tv?api_key={api_key}&query={series_name}";
    private static final String FAVORITE_URL = "https://api.themoviedb.org/3/account/{account_id}/favorite?api_key={api_key}&session_id={session_id}";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    // Adicionando o Logger
    private static final Logger logger = LoggerFactory.getLogger(WatchlistService.class);

    public WatchlistService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    // Busca IDs das séries
    public List<Integer> buscarSeriesIds(List<String> seriesNomes) throws Exception {
        List<Integer> seriesIds = new ArrayList<>();

        for (String nome : seriesNomes) {
            String response = restTemplate.getForObject(SEARCH_URL, String.class, API_KEY, nome);
            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode results = rootNode.path("results");

            if (results.isArray() && results.size() > 0) {
                int mediaId = results.get(0).path("id").asInt();
                seriesIds.add(mediaId);
            } else {
                logger.warn("Série não encontrada: {}", nome);
            }
        }

        logger.info("IDs das séries no formato JSON: {}", objectMapper.writeValueAsString(seriesIds));
        return seriesIds;
    }

    // Adiciona séries aos favoritos
    public ResponseEntity<String> adicionarSeriesFavoritas(List<Integer> seriesIds, String accountId, String sessionId) {
        for (Integer id : seriesIds) {
            Map<String, Object> body = Map.of(
                    "media_type", "tv",
                    "media_id", id,
                    "favorite", true
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(FAVORITE_URL, request, String.class, accountId, API_KEY, sessionId);

            if (response.getStatusCode() == HttpStatus.OK) {
                logger.info("Série favoritada com sucesso! ID: {}", id);
            } else {
                logger.error("Erro ao favoritar série: {} - {}", id, response.getBody());
            }
        }

        return ResponseEntity.ok("Séries processadas com sucesso.");
    }
}
