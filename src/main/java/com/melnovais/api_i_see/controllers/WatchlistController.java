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
    // Logger para registrar informações e eventos importantes (sucessos ou erros)
    private static final Logger logger = LoggerFactory.getLogger(WatchlistController.class);

    // Dependência do serviço que lida com as operações da watchlist
    private final WatchlistService watchlistService;

    // Construtor para injetar o serviço WatchlistService
    public WatchlistController(WatchlistService watchlistService) {
        this.watchlistService = watchlistService;
    }

    // Endpoint que recebe uma lista de nomes de séries e retorna seus IDs
    @PostMapping("/buscarSeriesIds")
    public List<Integer> buscarSeriesIds(@RequestBody List<String> seriesNomes) throws Exception {
        // Loga a lista de séries cujos IDs serão buscados
        logger.info("Buscando IDs das séries: {}", seriesNomes);
        // Chama o serviço para buscar os IDs das séries
        return watchlistService.buscarSeriesIds(seriesNomes);
    }

    // Endpoint que adiciona as séries aos favoritos da conta
    @PostMapping("/adicionarFavoritos")
    public ResponseEntity<String> adicionarSeriesFavoritas(
            @RequestBody List<Integer> seriesIds,  // Recebe os IDs das séries
            @RequestParam String accountId,        // Recebe o ID da conta
            @RequestParam String sessionId) {      // Recebe o ID da sessão
        // Loga a ação de adicionar séries aos favoritos com base nos IDs fornecidos
        logger.info("Adicionando séries aos favoritos: {}", seriesIds);
        // Chama o serviço para adicionar as séries aos favoritos
        return watchlistService.adicionarSeriesFavoritas(seriesIds, accountId, sessionId);
    }

    // Endpoint que adiciona as séries à lista "Terminadas"
    @PostMapping("/adicionarTerminadas")
    public ResponseEntity<String> adicionarSeriesTerminadas(
            @RequestBody List<Integer> seriesIds,  // Recebe os IDs das séries
            @RequestParam String listId,           // Recebe o ID da lista "Terminadas"
            @RequestParam String sessionId) {      // Recebe o ID da sessão
        // Loga a ação de adicionar séries à lista "Terminadas"
        logger.info("Adicionando séries à lista 'Terminadas': {}", seriesIds);
        // Chama o serviço para adicionar as séries à lista "Terminadas"
        return watchlistService.adicionarSeriesTerminadas(seriesIds, listId, sessionId);
    }
}
