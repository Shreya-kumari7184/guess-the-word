package com.guessword.guesstheword.controller;

import com.guessword.guesstheword.dto.GameStartResponse;
import com.guessword.guesstheword.dto.GameHistoryResponse;
import com.guessword.guesstheword.dto.GuessRequest;
import com.guessword.guesstheword.dto.GuessResultResponse;
import com.guessword.guesstheword.service.GameService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/games")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping("/start")
    public ResponseEntity<GameStartResponse> startGame(Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED).body(gameService.startGame(authentication));
    }

    @GetMapping("/{gameId}")
    public ResponseEntity<GameHistoryResponse> getGameHistory(@PathVariable Long gameId,
                                                               Authentication authentication) {
        return ResponseEntity.ok(gameService.getGameHistory(gameId, authentication));
    }

    @PostMapping("/{gameId}/guess")
    public ResponseEntity<GuessResultResponse> submitGuess(@PathVariable Long gameId,
                                                           @Valid @RequestBody GuessRequest request,
                                                           Authentication authentication) {
        return ResponseEntity.ok(gameService.submitGuess(gameId, request, authentication));
    }
}
