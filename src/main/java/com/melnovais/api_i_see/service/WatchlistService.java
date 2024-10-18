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
    private static final String API_KEY = "";
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
        // Cria uma lista de mapas com "media_type" e "media_id"
        List<Map<String, Object>> items = new ArrayList<>();
        for (Integer id : seriesIds) {
            // Adiciona os detalhes de cada série ao corpo da requisição
            Map<String, Object> item = new HashMap<>();
            item.put("media_type", "tv"); // Especifica o tipo de mídia
            item.put("media_id", id);     // Adiciona o ID da série
            items.add(item);  // Adiciona o item à lista
        }

        // Corpo da requisição contendo a lista de séries
        Map<String, Object> body = new HashMap<>();
        body.put("items", items); // Adiciona os itens ao corpo

        // Configura os headers para a requisição (definindo o tipo de conteúdo como JSON)
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        // Faz a requisição POST para adicionar as séries à lista de "Terminadas"
        ResponseEntity<String> response = restTemplate.postForEntity(
                ADD_TO_LIST_URL, request, String.class, listId, API_KEY, sessionId
        );

        // Verifica se a requisição foi bem-sucedida
        if (response.getStatusCode() == HttpStatus.OK) {
            // Loga o sucesso ao adicionar as séries à lista
            logger.info("Séries adicionadas à lista 'Terminadas' com sucesso!");
        } else {
            // Loga o erro ao tentar adicionar as séries à lista
            logger.error("Erro ao adicionar séries à lista 'Terminadas': {}", response.getBody());
        }

        // Retorna uma resposta indicando que as séries foram adicionadas à lista
        return ResponseEntity.ok("Séries adicionadas à lista 'Terminadas' com sucesso.");
    }
}
