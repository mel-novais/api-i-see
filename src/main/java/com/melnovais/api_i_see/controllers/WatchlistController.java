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

    // endpoint GET para listar favoritos
    // precisa de accountId e sessionId como parametros da url
    @GetMapping("/favoritos")
    public ResponseEntity<String> listarFavoritos(
            @RequestParam String accountId,
            @RequestParam String sessionId) {

        log.info("Listando series favoritas para o accountId: {}", accountId);
        return watchlistService.listarFavoritos(accountId, sessionId);
    }

    // endpoint GET para listar tendencias
    @GetMapping("/tendencias")
    public ResponseEntity<List<Map<String, Object>>> listarTendencias(
            @RequestParam String accountId,
            @RequestParam String sessionId) {

        log.info("Listando as tendencias para o accountId: {}", accountId);
        return watchlistService.listarTendencias(accountId, sessionId);
    }

    // endpoint POST para buscar IDs de varias series a partir dos nomes
    @PostMapping("/buscarSeriesIds")
    public List<Integer> buscarSeriesIds(@RequestBody List<String> seriesNomes) throws Exception {
        log.info("Buscando IDs das series: {}", seriesNomes);
        return watchlistService.buscarSeriesIds(seriesNomes);
    }

    // endpoint POST para adicionar series aos favoritos
    // recebe lista de IDs no corpo da requisicao
    @PostMapping("/adicionarFavoritos")
    public ResponseEntity<String> adicionarSeriesFavoritas(
            @RequestBody List<Integer> seriesIds,
            @RequestParam String accountId,
            @RequestParam String sessionId) {

        log.info("Adicionando series aos favoritos: {}", seriesIds);
        return watchlistService.adicionarSeriesFavoritas(seriesIds, accountId, sessionId);
    }

    // endpoint POST para adicionar series a uma watchlist
    // recebe lista de objetos com media_id e transforma em lista de inteiros
    @PostMapping("/adicionarWatchList")
    public ResponseEntity<String> adicionarSeriesWatchlist(
            @RequestBody List<Map<String, Object>> items,
            @RequestParam String listId,
            @RequestParam String sessionId) {

        List<Integer> seriesIds = items.stream()
                .map(item -> (Number) item.get("media_id")) // pega o campo media_id
                .map(Number::intValue) // converte para int
                .collect(Collectors.toList());

        return watchlistService.adicionarSeriesWatchlist(seriesIds, listId, sessionId);
    }
}
