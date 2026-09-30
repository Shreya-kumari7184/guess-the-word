package com.guessword.guesstheword.exception;

public class MaximumGuessesExceededException extends RuntimeException {
    public MaximumGuessesExceededException() { super("Maximum of 5 guesses reached for this game"); }
}
