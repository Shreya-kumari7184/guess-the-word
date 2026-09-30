package com.guessword.guesstheword.exception;

public class CompletedGameException extends RuntimeException {
    public CompletedGameException() { super("This game is already completed"); }
}
