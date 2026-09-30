package com.guessword.guesstheword.exception;

public class InvalidGuessException extends RuntimeException {
    public InvalidGuessException() { super("Guess must contain exactly 5 letters (A-Z)"); }
}
