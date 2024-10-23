package com.melnovais.api_i_see.controllers;

import com.melnovais.api_i_see.service.WatchlistService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class WatchlistController {

    // Logger para registrar informacoes sobre o controlador
    private static final Logger logger = LoggerFactory.getLogger(WatchlistController.class);
    private final WatchlistService watchlistService;

    // Construtor que inicializa o servico de watchlist
    public WatchlistController(WatchlistService watchlistService) {
        this.watchlistService = watchlistService;
    }

    // Endpoint para buscar os IDs das series
    @PostMapping("/buscarSeriesIds")
    public List<Integer> buscarSeriesIds(@RequestBody List<String> seriesNomes) throws Exception {
        logger.info("Buscando IDs das series: {}", seriesNomes);
        return watchlistService.buscarSeriesIds(seriesNomes);
    }

    // Endpoint para adicionar series aos favoritos
    @PostMapping("/adicionarFavoritos")
    public ResponseEntity<String> adicionarSeriesFavoritas(
            @RequestBody List<Integer> seriesIds,
            @RequestParam String accountId,
            @RequestParam String sessionId) {

        logger.info("Adicionando series aos favoritos: {}", seriesIds);
        return watchlistService.adicionarSeriesFavoritas(seriesIds, accountId, sessionId);
    }

    // Endpoint para adicionar series a watchlist
    @PostMapping("/adicionarWatchList")
    public ResponseEntity<String> adicionarSeriesWatchlist(
            @RequestBody List<Map<String, Object>> items,
            @RequestParam String listId,
            @RequestParam String sessionId) {

        // Extrai os IDs das series a partir dos itens recebidos
        List<Integer> seriesIds = items.stream()
                .map(item -> (Number) item.get("media_id"))
                .map(Number::intValue)
                .collect(Collectors.toList());

        return watchlistService.adicionarSeriesWatchlist(seriesIds, listId, sessionId);
    }
}