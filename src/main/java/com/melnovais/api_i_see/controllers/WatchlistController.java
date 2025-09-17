package com.melnovais.api_i_see.controllers;

import com.melnovais.api_i_see.service.WatchlistService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@Slf4j
public class WatchlistController {

    @Autowired
    private WatchlistService watchlistService;

    // precisa de accountId e sessionId como parametros da url
    @GetMapping("/favoritos")
    public ResponseEntity<String> listarFavoritos(
            @RequestParam String accountId,
            @RequestParam String sessionId) {

        log.info("Listando series favoritas para o accountId: {}", accountId);
        return watchlistService.listarFavoritos(accountId, sessionId);
    }

    @GetMapping("/tendencias")
    public ResponseEntity<List<Map<String, Object>>> listarTendencias() {
        return watchlistService.listarTendencias();
    }

    @PostMapping("/buscarSeriesIds")
    public List<Integer> buscarSeriesIds(@RequestBody List<String> seriesNomes) throws Exception {
        log.info("Buscando IDs das series: {}", seriesNomes);
        return watchlistService.buscarSeriesIds(seriesNomes);
    }

    @PostMapping("/adicionarFavoritos")
    public ResponseEntity<String> adicionarSeriesFavoritas(@RequestBody List<Integer> seriesIds) {
        log.info("Adicionando séries aos favoritos: {}", seriesIds);
        return watchlistService.adicionarSeriesFavoritas(seriesIds);
    }

    @PostMapping("/adicionarWatchList")
    public ResponseEntity<String> adicionarSeriesWatchlist(
            @RequestBody List<Integer> seriesIds,
            @RequestParam String listId) {

        return watchlistService.adicionarSeriesWatchlist(seriesIds, listId);
    }
}
