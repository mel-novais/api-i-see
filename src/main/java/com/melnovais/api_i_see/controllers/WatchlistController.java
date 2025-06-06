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

    @GetMapping("/favoritos")
    public ResponseEntity<String> listarFavoritos(
            @RequestParam String accountId,
            @RequestParam String sessionId) {

        logger.info("Listando séries favoritas para o accountId: {}", accountId);
        return watchlistService.listarFavoritos(accountId, sessionId);
    }

    @GetMapping("/tendencias")
    public ResponseEntity<List<Map<String, Object>>> listarTendencias(
            @RequestParam String accountId,
            @RequestParam String sessionId) {

        logger.info("Listando as tendências para o accountId: {}", accountId);
        return watchlistService.listarTendencias(accountId, sessionId);
    }

    // Endpoint para buscar os IDs das series
    @PostMapping("/buscarSeriesIds")
    public List<Integer> buscarSeriesIds(@RequestBody List<String> seriesNomes) throws Exception {
        // Loga a operacao de busca de IDs das series
        logger.info("Buscando IDs das series: {}", seriesNomes);
        // Chama o servico para buscar os IDs das series
        return watchlistService.buscarSeriesIds(seriesNomes);
    }

    // Endpoint para adicionar series aos favoritos
    @PostMapping("/adicionarFavoritos")
    public ResponseEntity<String> adicionarSeriesFavoritas(
            @RequestBody List<Integer> seriesIds,
            @RequestParam String accountId,
            @RequestParam String sessionId) {

        // Loga a operacao de adicionar series aos favoritos
        logger.info("Adicionando series aos favoritos: {}", seriesIds);
        // Chama o servico para adicionar as series aos favoritos
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

        // Chama o servico para adicionar as series a watchlist
        return watchlistService.adicionarSeriesWatchlist(seriesIds, listId, sessionId);
    }
}
