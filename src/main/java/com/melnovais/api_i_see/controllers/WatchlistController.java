package com.melnovais.api_i_see.controllers;

import com.melnovais.api_i_see.service.WatchlistService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class WatchlistController {
    private static final Logger logger = LoggerFactory.getLogger(WatchlistController.class);

    private final WatchlistService watchlistService;

    public WatchlistController(WatchlistService watchlistService) {
        this.watchlistService = watchlistService;
    }

    // Endpoint para buscar IDs das séries
    @PostMapping("/buscarSeriesIds")
    public List<Integer> buscarSeriesIds(@RequestBody List<String> seriesNomes) throws Exception {
        logger.info("Buscando IDs das séries: {}", seriesNomes);
        return watchlistService.buscarSeriesIds(seriesNomes);
    }

    // Endpoint para adicionar séries aos favoritos
    @PostMapping("/adicionarFavoritos")
    public ResponseEntity<String> adicionarSeriesFavoritas(
            @RequestBody List<Integer> seriesIds,
            @RequestParam String accountId,
            @RequestParam String sessionId) {
        logger.info("Adicionando séries aos favoritos: {}", seriesIds);
        return watchlistService.adicionarSeriesFavoritas(seriesIds, accountId, sessionId);
    }
}
