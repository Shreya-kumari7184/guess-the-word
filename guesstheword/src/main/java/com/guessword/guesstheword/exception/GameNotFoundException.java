package com.guessword.guesstheword.exception;

public class GameNotFoundException extends RuntimeException {
    public GameNotFoundException(Long gameId) { super("Game not found: " + gameId); }
}
