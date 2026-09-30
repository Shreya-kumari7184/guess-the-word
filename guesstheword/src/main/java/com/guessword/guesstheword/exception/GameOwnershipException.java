package com.guessword.guesstheword.exception;

public class GameOwnershipException extends RuntimeException {
    public GameOwnershipException() { this("You are not allowed to submit guesses for this game"); }
    public GameOwnershipException(String message) { super(message); }
}
